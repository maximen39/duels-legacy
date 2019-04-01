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

import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.IPlayerFileData;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * @author maximen39
 */
public class PlayerFileData implements IPlayerFileData {
    @Override
    public void save(EntityHuman entityHuman) {
    }

    @Override
    public NBTTagCompound load(EntityHuman entityHuman) {
        return null;
    }

    @Override
    public String[] getSeenPlayers() {
        return new String[0];
    }
}
