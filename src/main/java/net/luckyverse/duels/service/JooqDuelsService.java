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
package net.luckyverse.duels.service;

import net.luckyverse.api.privilege.Privilege;
import net.luckyverse.duels.arena.DuelsArena;
import net.luckyverse.duels.arena.DuelsArenaMode;
import net.luckyverse.duels.arena.DuelsArenaType;
import net.luckyverse.duels.data.Account;
import net.luckyverse.duels.data.StatisticsData;
import net.luckyverse.duels.profile.DuelsProfile;
import net.luckyverse.duels.statistics.DuelsStatistics;
import net.luckyverse.jooq.enums.*;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static net.luckyverse.jooq.tables.Accounts.ACCOUNTS;
import static net.luckyverse.jooq.tables.AccountsPrivileges.ACCOUNTS_PRIVILEGES;
import static net.luckyverse.jooq.tables.DuelsGames.DUELS_GAMES;
import static net.luckyverse.jooq.tables.DuelsStatisticsGames.DUELS_STATISTICS_GAMES;
import static net.luckyverse.jooq.tables.Games.GAMES;

/**
 * @author maximen39
 */
public class JooqDuelsService implements DuelsService {

    private final DSLContext context;
    private List<StatisticsData> statisticsData;

    public JooqDuelsService(DSLContext context) {
        this.context = context;
        this.statisticsData = new ArrayList<>();
    }

    @Override
    public void flush(DuelsProfile profile) {
        for (Iterator<StatisticsData> iterator = statisticsData.listIterator(); iterator.hasNext(); ) {
            StatisticsData statisticsData = iterator.next();
            if (statisticsData.profile() == profile) {
                iterator.remove();
                addStatistics(profile, statisticsData.arena(), statisticsData.action(), statisticsData.date());
            }
        }
    }

    @Override
    public long startGame() {
        return context.insertInto(
                GAMES,
                GAMES.TYPE,
                GAMES.START_TIME
        ).values(
                GamesType.duels,
                new Timestamp(new Date().getTime())
        ).returning(GAMES.ID).fetchOne().getId();
    }

    @Override
    public DuelsService endGame(long id) {
        context.update(GAMES)
                .set(GAMES.END_TIME, new Timestamp(new Date().getTime()))
                .where(GAMES.ID.eq(id))
                .executeAsync();
        return this;
    }

    @Override
    public Account getAccount(String name) {
        Record record = context.select(
                ACCOUNTS.ID,
                ACCOUNTS.LOCALE,
                ACCOUNTS.NAME
        ).from(ACCOUNTS).where(ACCOUNTS.NAME.eq(name)).fetchOne();

        if (record != null) {
            return new Account()
                    .id(record.getValue(ACCOUNTS.ID))
                    .name(record.getValue(ACCOUNTS.NAME))
                    .locale(record.getValue(ACCOUNTS.LOCALE));
        } else {
            return null;
        }
    }

    @Override
    public Account createAccount(String name, String locale) {
        long id = context.insertInto(
                ACCOUNTS,
                ACCOUNTS.LOCALE,
                ACCOUNTS.NAME
        ).values(
                locale,
                name).returning(ACCOUNTS.ID).fetchOne().getId();
        return new Account().locale(locale).name(name).id(id);
    }

    @Override
    public Collection<Privilege> getAccountPrivileges(long accountId) {
        Condition condition = ACCOUNTS_PRIVILEGES.ACCOUNT_ID.eq(accountId).and(isNotExpired());

        return context.select(
                ACCOUNTS_PRIVILEGES.ACCOUNT_ID,
                ACCOUNTS_PRIVILEGES.PRIVILAGE
        ).from(ACCOUNTS_PRIVILEGES
        ).where(condition).fetch().stream().map(record ->
                Privilege.valueOf(record.getValue(ACCOUNTS_PRIVILEGES.PRIVILAGE).name().toUpperCase())
        ).collect(Collectors.toSet());
    }

    @Override
    public long getCoins(long accountId) {
        return 0;
    }

    @Override
    public DuelsService updateGameData(DuelsProfile profile, DuelsArena arena) {
        Objects.requireNonNull(arena, "Arena can't be null!");
        Objects.requireNonNull(profile, "Profile can't be null!");

        Condition condition = duelsGamesEq(profile.id(), arena.type(), arena.mode());
        DuelsStatistics statistics = profile.statistics();

        context.insertInto(
                DUELS_GAMES,
                DUELS_GAMES.ACCOUNT_ID,
                DUELS_GAMES.GAME_TYPE,
                DUELS_GAMES.GAME_MODE,
                DUELS_GAMES.COINS,
                DUELS_GAMES.GAMES,
                DUELS_GAMES.WINS,
                DUELS_GAMES.KILLS,
                DUELS_GAMES.DEATHS,
                DUELS_GAMES.MELEE_SWINGS,
                DUELS_GAMES.MELEE_HITS,
                DUELS_GAMES.BOW_SWINGS,
                DUELS_GAMES.BOW_HITS
        ).values(
                profile.id(),
                DuelsGamesGameType.valueOf(arena.type().name().toLowerCase()),
                DuelsGamesGameMode.valueOf(arena.mode().name().toLowerCase()),
                profile.coins(),
                (int) statistics.games(),
                (int) statistics.wins(),
                (int) statistics.kills(),
                (int) statistics.deaths(),
                (int) statistics.meleeSwings(),
                (int) statistics.meleeHits(),
                (int) statistics.bowSwings(),
                (int) statistics.bowHits()
        ).onDuplicateKeyUpdate()
                .set(DUELS_GAMES.COINS, DUELS_GAMES.COINS.add(profile.coins()))
                .set(DUELS_GAMES.GAMES, DUELS_GAMES.GAMES.add(statistics.games()))
                .set(DUELS_GAMES.WINS, DUELS_GAMES.WINS.add(statistics.wins()))
                .set(DUELS_GAMES.KILLS, DUELS_GAMES.KILLS.add(statistics.kills()))
                .set(DUELS_GAMES.DEATHS, DUELS_GAMES.DEATHS.add(statistics.deaths()))
                .set(DUELS_GAMES.MELEE_SWINGS, DUELS_GAMES.MELEE_SWINGS.add(statistics.meleeSwings()))
                .set(DUELS_GAMES.MELEE_HITS, DUELS_GAMES.MELEE_HITS.add(statistics.meleeHits()))
                .set(DUELS_GAMES.BOW_SWINGS, DUELS_GAMES.BOW_SWINGS.add(statistics.bowSwings()))
                .set(DUELS_GAMES.BOW_HITS, DUELS_GAMES.BOW_HITS.add(statistics.bowHits()))
                .where(condition).executeAsync();

        return this;
    }

