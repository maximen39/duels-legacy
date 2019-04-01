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

import net.luckyverse.api.configuration.Configuration;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.configuration.DuelsConfiguration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author maximen39
 */
public class CommonStageEventListener implements Listener {

    private DuelsArena arena;
    private DuelsConfiguration configuration;

    public CommonStageEventListener(DuelsArena arena, Configuration configuration) {
        this.arena = arena;
        this.configuration = (DuelsConfiguration) configuration;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        int back = 4;
        Player player = event.getPlayer();
        Location location = player.getLocation();
        int xLoc = location.getBlockX();
        int zLoc = location.getBlockZ();
        int borderSize = arena.borderSize();
        int maxX = arena.borderX() + borderSize;
        int minX = -(arena.borderX() + borderSize);
        int maxZ = arena.borderZ() + borderSize;
        int minZ = -(arena.borderZ() + borderSize);
        if (Math.abs(xLoc) > maxX || Math.abs(zLoc) > maxZ) {
            if (xLoc <= minX) {
                xLoc = minX + back;
            } else if (xLoc >= maxX) {
                xLoc = maxX - back;
            }

            if (zLoc <= minZ) {
                zLoc = minZ + back;
            } else if (zLoc >= maxZ) {
                zLoc = maxZ - back;
            }
            Location to = new Location(
                    location.getWorld(), xLoc,
                    location.getY(), zLoc,
                    location.getYaw(),
                    location.getPitch());
            player.teleport(to);
        }
    }
}