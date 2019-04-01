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

import java.util.HashMap;

/**
 * @author maximen39
 */
public class CaseInsensitiveMap<V> extends HashMap<String, V> {

    @Override
    public V put(String key, V value) {
        return super.put(key.toLowerCase(), value);
    }

    @Override
    public V get(Object key) {
        return key instanceof String ? super.get(key.toString().toLowerCase()) : super.get(key);
    }

    @Override
    public V remove(Object key) {
        return key instanceof String ? super.remove(key.toString().toLowerCase()) : super.remove(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String ? super.containsKey(key.toString().toLowerCase()) : super.containsKey(key);
    }
}
