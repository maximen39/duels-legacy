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
package net.luckyverse.duels.arena.listeners;

import net.luckyverse.duels.DuelsArenaFactory;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.arena.states.WaitingState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;

/**
 * @author maximen39
 */
public class CommonArenaEventListener implements Listener {

    private DuelsArenaFactory arenaFactory;

    public CommonArenaEventListener(DuelsArenaFactory arenaFactory) {
        this.arenaFactory = arenaFactory;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (arenaFactory.findFreeArena() == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        DuelsArena freeArena = arenaFactory.findFreeArena();
        if (freeArena != null) {
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                if (onlinePlayer != player) {
                    player.hidePlayer(onlinePlayer);
                    onlinePlayer.hidePlayer(player);
                }
            });
            ((WaitingState) freeArena.activeStage()).join(event);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
}