    @Override
    public DuelsService addKill(DuelsProfile profile, DuelsArena arena) {
        Objects.requireNonNull(arena, "Arena can't be null!");
        Objects.requireNonNull(profile, "Profile can't be null!");

        statisticsData.add(new StatisticsData()
                .profile(profile)
                .arena(arena)
                .action(DuelsStatisticsGamesAction.kill)
                .date(new Date()));
        return this;
    }

    @Override
    public DuelsService addDeath(DuelsProfile profile, DuelsArena arena) {
        Objects.requireNonNull(arena, "Arena can't be null!");
        Objects.requireNonNull(profile, "Profile can't be null!");

        statisticsData.add(new StatisticsData()
                .profile(profile)
                .arena(arena)
                .action(DuelsStatisticsGamesAction.death)
                .date(new Date()));
        return this;
    }

    @Override
    public DuelsService addWin(DuelsProfile profile, DuelsArena arena) {
        Objects.requireNonNull(arena, "Arena can't be null!");
        Objects.requireNonNull(profile, "Profile can't be null!");

        statisticsData.add(new StatisticsData()
                .profile(profile)
                .arena(arena)
                .action(DuelsStatisticsGamesAction.win)
                .date(new Date()));
        return this;
    }

    @Override
    public DuelsService addMeleeSwing(DuelsProfile profile, DuelsArena arena) {
        Objects.requireNonNull(arena, "Arena can't be null!");
        Objects.requireNonNull(profile, "Profile can't be null!");

        statisticsData.add(new StatisticsData()
                .profile(profile)
                .arena(arena)
                .action(DuelsStatisticsGamesAction.melee_swing)
                .date(new Date()));
        return this;
    }

    @Override
    public DuelsService addMeleeHit(DuelsProfile profile, DuelsArena arena) {
        Objects.requireNonNull(arena, "Arena can't be null!");
        Objects.requireNonNull(profile, "Profile can't be null!");

        statisticsData.add(new StatisticsData()
                .profile(profile)
                .arena(arena)
                .action(DuelsStatisticsGamesAction.melee_hit)
                .date(new Date()));
        return this;
    }

    @Override
    public DuelsService addBowSwing(DuelsProfile profile, DuelsArena arena) {
        Objects.requireNonNull(arena, "Arena can't be null!");
        Objects.requireNonNull(profile, "Profile can't be null!");

        statisticsData.add(new StatisticsData()
                .profile(profile)
                .arena(arena)
                .action(DuelsStatisticsGamesAction.bow_swing)
                .date(new Date()));
        return this;
    }

    @Override
    public DuelsService addBowHit(DuelsProfile profile, DuelsArena arena) {
        Objects.requireNonNull(arena, "Arena can't be null!");
        Objects.requireNonNull(profile, "Profile can't be null!");

        statisticsData.add(new StatisticsData()
                .profile(profile)
                .arena(arena)
                .action(DuelsStatisticsGamesAction.bow_hit)
                .date(new Date()));
        return this;
    }

    private void addStatistics(DuelsProfile profile, DuelsArena arena, DuelsStatisticsGamesAction action, Date date) {
        context.insertInto(
                DUELS_STATISTICS_GAMES,
                DUELS_STATISTICS_GAMES.ACCOUNT_ID,
                DUELS_STATISTICS_GAMES.GAME_ID,
                DUELS_STATISTICS_GAMES.GAME_TYPE,
                DUELS_STATISTICS_GAMES.GAME_MODE,
                DUELS_STATISTICS_GAMES.ACTION,
                DUELS_STATISTICS_GAMES.CREATED_AT
        ).values(
                profile.id(), arena.gameId(),
                DuelsStatisticsGamesGameType.valueOf(arena.type().name().toLowerCase()),
                DuelsStatisticsGamesGameMode.valueOf(arena.mode().name().toLowerCase()),
                action, new Timestamp(date.getTime())
        ).executeAsync();
    }

    private Condition duelsGamesEq(long accountId, DuelsArenaType type, DuelsArenaMode mode) {
        return DUELS_GAMES.ACCOUNT_ID.eq(accountId)
                .and(DUELS_GAMES.GAME_TYPE.eq(DuelsGamesGameType.valueOf(
                        type.name().toLowerCase())))
                .and(DUELS_GAMES.GAME_MODE.eq(DuelsGamesGameMode.valueOf(
                        mode.name().toLowerCase())));
    }

    private Condition isNotExpired() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return ACCOUNTS_PRIVILEGES.ACTIVE_UNTIL.gt(timestamp).or(
                ACCOUNTS_PRIVILEGES.ACTIVE_UNTIL.isNull());
    }

}
