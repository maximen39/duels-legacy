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
import net.luckyverse.api.arena.ArenaStage;
import net.luckyverse.api.arena.TeamArena;
import net.luckyverse.api.configuration.Configuration;
import net.luckyverse.api.translate.TranslationProvider;
import net.luckyverse.arena.AbstractArena;
import net.luckyverse.duels.kit.Kit;
import net.luckyverse.duels.kit.KitImpl;
import net.luckyverse.duels.kit.gui.ItemBuilder;
import net.luckyverse.duels.kit.gui.buttons.Button;
import net.luckyverse.duels.kit.gui.types.PaginatedInventory;
import net.luckyverse.duels.profile.DuelsProfile;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author maximen39
 */
public class DuelsArena extends AbstractArena<DuelsProfile, DuelsArena>
        implements TeamArena<DuelsTeam, DuelsProfile, DuelsArena>, DuelsKit {

    @Inject
    private Configuration configuration;
    /**
     * Arena name
     */
    @Expose
    private String name;
    /**
     * Duration of game
     */
    @Expose
    private int gameDuration = 480;
    /**
     * List of teams
     */
    @Expose
    private List<DuelsTeam> teams = Collections.emptyList();
    /**
     * List of kits
     */
    @Expose
    private List<KitImpl> kits = Collections.emptyList();
    /**
     * World name
     */
    @Expose
    private String worldName = "world";
    @Expose
    private DuelsArenaType type = DuelsArenaType._1VS1;
    @Expose
    private DuelsArenaMode mode = DuelsArenaMode.CLASSIC;
    @Expose
    private int borderX = 0;
    @Expose
    private int borderZ = 0;
    @Expose
    private int borderSize = 125;

    private ArenaStage<DuelsArena, DuelsProfile> activeStage;
    private Set<Block> blocksPlaced = new HashSet<>();
    private Set<DuelsProfile> alivePlayers = new HashSet<>();
    private long gameId;

    public ArenaStage<DuelsArena, DuelsProfile> activeStage() {
        return activeStage;
    }

    public void activeStage(ArenaStage<DuelsArena, DuelsProfile> activeStage) {
        this.activeStage = activeStage;
    }

    public Block addBlock(Block block) {
        blocksPlaced.add(block);
        return block;
    }

    public Block removeBlock(Block block) {
        blocksPlaced.remove(block);
        return block;
    }

    public boolean canDestroy(Block block) {
        return blocksPlaced.contains(block);
    }

    public DuelsArena clearBlocks() {
        blocksPlaced.forEach(block -> block.setType(Material.AIR));
        blocksPlaced.clear();
        return this;
    }

    public DuelsArena addAlivePlayer(DuelsProfile duelsProfile) {
        alivePlayers.add(duelsProfile);
        return this;
    }

    public DuelsArena removeAlivePlayer(DuelsProfile duelsProfile) {
        alivePlayers.remove(duelsProfile);
        return this;
    }

    public Set<DuelsProfile> alivePlayers() {
        return alivePlayers;
    }

    public boolean isPlayerAlive(DuelsProfile duelsProfile) {
        return alivePlayers.contains(duelsProfile);
    }

    public boolean isAllies(DuelsProfile profile, DuelsProfile target) {
        return teams.stream().anyMatch(team -> team.hasPlayer(profile) && team.hasPlayer(target));
    }

    public Stream<DuelsTeam> aliveEnemyTeams() {
        return teams.stream().filter(team -> team.players().stream()
                .anyMatch(profile -> isPlayerAlive(profile) && !hasSpectator(profile)));
    }

    public boolean isMax() {
        return limit() <= players().size();
    }

    public DuelsTeam suitableTeam(DuelsProfile profile) {
        Comparator<DuelsTeam> comparator = Comparator.comparingInt(duelsTeam -> duelsTeam.players().size());
        return teams().stream()
                .filter(duelsTeam -> !duelsTeam.isMax())
                .min(comparator).orElse(null);
    }

    @Override
    public int limit() {
        return teams().stream().mapToInt(DuelsTeam::limit).sum();
    }

    public String name() {
        return name;
    }

    @Override
    public DuelsArena addTeam(DuelsTeam team) {
        teams.add(team);
        return this;
    }

    @Override
    public DuelsArena removeTeam(DuelsTeam team) {
        teams.remove(team);
        return this;
    }

    @Override
    public Collection<DuelsTeam> teams() {
        return teams;
    }

    public DuelsTeam team(DuelsProfile duelsProfile) {
        return teams.stream()
                .filter(duelsTeam -> duelsTeam.hasPlayer(duelsProfile))
                .findFirst().orElse(null);
    }

    @Override
    public Collection<KitImpl> kits() {
        return kits;
    }

    @Override
    public DuelsArena addKit(Kit kit) {
        kits.add((KitImpl) kit);
        return this;
    }

    @Override
    public boolean hasKit(Kit kit) {
        return kits.contains((KitImpl) kit);
    }

    @Override
    public DuelsArena removeKit(Kit kit) {
        kits.remove((KitImpl) kit);
        return this;
    }

    @Override
    public Kit defaultKit() {
        return kits().stream().findFirst().orElse(null);
    }

    @Override
    public DuelsArena addPlayer(DuelsProfile profile) {
        return super.addPlayer(profile);
    }

    @Override
    public void addSpectator(DuelsProfile profile) {
        profile.player().setGameMode(GameMode.SPECTATOR);
        clearInventory(profile.player());
        profile.player().setAllowFlight(true);
        profile.player().setFlying(true);

        for (DuelsProfile duelsProfile : players()) {
            if (duelsProfile.player() != profile.player()) {
                if (isPlayerAlive(duelsProfile) && !hasSpectator(duelsProfile)) {
                    duelsProfile.player().hidePlayer(profile.player());
                } else {
                    profile.player().showPlayer(duelsProfile.player());
                }
            }
        }
        super.addSpectator(profile);
    }

    public int gameDuration() {
        return gameDuration;
    }

    public String worldName() {
        return worldName;
    }

    public DuelsArenaType type() {
        return type;
    }

    public DuelsArenaMode mode() {
        return mode;
    }

    public void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }

    public void resetPlayer(Player player) {
        player.setMaxHealth(20D);
        player.setHealth(20D);
        player.setFoodLevel(20);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setLevel(0);
        player.setGameMode(GameMode.SURVIVAL);
        player.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .forEach(player::removePotionEffect);
    }

    public void openKitSelector(DuelsProfile profile, TranslationProvider translationProvider) {
        PaginatedInventory paginatedGUI = new PaginatedInventory(profile, translationProvider);
        for (Kit kit : kits()) {
            Button guiButton = new Button(new ItemBuilder(kit.icon())
                    .name(kit.name())
                    .lore(kit.description())
                    .build());

            guiButton.setListener(event -> {
                event.setCancelled(true);
                profile.selectedKit(kit);
                profile.player().sendMessage(translationProvider.forKey("kitSelectedMessage", profile.locale())
                        .replace("$kitName", kit.name()));
                profile.player().closeInventory();
            });
            paginatedGUI.addButton(guiButton);
        }
        profile.player().openInventory(paginatedGUI.getInventory());
    }

    public void setGameDuration(int gameDuration) {
        this.gameDuration = gameDuration;
    }

    public void addKits(KitImpl kit) {
        kits.add(kit);
    }

    public void removeKits(KitImpl kit) {
        kits.remove(kit);
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public int borderSize() {
        return borderSize;
    }

    public int borderX() {
        return borderX;
    }

    public int borderZ() {
        return borderZ;
    }

    public long gameId() {
        return gameId;
    }

    public void gameId(long gameId) {
        this.gameId = gameId;
    }

    public Optional<DuelsProfile> getProfile(String username) {
        return players().stream().filter(profile -> profile.name().equalsIgnoreCase(username)).findFirst();
    }

    public Optional<DuelsProfile> getProfile(Player player) {
        return getProfile(player.getName());
    }
}
