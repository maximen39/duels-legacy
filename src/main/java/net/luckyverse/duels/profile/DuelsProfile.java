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
package net.luckyverse.duels.profile;

import net.luckyverse.api.privilege.Privilege;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.kit.Kit;
import net.luckyverse.duels.service.DuelsService;
import net.luckyverse.duels.statistics.DuelsStatistics;
import net.luckyverse.profile.BaseProfile;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * @author maximen39
 */
public class DuelsProfile extends BaseProfile {

    private final DuelsStatistics duelsStatistics;
    private final DuelsService service;
    private Kit selectedKit;
    private long coins;

    public DuelsProfile(Player player, String locale, Collection<Privilege> privileges,
                        DuelsStatistics duelsStatistics, DuelsService service, long coins) {
        super(player, locale, privileges);
        this.duelsStatistics = duelsStatistics;
        this.service = service;
        this.coins = coins;
    }

    public DuelsStatistics statistics() {
        return duelsStatistics;
    }

    public Kit selectedKit() {
        return selectedKit;
    }

    public void selectedKit(Kit selectedKit) {
        this.selectedKit = selectedKit;
    }

    public long coins() {
        return this.coins;
    }

    public DuelsProfile addCoins(long coins) {
        this.coins += coins;
        return this;
    }

    public DuelsProfile addGivenDamage(double damage) {
        statistics().addGivenDamage(damage);
        return this;
    }

    public DuelsProfile addMeleeSwings(DuelsArena arena) {
        statistics().addMeleeSwings();
        service.addMeleeSwing(this, arena);
        return this;
    }

    public DuelsProfile addMeleeHits(DuelsArena arena) {
        statistics().addMeleeHits();
        service.addMeleeHit(this, arena);
        return this;
    }

    public DuelsProfile addBowSwings(DuelsArena arena) {
        statistics().addBowSwings();
        service.addBowSwing(this, arena);
        return this;
    }

    public DuelsProfile addBowHits(DuelsArena arena) {
        statistics().addBowHits();
        service.addBowHit(this, arena);
        return this;
    }

    public DuelsProfile addWin(DuelsArena arena) {
        statistics().addWin();
        service.addWin(this, arena);
        return this;
    }

    public DuelsProfile addGame() {
        statistics().addGame();
        return this;
    }

    public DuelsProfile addKill(DuelsProfile victim, DuelsArena arena) {
        statistics().addKill(victim);
        service.addKill(this, arena);
        return this;
    }

    public DuelsProfile addDeath(String reason, DuelsArena arena) {
        statistics().addDeath(reason);
        service.addDeath(this, arena);
        return this;
    }
}
