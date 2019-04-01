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
import net.luckyverse.api.configuration.Configuration;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.arena.listeners.CommonStageEventListener;
import net.luckyverse.duels.profile.DuelsProfile;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author maximen39
 */
public class PrepareState extends AbstractState<DuelsArena, DuelsProfile> {

    @Inject
    private Plugin plugin;
    @Inject
    private Configuration configuration;

    @Override
    public void startStage(DuelsArena arena) {
        super.startStage(arena);
        arena.activeStage(this);
        Bukkit.getPluginManager().registerEvents(new CommonStageEventListener(arena, configuration), plugin);
        createAndLoadWorld(arena.worldName());

        nextStage().startStage(arena);
    }

    private void createAndLoadWorld(String name) {
        Bukkit.createWorld(WorldCreator.name(name));
        World world = Bukkit.getWorld(name);
        world.getWorldBorder().setCenter(arena.borderX(), arena.borderZ());
        world.getWorldBorder().setSize(arena.borderSize() * 2);
        world.setAutoSave(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                world.setStorm(false);
                world.setTime(1200L);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void endStage() {
    }
}
