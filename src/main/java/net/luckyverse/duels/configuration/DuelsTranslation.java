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

/**
 * @author maximen39
 */
public class DuelsTranslation implements Configuration {

    public static final String FILE_NAME = "translation";

    @Expose
    public String fullServer = "На арене уже максимально игроков!";
    @Expose
    public String joinMsg = "$player присоединился к игре ($online/$limit)";
    @Expose
    public String quitMsg = "$player покинул игру ($online/$limit)";
    @Expose
    public String scoreboardTitle = "Duels";
    @Expose
    public String scoreboardLinePlayers = "Игроков $online/$limit";
    @Expose
    public String scoreboardLineWait = "Ожидание...";
    @Expose
    public String scoreboardLineArenaName = "Карта: $arenaName";
    @Expose
    public String scoreboardLineStartIn = "Начало через $seconds";
    @Expose
    public String scoreboardLineTimeLeft = "Осталось: $time";
    @Expose
    public String scoreboardLineKills = "Убийств: $kills";
    @Expose
    public String scoreboardLineMode = "Режим: $mode";
    @Expose
    public String scoreboardLineBestStreak = "bestStreak: $bestStreak";
    @Expose
    public String gameStartIn = "Начало через $seconds";
    @Expose
    public String domain = "luckyverse.net";
    @Expose
    public String netherStarSelectKit = "Выбор набора";
    @Expose
    public String kitSelectedMessage = "Набор $kitName успешно выбран";
    @Expose
    public String inventoryKitName = "Выбор набора";
    @Expose
    public String inventoryKitPreviousPage = "Назад";
    @Expose
    public String inventoryKitCurrentPage = "$currentPage/$maxPages";
    @Expose
    public String inventoryKitNextPage = "Вперед";
    @Expose
    public String damageGiven = "Нанесено урона: $damage❤";
    @Expose
    public String deathMessage_leave = "$victim покинул дуэль";
    @Expose
    public String deathMessageWithKiller_leave = "$victim испугался $killer и покинул дуэль";
    @Expose
    public String gameOver = "Игра окончена";
    @Expose
    public String gameOverMsg = "Duel - winners: $winner - Убийств: $kills";
    @Expose
    public String gameOverMsgDraw = "Duel - DRAW - Потрачено жизней: $damageGiven❤";
    @Expose
    public String errorCommandSyntax = "Команда ненайдена!";
    @Expose
    public String noPermission = "У вас нет прав на выполнение этой команды!";
    @Expose
    public String errExecuteCommand = "Ошибка при выполнение команды!";
    @Expose
    public String commandsList = "Команды: \n/duels arena - Настрока арены";
    @Expose
    public String arenaCommandsList = "Команды: " +
            "\n/duels arena addTeam [limit] - Добавить команду" +
            "\n/duels arena addKit [name] [iconMaterial] [description...] - Добавить кит";
    @Expose
    public String teamAdded = "Команда успешно добавлена";
    @Expose
    public String kitAdded = "Набор успешно добавлен";
    @Expose
    public String deathMessage_contact = "";
    @Expose
    public String deathMessage_entity_attack = "";
    @Expose
    public String deathMessage_projectile = "";
    @Expose
    public String deathMessage_suffocation = "";
    @Expose
    public String deathMessage_fall = "";
    @Expose
    public String deathMessage_fire = "";
    @Expose
    public String deathMessage_fire_tick = "";
    @Expose
    public String deathMessage_melting = "";
    @Expose
    public String deathMessage_lava = "";
    @Expose
    public String deathMessage_drowning = "";
    @Expose
    public String deathMessage_block_explosion = "";
    @Expose
    public String deathMessage_entity_explosion = "";
    @Expose
    public String deathMessage_void = "";
    @Expose
    public String deathMessage_lightning = "";
    @Expose
    public String deathMessage_suicide = "";
    @Expose
    public String deathMessage_starvation = "";
    @Expose
    public String deathMessage_poison = "";
    @Expose
    public String deathMessage_magic = "";
    @Expose
    public String deathMessage_wither = "";
    @Expose
    public String deathMessage_falling_block = "";
    @Expose
    public String deathMessage_thorns = "";
    @Expose
    public String deathMessage_custom = "";
    @Expose
    public String deathMessageWithKiller_contact = "";
    @Expose
    public String deathMessageWithKiller_entity_attack = "";
    @Expose
    public String deathMessageWithKiller_projectile = "";
    @Expose
    public String deathMessageWithKiller_suffocation = "";
    @Expose
    public String deathMessageWithKiller_fall = "";
    @Expose
    public String deathMessageWithKiller_fire = "";
    @Expose
    public String deathMessageWithKiller_fire_tick = "";
    @Expose
    public String deathMessageWithKiller_melting = "";
    @Expose
    public String deathMessageWithKiller_lava = "";
    @Expose
    public String deathMessageWithKiller_drowning = "";
    @Expose
    public String deathMessageWithKiller_block_explosion = "";
    @Expose
    public String deathMessageWithKiller_entity_explosion = "";
    @Expose
    public String deathMessageWithKiller_void = "";
    @Expose
    public String deathMessageWithKiller_lightning = "";
    @Expose
    public String deathMessageWithKiller_suicide = "";
    @Expose
    public String deathMessageWithKiller_starvation = "";
    @Expose
    public String deathMessageWithKiller_poison = "";
    @Expose
    public String deathMessageWithKiller_magic = "";
    @Expose
    public String deathMessageWithKiller_wither = "";
    @Expose
    public String deathMessageWithKiller_falling_block = "";
    @Expose
    public String deathMessageWithKiller_thorns = "";
    @Expose
    public String deathMessageWithKiller_custom = "";

    @Override
    public String fileName() {
        return FILE_NAME;
    }
}
