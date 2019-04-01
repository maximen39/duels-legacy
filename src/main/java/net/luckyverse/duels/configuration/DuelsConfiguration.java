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
package net.luckyverse.duels.configuration;

import com.google.gson.annotations.Expose;
import net.luckyverse.api.configuration.Configuration;

import java.util.Collections;
import java.util.Set;

/**
 * @author maximen39
 */
public class DuelsConfiguration implements Configuration {

    public static final String FILE_NAME = "configuration.json";

    @Expose
    public String url = "localhost";
    @Expose
    public String user = "root";
    @Expose
    public String password = "password";
    @Expose
    public boolean jooqDebug = false;

    @Expose
    public Set<String> includedArenas = Collections.emptySet();
    @Expose
    public long coinsForKill = 10;
    @Expose
    public long coinsForWin = 10;
    @Expose
    public boolean enableChatFormat = true;
    @Expose
    public String chatFormat = "$teamcolor[$team]§7 $name&7: $chatcolor$message";
    @Expose
    public String spectatorChatFormat = "&8[S] $name&7: $message";

    @Override
    public String fileName() {
        return FILE_NAME;
    }
}
