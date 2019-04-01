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

/**
 * @author maximen39
 */
public enum DuelsArenaMode {

    BOW("bow"),

    CLASSIC("classic"),

    OP("op"),

    UHC("uhc"),

    NO_DEBUFF("no-debuff"),

    MEGAWALLS("megawalls"),

    BLITZ("blitz"),

    SKYWARS("skywars"),

    COMBO("combo"),

    BOW_SPLEEF("bow-spleef"),

    SUMO("sumo");


    private String name;

    DuelsArenaMode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
