/*
 * Copyright (C) 2018 maximen39
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.luckyverse.duels.arena.states;

import com.google.inject.Inject;
import net.luckyverse.api.actionbar.ActionbarProvider;
import net.luckyverse.api.configuration.Configuration;
import net.luckyverse.api.scoreboard.Objective;
import net.luckyverse.api.scoreboard.Scoreboard;
import net.luckyverse.api.scoreboard.ScoreboardProvider;
import net.luckyverse.api.translate.TranslationProvider;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.arena.DuelsTeam;
import net.luckyverse.duels.configuration.DuelsConfiguration;
import net.luckyverse.duels.kit.Kit;
import net.luckyverse.duels.profile.DuelsProfile;
import net.luckyverse.duels.service.DuelsService;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author maximen39
 */
public class PlayingState extends AbstractState<DuelsArena, DuelsProfile> implements Listener {
    private static final String ALREADY_PLAYING = "Игра уже начата";

    @Inject
    private Plugin plugin;
    @Inject
    private TranslationProvider translationProvider;
    @Inject
    private ScoreboardProvider scoreboardProvider;
    @Inject
    private ActionbarProvider actionbarProvider;
    @Inject
    private Configuration configuration;
    @Inject
    private DuelsService duelsService;

    private Map<DuelsProfile, BukkitRunnable> scoreboardRunnableMap = new HashMap<>();
    private BukkitRunnable gameRunnable;
    private int timeLeft;

    @Override
    public void startStage(DuelsArena arena) {
        super.startStage(arena);
        arena.activeStage(this);
        arena.gameId(duelsService.startGame());
        timeLeft = arena.gameDuration();

        arena.players().forEach(duelsProfile -> scoreboardRunnableMap
                .put(duelsProfile, createScoreboard(duelsProfile)));
        arena.alivePlayers().forEach(this::giveKit);
        arena.teams().forEach(duelsTeam -> duelsTeam.players()
                .forEach(profile -> profile.player().teleport(duelsTeam.spawn())));
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startGameRunnable();
    }

