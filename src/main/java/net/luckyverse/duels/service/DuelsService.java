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
package net.luckyverse.duels.service;

import net.luckyverse.api.privilege.Privilege;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.data.Account;
import net.luckyverse.duels.profile.DuelsProfile;

import java.util.Collection;


/**
 * @author maximen39
 */
public interface DuelsService {

    void flush(DuelsProfile profile);

    long startGame();

    DuelsService endGame(long id);

    Account getAccount(String name);

    Account createAccount(String name, String locale);

    Collection<Privilege> getAccountPrivileges(long accountId);

    long getCoins(long accountId);

    DuelsService updateGameData(DuelsProfile profile, DuelsArena arena);

    DuelsService addKill(DuelsProfile profile, DuelsArena arena);

    DuelsService addDeath(DuelsProfile profile, DuelsArena arena);

    DuelsService addWin(DuelsProfile profile, DuelsArena arena);

    DuelsService addMeleeSwing(DuelsProfile profile, DuelsArena arena);

    DuelsService addMeleeHit(DuelsProfile profile, DuelsArena arena);

    DuelsService addBowSwing(DuelsProfile profile, DuelsArena arena);

    DuelsService addBowHit(DuelsProfile profile, DuelsArena arena);
}
