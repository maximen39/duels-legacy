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
package net.luckyverse.duels.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.profile.DuelsProfile;
import net.luckyverse.jooq.enums.DuelsStatisticsGamesAction;

import java.util.Date;

/**
 * @author maximen39
 */
@Getter
@Setter
@Accessors(fluent = true)
public class StatisticsData {

    private DuelsStatisticsGamesAction action;
    private DuelsProfile profile;
    private DuelsArena arena;
    private Date date;
}