    private void startGameRunnable() {
        gameRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeft > 0) {
                    timeLeft--;
                } else {
                    cancel();
                    nextStage().startStage(arena);
                }
            }
        };
        gameRunnable.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        arena.getProfile(event.getPlayer()).ifPresent(profile -> {
            if (arena.isPlayerAlive(profile)) {
                Entity killer = lastDamage(profile);
                killPlayer(canKill(killer, profile) ? killer : null, profile, "leave");
            }

            if (arena.hasSpectator(profile)) {
                arena.removeSpectator(profile);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            arena.getProfile((Player) event.getEntity()).ifPresent(profile -> {
                if (event.isCancelled()) {
                    return;
                }
                DecimalFormat df = new DecimalFormat("#.#");
                df.setRoundingMode(RoundingMode.UP);

                Player player = (Player) event.getEntity();
                Entity killer = getDamagerIfArrow(event.getDamager());

                if (killer != null && canKill(killer, profile)) {
                    if (killer instanceof Player) {
                        arena.getProfile((Player) killer).ifPresent(killerProfile -> {
                            actionbarProvider.create()
                                    .text(translate("damageGiven", killerProfile)
                                            .replace("$damage", df.format(event.getFinalDamage())))
                                    .send((Player) killer);
                            killerProfile.addGivenDamage(event.getFinalDamage());
                        });
                    }

                    if (player.getHealth() - event.getFinalDamage() <= 0) {
                        playKillSound(player);
                        killPlayer(killer, profile, event.getCause().name().toLowerCase());
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            });
        } else if (event.getDamager() instanceof Player) {
            if (event.isCancelled()) {
                return;
            }
            arena.getProfile((Player) event.getDamager()).ifPresent(profile -> {
                if (!arena.isPlayerAlive(profile) || arena.hasSpectator(profile)) {
                    event.setCancelled(true);
                }
            });
        }
    }

    private void playKillSound(Player player) {
        player.playSound(player.getLocation(), Sound.HURT_FLESH, 10, 1);
        player.getNearbyEntities(5, 5, 5)
                .stream().filter(entity -> entity instanceof Player)
                .forEach((p -> ((Player) p).playSound(
                        player.getLocation(),
                        Sound.HURT_FLESH,
                        10, 1)));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        arena.getProfile(event.getEntity().getName()).ifPresent(profile -> {
            if (event.isCancelled()) {
                return;
            }

            Player player = (Player) event.getEntity();
            if (player.getHealth() - event.getFinalDamage() <= 0) {
                playKillSound(player);
                killPlayer(null, profile, event.getCause().name().toLowerCase());
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        arena.getProfile(event.getPlayer()).ifPresent(profile -> {
            if (arena.isPlayerAlive(profile) && !arena.hasSpectator(profile) &&
                    event.canBuild() && !event.isCancelled()) {
                arena.addBlock(event.getBlock());
            } else {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        arena.getProfile(event.getPlayer()).ifPresent(profile -> {
            if (arena.isPlayerAlive(profile) && !arena.hasSpectator(profile) &&
                    !event.isCancelled() && arena.canDestroy(event.getBlock())) {
                arena.removeBlock(event.getBlock());
            } else {
                event.setCancelled(true);
            }
        });
    }

    private void giveKit(DuelsProfile profile) {
        Kit kit = profile.selectedKit() != null ? profile.selectedKit() : arena.defaultKit();
        if (kit == null) {
            return;
        }

        PlayerInventory playerInventory = profile.player().getInventory();
        for (int i = 0; i < kit.hotBarItems().size() && i < 9; i++) {
            playerInventory.setItem(i, kit.hotBarItems().get(i));
        }
        for (int i = 0; i < kit.inventoryItems().size(); i++) {
            playerInventory.setItem(i + 9, kit.inventoryItems().get(i));
        }
        playerInventory.setHelmet(kit.helmet());
        playerInventory.setChestplate(kit.chestplate());
        playerInventory.setLeggings(kit.leggings());
        playerInventory.setBoots(kit.boots());
    }

    protected Entity lastDamage(DuelsProfile profile) {
        Player player = profile.player();
        EntityDamageEvent lastDamageCause = player.getLastDamageCause();
        if (lastDamageCause instanceof EntityDamageByEntityEvent) {
            return getDamagerIfArrow(((EntityDamageByEntityEvent) lastDamageCause).getDamager());
        }
        return null;
    }

    protected Entity getDamagerIfArrow(Entity damager) {
        if (damager instanceof Arrow && ((Arrow) damager).getShooter() instanceof Entity) {
            damager = (Entity) ((Arrow) damager).getShooter();
        }
        return damager;
    }

    protected void killPlayer(Entity killer, DuelsProfile victim, String reason) {
        if (killer instanceof Player) {
            arena.getProfile(killer.getName()).ifPresent(profile -> {
                profile.addKill(victim, arena);
                profile.addCoins(((DuelsConfiguration) configuration).coinsForKill);
            });
        }

        victim.addDeath(reason, arena);
        arena.removeAlivePlayer(victim).addSpectator(victim);
        sendDeathMessage(reason, killer, victim);

        if (arena.aliveEnemyTeams().count() <= 1) {
            nextStage().startStage(arena);
        }
    }

    private void sendDeathMessage(String reason, Entity killer, DuelsProfile victim) {
        for (DuelsProfile profile : arena.players()) {
            profile.player().sendMessage(killer != null ? translate("deathMessageWithKiller_" + reason, profile)
                    .replace("$killer", killer.getName())
                    .replace("$victim", victim.name()) : translate("deathMessage_" + reason, profile)
                    .replace("$victim", victim.name()));
        }
    }

    protected boolean canKill(Entity killer, DuelsProfile victim) {
        if (killer instanceof Player) {
            DuelsProfile profile = arena.getProfile(killer.getName()).orElse(null);
            return profile != null && arena.isPlayerAlive(profile) && !arena.hasSpectator(profile) &&
                    arena.isPlayerAlive(victim) && !arena.hasSpectator(victim) &&
                    !arena.isAllies(profile, victim);
        } else {
            return arena.isPlayerAlive(victim) && !arena.hasSpectator(victim);
        }
    }

    private BukkitRunnable createScoreboard(DuelsProfile profile) {
        Scoreboard scoreboard = scoreboardProvider.create().sendPlayer(profile.player());
        Objective objective = scoreboard.newObjective();
        objective.setTitle(translate("scoreboardTitle", profile));

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                List<String> lines = new ArrayList<>();
                lines.add(translate("scoreboardLineTimeLeft", profile)
                        .replace("$time", formattedTime(timeLeft)));
                lines.add("");
                if (arena.alivePlayers().size() > 2) {
                    lines.add(translate("scoreboardLineKills", profile)
                            .replace("$kills", String.valueOf(profile.statistics().kills())));
                    lines.add("");
                }
                lines.add(translate("scoreboardLineMode", profile)
                        .replace("$mode", arena.name()));
                lines.add(translate("scoreboardLineBestStreak", profile)
                        .replace("$bestStreak", String.valueOf(profile.statistics().gameableMaxWinStreak())));
                lines.add("");
                lines.add(translate("domain", profile));

                objective.setLines(lines);
            }
        };
        runnable.runTaskTimer(plugin, 0L, 3L);
        return runnable;
    }

    private String formattedTime(int sec) {
        int minutes = sec / 60;
        int seconds = sec % 60;
        String disMin = (minutes < 10 ? "0" : "") + minutes;
        String disSec = (seconds < 10 ? "0" : "") + seconds;
        return disMin + ":" + disSec;
    }

    private void sendEndGameMsg(DuelsProfile profile, DuelsTeam winner) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.UP);
        Player player = profile.player();

        if (winner != null) {
            long kills = arena.teams().stream()
                    .filter(duelsTeam -> duelsTeam == winner)
                    .flatMap(duelsTeam -> duelsTeam.players().stream())
                    .mapToLong(p -> p.statistics().kills()).sum();

            player.sendMessage(translate("gameOverMsg", profile)
                    .replace("$winner", Arrays.toString(winner.players()
                            .stream().map(DuelsProfile::name).toArray())
                            .replaceAll("[\\[\\]]*", ""))
                    .replace("$kills", String.valueOf(kills)));
        } else {
            double damageGiven = arena.teams().stream()
                    .flatMap(duelsTeam -> duelsTeam.players().stream())
                    .mapToDouble(p -> p.statistics().damageGiven()).sum();

            player.sendMessage(translate("gameOverMsgDraw", profile)
                    .replace("$damageGiven", df.format(damageGiven)));
        }
    }

    private DuelsTeam getWinner() {
        return arena.aliveEnemyTeams().count() <= 1 ? arena.aliveEnemyTeams().findFirst().orElse(null) : null;
    }

    @Override
    public void endStage() {
        HandlerList.unregisterAll(this);
        scoreboardRunnableMap.values().forEach(BukkitRunnable::cancel);
        scoreboardRunnableMap.clear();
        gameRunnable.cancel();
        DuelsTeam winner = getWinner();
        arena.players().forEach(profile -> sendEndGameMsg(profile, winner));
        if (winner != null) {
            winner.players().forEach(profile -> {
                profile.addWin(arena);
                profile.addCoins(((DuelsConfiguration) configuration).coinsForWin);
            });
        }
        arena.players().stream()
                .filter(profile -> profile.player().isOnline())
                .forEachOrdered(DuelsProfile::addGame);
        duelsService.endGame(arena.gameId());
    }

    private String translate(String key, DuelsProfile profile) {
        return translationProvider.forKey(key, profile.locale());
    }
}
