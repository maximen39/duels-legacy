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

import net.luckyverse.api.statistics.GameableStatistics;
import net.luckyverse.api.statistics.KillableStatistics;
import net.luckyverse.duels.profile.DuelsProfile;

/**
 * @author maximen39
 */
public interface DuelsStatistics extends KillableStatistics<DuelsProfile, String>, GameableStatistics {

    /**
     * Damage to enemies
     *
     * @return Damage given
     */
    double damageGiven();

    /**
     * Add the damage dealt to the enemy
     *
     * @param value damage
     * @return current statistic
     */
    DuelsStatistics addGivenDamage(double value);

    DuelsStatistics addMeleeSwings();

    long meleeSwings();

    DuelsStatistics addMeleeHits();

    long meleeHits();

    DuelsStatistics addBowSwings();

    long bowSwings();

    DuelsStatistics addBowHits();

    long bowHits();
}
