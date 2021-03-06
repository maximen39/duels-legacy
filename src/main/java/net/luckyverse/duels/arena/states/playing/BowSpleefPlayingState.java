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
package net.luckyverse.duels.arena.states.playing;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author maximen39
 */
public class BowSpleefPlayingState extends BowPlayingState {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        arena.teams().stream().findFirst().ifPresent(duelsTeam -> {
            double deathY = duelsTeam.spawn().getY() - 1;
            if (event.getPlayer().getLocation().getY() <= deathY) {
                arena.getProfile(event.getPlayer()).ifPresent(profile -> {
                    if (arena.isPlayerAlive(profile)) {
                        Entity killer = lastDamage(profile);
                        killPlayer(!canKill(killer, profile) ? null : killer, profile, "kill");
                    }
                });
            }
        });
    }
}
