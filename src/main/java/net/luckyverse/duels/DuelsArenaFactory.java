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

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import net.luckyverse.api.configuration.Configuration;
import net.luckyverse.api.injection.WrappedInjector;
import net.luckyverse.api.io.PathFinder;
import net.luckyverse.api.store.Store;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.arena.states.*;
import net.luckyverse.duels.arena.states.playing.*;
import net.luckyverse.duels.configuration.DuelsConfiguration;
import net.luckyverse.duels.profile.DuelsProfile;
import net.luckyverse.duels.service.DuelsService;
import net.luckyverse.duels.store.CaseInsensitiveMap;
import net.luckyverse.duels.store.DuelsDatabaseProfileStore;
import net.luckyverse.duels.store.DuelsInMemoryProfileStoreCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author maximen39
 */
public class DuelsArenaFactory {

    public static final String ARENAS_FOLDER = "arenas/";
    private static final Logger LOGGER = LoggerFactory.getLogger(DuelsArenaFactory.class);

    @Inject
    @Named("advanced")
    private Gson gson;
    @Inject
    private PathFinder finder;
    @Inject
    private DuelsService service;
    @Inject
    private Configuration configuration;

    private WrappedInjector injector;
    private List<DuelsArena> arenas = new ArrayList<>();

    void initInjector(WrappedInjector injector) {
        if (this.injector != null) {
            throw new IllegalStateException("Injector already inited");
        }
        this.injector = injector;
    }

    public DuelsArena create(String name) {
        DuelsArena arena = null;

        try {
            Path path = finder.find(ARENAS_FOLDER + name + ".json");
            String json = String.join("", Files.readAllLines(path, Charset.forName("UTF-8")));
            arena = gson.fromJson(json, DuelsArena.class);

            PrepareState prepareState = new PrepareState();
            WaitingState waitingState = new WaitingState();
            StartingState startingState = new StartingState();
            FinishState finishState = new FinishState();
            PlayingState playingState;
            switch (arena.mode()) {
                case BOW:
                    playingState = new BowPlayingState();
                    break;
                case BOW_SPLEEF:
                    playingState = new BowSpleefPlayingState();
                    break;
                case COMBO:
                    playingState = new ComboPlayingState();
                    break;
                case MEGAWALLS:
                    playingState = new MegawallsPlayingState();
                    break;
                case SKYWARS:
                    playingState = new SkywarsPlayingState();
                    break;
                case SUMO:
                    playingState = new SumoPlayingState();
                    break;
                default:
                    playingState = new ClassicPlayingState();
                    break;
            }

            prepareState.setup(null, waitingState);
            waitingState.setup(null, startingState);
            startingState.setup(waitingState, playingState);
            playingState.setup(null, finishState);
            finishState.setup(null, waitingState);

            Injector injector = createInjector(arena);
            injector.injectMembers(prepareState);
            injector.injectMembers(waitingState);
            injector.injectMembers(startingState);
            injector.injectMembers(playingState);
            injector.injectMembers(finishState);
            injector.injectMembers(arena);

            prepareState.startStage(arena);
            this.arenas.add(arena);
            LOGGER.info(String.format("Arena '%s' successfully started", name));
        } catch (IOException ex) {
            LOGGER.error("Error while loading arena", ex);
        }
        return arena;
    }

    /**
     * @param profile profile
     *                <p>
     *                Find arena when playing profile
     * @return DuelsArena
     */
    public DuelsArena findArena(DuelsProfile profile) {
        for (DuelsArena arena : arenas) {
            if (arena.containsPlayer(profile)) {
                return arena;
            }
        }
        return null;
    }

    /**
     * @param username username
     *                 <p>
     *                 Find arena when playing
     * @return DuelsArena
     */
    public DuelsArena findArena(String username) {
        for (DuelsArena arena : arenas) {
            for (DuelsProfile profile : arena.players()) {
                if (profile.name().equalsIgnoreCase(username)) {
                    return arena;
                }
            }
        }
        return null;
    }

    /**
     * @return free arena
     */
    public DuelsArena findFreeArena() {
        for (DuelsArena arena : arenas) {
            if (isArenaWaiting(arena) && isArenaFree(arena)) {
                return arena;
            }
        }
        return null;
    }

    /**
     * @return true if arena in process waiting
     */
    private boolean isArenaWaiting(DuelsArena arena) {
        return arena.activeStage() instanceof WaitingState;
    }

    /**
     * @return true if arena is free
     */
    private boolean isArenaFree(DuelsArena arena) {
        return !arena.isMax();
    }

    private Injector createInjector(DuelsArena arena) {
        Store<String, Optional<DuelsProfile>> profileStore = initStore(arena);
        return this.injector.injector().createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(new TypeLiteral<Store<String, Optional<DuelsProfile>>>() {
                }).annotatedWith(Names.named("profile")).toInstance(profileStore);
            }
        });
    }

    private Store<String, Optional<DuelsProfile>> initStore(DuelsArena arena) {
        return new DuelsInMemoryProfileStoreCache(
                new CaseInsensitiveMap<>(), new DuelsDatabaseProfileStore(service, arena));
    }

    void loadArenas() {
        for (String includedArena : ((DuelsConfiguration) configuration).includedArenas) {
            create(includedArena);
        }
    }
}
