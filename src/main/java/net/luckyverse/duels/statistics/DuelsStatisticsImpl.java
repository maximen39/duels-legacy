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
package net.luckyverse.duels.statistics;

import net.luckyverse.duels.profile.DuelsProfile;

/**
 * @author maximen39
 */
public class DuelsStatisticsImpl implements DuelsStatistics {

    private long wins;
    private long games;
    private long kills;
    private long deaths;
    private long damageGiven;
    private long meleeSwings;
    private long meleeHits;
    private long bowSwings;
    private long bowHits;

    @Override
    public double damageGiven() {
        return damageGiven;
    }

    @Override
    public DuelsStatistics addGivenDamage(double value) {
        damageGiven += value;
        return this;
    }

    @Override
    public DuelsStatistics addMeleeSwings() {
        meleeSwings++;
        return this;
    }

    @Override
    public long meleeSwings() {
        return meleeSwings;
    }

    @Override
    public DuelsStatistics addMeleeHits() {
        meleeHits++;
        return this;
    }

    @Override
    public long meleeHits() {
        return meleeHits;
    }

    @Override
    public DuelsStatistics addBowSwings() {
        bowSwings++;
        return this;
    }

    @Override
    public long bowSwings() {
        return bowSwings;
    }

    @Override
    public DuelsStatistics addBowHits() {
        bowHits++;
        return this;
    }

    @Override
    public long bowHits() {
        return bowHits;
    }

    @Override
    public long wins() {
        return wins;
    }

    @Override
    public DuelsStatistics addWin() {
        wins++;
        return this;
    }

    @Override
    public long games() {
        return games;
    }

    @Override
    public DuelsStatistics addGame() {
        games++;
        return this;
    }

    @Override
    public double winableCoefficient() {
        return 0;
    }

    @Override
    public long gameableMaxWinStreak() {
        return 0;
    }

    @Override
    public long gameableMaxLoseStreak() {
        return 0;
    }

    @Override
    public long quickestWin() {
        return 0;
    }

    @Override
    public long kills() {
        return kills;
    }

    @Override
    public DuelsStatistics addKill(DuelsProfile victim) {
        kills++;
        return this;
    }

    @Override
    public long deaths() {
        return deaths;
    }

    @Override
    public DuelsStatistics addDeath(String reason) {
        deaths++;
        return this;
    }

    @Override
    public double killableCoefficient() {
        return 0;
    }

    @Override
    public long killableMaxStreak() {
        return 0;
    }

    @Override
    public long quickestKill() {
        return 0;
    }
}
