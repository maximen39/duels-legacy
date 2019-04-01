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
import net.luckyverse.api.tag.Tag;
import net.luckyverse.api.tag.TagProvider;
import net.luckyverse.api.translate.TranslationProvider;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.arena.DuelsTeam;
import net.luckyverse.duels.kit.gui.ItemBuilder;
import net.luckyverse.duels.profile.DuelsProfile;
import net.luckyverse.duels.store.CaseInsensitiveMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author maximen39
 */
public class WaitingState extends AbstractState<DuelsArena, DuelsProfile> implements Listener {
    private static Material OPEN_GUI_MATERIAL = Material.NETHER_STAR;

    @Inject
    private Plugin plugin;
    @Inject
    @Named("profile")
    private Store<String, Optional<DuelsProfile>> profileStore;
    @Inject
    private TranslationProvider translationProvider;
    @Inject
    private ScoreboardProvider scoreboardProvider;
    @Inject
    private TagProvider tagProvider;

    private Map<DuelsProfile, BukkitRunnable> scoreboardRunnableMap = new HashMap<>();
    private HashMap<String, Tag> tags = new CaseInsensitiveMap<>();

    @Override
    public void startStage(DuelsArena arena) {
        super.startStage(arena);
        arena.activeStage(this);
        arena.players().forEach(duelsProfile -> scoreboardRunnableMap
                .put(duelsProfile, createScoreboard(duelsProfile)));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        arena.clearInventory(player);
        arena.resetPlayer(player);

        arena.players().forEach(profile -> {
            player.showPlayer(profile.player());
            profile.player().showPlayer(player);
        });

        profileStore.fetch(player.getName()).ifPresent(profile -> {
            profile.player(player);
            arena.addPlayer(profile);
            DuelsTeam duelsTeam = arena
                    .suitableTeam(profile)
                    .addPlayer(profile);
            player.teleport(duelsTeam.spawn());

            if (arena.kits().size() > 1) {
                player.getInventory().setItem(0, new ItemBuilder(OPEN_GUI_MATERIAL)
                        .name(translate("netherStarSelectKit", profile)).build());
            }

            for (DuelsProfile duelsProfile : arena.players()) {
                duelsProfile.player().sendMessage(translate("joinMsg", duelsProfile)
                        .replace("$player", profile.name())
                        .replace("$online", String.valueOf(arena.players().size()))
                        .replace("$limit", String.valueOf(arena.limit())));
            }

            if (arena.players().size() >= arena.limit()) {
                nextStage().startStage(arena);
            } else {
                scoreboardRunnableMap.put(profile, createScoreboard(profile));
                setTag(profile, duelsTeam.getColor().toString());
            }
        });
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
        });
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
                        translate("scoreboardLineWait", profile),
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

    private void setTag(DuelsProfile profile, String prefix) {
        List<Player> players = arena.players().stream().map(DuelsProfile::player).collect(Collectors.toList());
        tags.forEach((key, value) -> arena.getProfile(key)
                .ifPresent(p -> value.apply(
                        p.player(),
                        Collections.singleton(profile.player()))
                ));

        Tag tag = tagProvider.create().prefix(prefix);
        tag.apply(profile.player(), players);
        tags.put(profile.name(), tag);

        profile.player().setPlayerListName(profile.displayName());
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

    @EventHandler
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
        HandlerList.unregisterAll(this);
        scoreboardRunnableMap.values().forEach(BukkitRunnable::cancel);
        scoreboardRunnableMap.clear();
    }

    private String translate(String key, DuelsProfile profile) {
        return translationProvider.forKey(key, profile.locale());
    }
}
