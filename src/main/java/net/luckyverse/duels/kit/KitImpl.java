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
package net.luckyverse.duels.kit;

import com.google.gson.annotations.Expose;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author maximen39
 */
@Getter
@Setter
@Accessors(fluent = true)
@EqualsAndHashCode
public class KitImpl implements Kit {

    @Expose
    private String name;
    @Expose
    private List<String> description;
    @Expose
    private Material icon;
    @Expose
    private List<ItemStack> hotBarItems;
    @Expose
    private List<ItemStack> inventoryItems;
    @Expose
    private ItemStack helmet;
    @Expose
    private ItemStack chestplate;
    @Expose
    private ItemStack leggings;
    @Expose
    private ItemStack boots;
}
