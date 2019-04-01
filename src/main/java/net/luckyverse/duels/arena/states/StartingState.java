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
import net.luckyverse.api.scoreboard.Objective;
import net.luckyverse.api.scoreboard.Scoreboard;
import net.luckyverse.api.scoreboard.ScoreboardProvider;
import net.luckyverse.api.store.Store;
import net.luckyverse.api.translate.TranslationProvider;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.profile.DuelsProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.inject.Named;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author maximen39
 */
public class StartingState extends AbstractState<DuelsArena, DuelsProfile> implements Listener {
    private static Material OPEN_GUI_MATERIAL = Material.NETHER_STAR;

    @Inject
    private Plugin plugin;
    @Inject
    private TranslationProvider translationProvider;
    @Inject
    private ScoreboardProvider scoreboardProvider;
    @Inject
    @Named("profile")
    private Store<String, Optional<DuelsProfile>> profileStore;

    private int startGameDelay;
    private BukkitRunnable startGameDelayRunnable;
    private Map<DuelsProfile, BukkitRunnable> scoreboardRunnableMap = new HashMap<>();

    @Override
    public void startStage(DuelsArena arena) {
        super.startStage(arena);
        arena.activeStage(this);
        startGameDelay = 10;
        arena.players().forEach(duelsProfile -> scoreboardRunnableMap
                .put(duelsProfile, createScoreboard(duelsProfile)));
        startDelayRunnable();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        arena.getProfile(event.getPlayer()).ifPresent(profile -> {
            arena.teams().stream()
                    .filter(duelsTeam -> duelsTeam.players().contains(profile))
                    .forEach(duelsTeam -> duelsTeam.removePlayer(profile));
            arena.removePlayer(profile);
            scoreboardRunnableMap.get(profile).cancel();
            scoreboardRunnableMap.remove(profile);

            for (DuelsProfile duelsProfile : arena.players()) {
                duelsProfile.player().sendMessage(translate("quitMsg", duelsProfile)
                        .replace("$player", profile.name())
                        .replace("$online", String.valueOf(arena.players().size()))
                        .replace("$limit", String.valueOf(arena.limit())));
            }

            profileStore.remove(event.getPlayer().getName());
            if (arena.players().size() < arena.limit()) {
                previousStage().startStage(arena);
            }
        });
    }

    private void startDelayRunnable() {
        startGameDelayRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (startGameDelay > 0) {
                    for (DuelsProfile duelsProfile : arena.players()) {
                        duelsProfile.player().sendMessage(translate("gameStartIn", duelsProfile)
                                .replace("$seconds", String.valueOf(startGameDelay)));
                    }
                    startGameDelay--;
                } else {
                    arena.players().forEach(arena::addAlivePlayer);
                    nextStage().startStage(arena);
                }
            }
        };
        startGameDelayRunnable.runTaskTimer(plugin, 0L, 20L);
    }

    private BukkitRunnable createScoreboard(DuelsProfile profile) {
        Scoreboard scoreboard = scoreboardProvider.create().sendPlayer(profile.player());
        Objective objective = scoreboard.newObjective();
        objective.setTitle(translate("scoreboardTitle", profile));

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                objective.setLines(Arrays.asList(
                        "",
                        translate("scoreboardLinePlayers", profile)
                                .replace("$online", String.valueOf(arena.players().size()))
                                .replace("$limit", String.valueOf(arena.limit())),
                        "",
                        translate("scoreboardLineStartIn", profile)
                                .replace("$seconds", String.valueOf(startGameDelay + 1)),
                        "",
                        translate("scoreboardLineArenaName", profile)
                                .replace("$arenaName", arena.name()),
                        "",
                        translate("domain", profile)));
            }
        };
        runnable.runTaskTimer(plugin, 0L, 3L);
        return runnable;
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (arena.getProfile(event.getPlayer()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (arena.getProfile(event.getPlayer()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (arena.getProfile(event.getPlayer()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && arena.getProfile(event.getEntity().getName()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (arena.getProfile(event.getEntity().getName()).isPresent()) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event) {
        if (arena.getProfile(event.getWhoClicked().getName()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (arena.getProfile(event.getPlayer()).isPresent()) {
            event.setCancelled(true);
            if (event.hasItem() && event.getItem().getType() == OPEN_GUI_MATERIAL) {
                arena.getProfile(event.getPlayer()).ifPresent(profile ->
                        arena.openKitSelector(profile, translationProvider));
            }
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (arena.getProfile(event.getPlayer()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @Override
    public void endStage() {
        startGameDelayRunnable.cancel();
        startGameDelayRunnable = null;
        HandlerList.unregisterAll(this);
        scoreboardRunnableMap.values().forEach(BukkitRunnable::cancel);
        scoreboardRunnableMap.clear();
    }

    private String translate(String key, DuelsProfile profile) {
        return translationProvider.forKey(key, profile.locale());
    }
}
