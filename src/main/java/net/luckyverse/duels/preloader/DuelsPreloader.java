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
package net.luckyverse.duels.preloader;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import net.luckyverse.api.configuration.Configuration;
import net.luckyverse.api.io.PathFinder;
import net.luckyverse.api.translate.TranslationProvider;
import net.luckyverse.duels.configuration.DuelsConfiguration;
import net.luckyverse.duels.configuration.DuelsTranslation;
import net.luckyverse.duels.service.DuelsService;
import net.luckyverse.duels.service.JooqDuelsService;
import net.luckyverse.injection.AbstractInjectPreloader;
import net.luckyverse.io.Utils;
import net.luckyverse.translate.DefaultTranslationProvider;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author maximen39
 */
public class DuelsPreloader extends AbstractInjectPreloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DuelsPreloader.class);

    @Inject
    @Named("advanced")
    private Gson gson;
    @Inject
    private PathFinder finder;
    @Inject
    private Plugin plugin;

    private DuelsService duelsService;
    private Configuration configuration;
    private TranslationProvider translationProvider;

    @Override
    public void preload() {
        configuration = initConfiguration();
        duelsService = initDuelsService();
        translationProvider = initTranslationProvider();
    }

    private Connection getConnection(DuelsConfiguration config) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(config.url, config.user, config.password);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return connection;
    }

    private DuelsService initDuelsService() {
        Settings settings = new Settings();
        Connection connection = getConnection((DuelsConfiguration) configuration);
        settings.withExecuteLogging(((DuelsConfiguration) configuration).jooqDebug);
        DSLContext context = DSL.using(
                connection, SQLDialect.MYSQL, settings);
        new BukkitRunnable() {
            @Override
            public void run() {
                context.execute("select 1;");
            }
        }.runTaskTimer(plugin, 0L, 1200);
        return new JooqDuelsService(context);
    }

    private TranslationProvider initTranslationProvider() {
        Map<String, Configuration> translations = loadTranslations(
                finder, DuelsTranslation.class,
                DuelsTranslation.FILE_NAME, fetchAvailableLocales(), gson);
        return new DefaultTranslationProvider(translations);
    }

    private Configuration initConfiguration() {
        String fileName = "config/" + DuelsConfiguration.FILE_NAME;
        Path pathToConfig = Paths.get(fileName);
        Configuration configuration = Utils.read(pathToConfig, DuelsConfiguration.class, gson);
        if (configuration == null) {
            LOGGER.error(String.format("Configuration '%s' not found ", fileName));
        }
        return configuration;
    }

    @Override
    public AbstractModule module() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Configuration.class).toInstance(configuration);
                bind(DuelsService.class).toInstance(duelsService);
                bind(TranslationProvider.class).toInstance(translationProvider);
            }
        };
    }
}
