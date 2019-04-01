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
package net.luckyverse.duels.arena;

import com.google.gson.annotations.Expose;
import net.luckyverse.arena.AbstractTeam;
import net.luckyverse.duels.profile.DuelsProfile;
import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 * @author maximen39
 */
public class DuelsTeam extends AbstractTeam<DuelsTeam, DuelsProfile> {

    /**
     * Limit of players on team
     */
    @Expose
    private int limit;
    /**
     * Location of spawn
     */
    @Expose
    private Location spawn;
    /**
     * color of team
     */
    @Expose
    private ChatColor color = ChatColor.WHITE;

    public boolean isMax() {
        return limit() <= players().size();
    }

    public Location spawn() {
        return spawn;
    }

    public ChatColor getColor() {
        return color;
    }

    @Override
    public int limit() {
        return limit;
    }

    public DuelsTeam setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public DuelsTeam setSpawn(Location spawn) {
        this.spawn = spawn;
        return this;
    }

    public String name() {
        StringBuilder builder = new StringBuilder();
        String color = getColor().name();
        for (String s : color.split("_")) {
            builder.append(s.charAt(0));
        }
        return builder.toString();
    }
}
