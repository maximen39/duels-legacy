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

import net.luckyverse.duels.arena.states.PlayingState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 * @author maximen39
 */
public class BowPlayingState extends PlayingState {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!arena.getProfile(getDamagerIfArrow(event.getEntity()).getName()).isPresent() &&
                !arena.getProfile(getDamagerIfArrow(event.getDamager()).getName()).isPresent()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!arena.getProfile(getDamagerIfArrow(event.getEntity()).getName()).isPresent()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!arena.getProfile(event.getEntity().getName()).isPresent()) {
            return;
        }
        event.setCancelled(true);
        event.setFoodLevel(20);
    }
}
