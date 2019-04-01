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
import com.google.inject.name.Named;
import net.luckyverse.api.store.Store;
import net.luckyverse.api.translate.TranslationProvider;
import net.luckyverse.connector.Connector;
import net.luckyverse.connector.ServernameFilterType;
import net.luckyverse.connector.connections.CoreChannel;
import net.luckyverse.connector.connections.DataConnection;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.profile.DuelsProfile;
import net.luckyverse.duels.service.DuelsService;
import net.luckyverse.duels.store.DuelsInMemoryProfileStoreCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author maximen39
 */
public class FinishState extends AbstractState<DuelsArena, DuelsProfile> implements Listener {

    private static final String ARENA_REBOOTS = "Arena reboots";

    @Inject
    private Plugin plugin;
    @Inject
    private DuelsService service;
    @Inject
    private TranslationProvider translationProvider;
    @Inject
    @Named("profile")
    private Store<String, Optional<DuelsProfile>> profileStore;
    private int nextStageDelay = 10;

    @Override
    public void startStage(DuelsArena arena) {
        super.startStage(arena);
        arena.activeStage(this);
        nextStageDelay = 10;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        arena.clearBlocks();
        Bukkit.getWorlds().forEach(world -> world.getEntities()
                .stream().filter(entity -> entity instanceof Item)
                .forEach(Entity::remove));

        for (DuelsProfile profile : arena.players()) {
            Player player = profile.player();
            arena.clearInventory(player);
            arena.resetPlayer(player);
        }

        startNextStageRunnable();
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
        ((DuelsInMemoryProfileStoreCache) profileStore).flush();

        for (DuelsProfile profile : arena.players()) {
            Player player = profile.player();
            player.kickPlayer(translate("gameOver", profile));
            service.flush(profile);
        }

        arena.players().clear();
        arena.spectators().clear();
        arena.alivePlayers().clear();
        arena.teams().forEach(duelsTeam -> duelsTeam.players().clear());

        HandlerList.unregisterAll(this);
    }

    private String translate(String key, DuelsProfile profile) {
        return translationProvider.forKey(key, profile.locale());
    }
}
