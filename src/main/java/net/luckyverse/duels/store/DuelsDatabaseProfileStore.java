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
package net.luckyverse.duels.store;

import net.luckyverse.api.privilege.Privilege;
import net.luckyverse.api.store.Store;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.data.Account;
import net.luckyverse.duels.profile.DuelsProfile;
import net.luckyverse.duels.service.DuelsService;
import net.luckyverse.duels.statistics.DuelsStatisticsImpl;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author maximen39
 */
public class DuelsDatabaseProfileStore implements Store<String, Optional<DuelsProfile>> {

    private final DuelsService duelsService;
    private final DuelsArena arena;

    public DuelsDatabaseProfileStore(DuelsService duelsService, DuelsArena arena) {
        this.duelsService = duelsService;
        this.arena = arena;
    }

    @Override
    public Optional<DuelsProfile> fetch(String key) {
        Account account = duelsService.getAccount(key);
        if (account != null) {
            long coins = duelsService.getCoins(account.id());
            Collection<Privilege> privileges = duelsService.getAccountPrivileges(account.id());
            return createProfile(account.id(), account.name(), account.locale(), privileges, coins);
        } else {
            return createProfile(0L, key, getPlayerLocale(key),
                    Collections.singletonList(Privilege.PLAYER), 0);
        }
    }

    @Override
    public Optional<DuelsProfile> store(String key, Optional<DuelsProfile> value) {
        value.ifPresent(profile -> {
            Account account = duelsService.getAccount(key);
            if (account == null) {
                account = duelsService.createAccount(key, getPlayerLocale(key));
            }
            if (account != null) {
                duelsService.updateGameData(profile, arena);
            }
        });
        return value;
    }

    @Override
    public Optional<DuelsProfile> remove(String key) {
        return Optional.empty();
    }

    private String getPlayerLocale(String name) {
        //Player player = Bukkit.getPlayer(name);
        String locale = "ru";
            /*if (player != null) {
                locale = ReflectionUtil.getString(((CraftPlayer) player).getHandle(), "locale");
            }*/
        return locale;
    }

    private Optional<DuelsProfile> createProfile(Long id, String name, String locale, Collection<Privilege> privileges,
                                                 long coins) {
        return Optional.ofNullable(Bukkit.getPlayer(name))
                .map(p -> (DuelsProfile) new DuelsProfile(p, locale, privileges,
                        new DuelsStatisticsImpl(), duelsService, coins).id(id));
    }
}
