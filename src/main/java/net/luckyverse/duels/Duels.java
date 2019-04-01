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
package net.luckyverse.duels;

import com.google.inject.Inject;
import net.luckyverse.api.injection.WrappedInjector;
import net.luckyverse.api.plugable.AbstractModule;
import net.luckyverse.duels.arena.PlayerFileData;
import net.luckyverse.duels.arena.listeners.CommonArenaEventListener;
import net.luckyverse.duels.command.CommandsManager;
import net.luckyverse.duels.command.commands.DuelsCommand;
import net.luckyverse.duels.kit.gui.buttons.InventoryListener;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author maximen39
 */
public class Duels extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(Duels.class);

    @Inject
    private Plugin plugin;
    @Inject
    private WrappedInjector injector;

    @Override
    public void onEnable() {
        MinecraftServer.getServer().getPlayerList().playerFileData = new PlayerFileData();
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), plugin);

        DuelsArenaFactory arenaFactory = injector.injector().getInstance(DuelsArenaFactory.class);
        arenaFactory.initInjector(injector);
        arenaFactory.loadArenas();
        Bukkit.getPluginManager().registerEvents(new CommonArenaEventListener(arenaFactory), plugin);

        CommandsManager commandsManager = injector.injector().getInstance(CommandsManager.class);
        DuelsCommand duelsCommand = new DuelsCommand(arenaFactory);
        injector.injector().injectMembers(duelsCommand);
        commandsManager.register(getCommandMap(), duelsCommand);

        LOGGER.info("Duels successfully started");
    }

    private CommandMap getCommandMap() {
        return ((CraftServer) Bukkit.getServer()).getCommandMap();
    }

    @Override
    public String getName() {
        return "Duels";
    }

    @Override
    public void onDisable() {
    }
}
