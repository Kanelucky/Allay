package org.allaymc.api.player;

import org.allaymc.api.message.MayContainTrKey;
import org.allaymc.api.message.TrKeys;

import java.util.EnumSet;

/**
 * GameMode represents a game mode that may be assigned to a player. Upon joining the world, players will be
 * given the default game mode that the world holds. Game modes specify the way that a player interacts with
 * and plays in the world.
 *
 * @author daoge_cmd, Kanelucky
 */
public class GameMode {

    /**
     * SURVIVAL is the survival game mode: Players with this game mode have limited supplies and can break
     * blocks after taking some time.
     */
    public static final GameMode SURVIVAL = GameMode
            .builder()
            .id(0)
            .name("survival")
            .translationKey(TrKeys.MC_GAMEMODE_SURVIVAL)
            .abilities(EnumSet.noneOf(PlayerAbility.class))
            .build();

    /**
     * CREATIVE represents the creative game mode: Players with this game mode have infinite blocks and
     * items and can break blocks instantly. Players with creative mode can also fly.
     */
    public static final GameMode CREATIVE = GameMode
            .builder()
            .id(1)
            .name("creative")
            .translationKey(TrKeys.MC_GAMEMODE_CREATIVE)
            .abilities(EnumSet.of(PlayerAbility.MAY_FLY, PlayerAbility.INFINITE_BLOCK))
            .build();

    /**
     * ADVENTURE represents the adventure game mode: Players with this game mode cannot edit the world
     * (placing or breaking blocks).
     */
    public static final GameMode ADVENTURE = GameMode
            .builder()
            .id(2)
            .name("adventure")
            .translationKey(TrKeys.MC_GAMEMODE_ADVENTURE)
            .abilities(EnumSet.noneOf(PlayerAbility.class))
            .build();

    /**
     * SPECTATOR represents the spectator game mode: Players with this game mode cannot interact with the
     * world and cannot be seen by other players. spectator players can fly, like creative mode, and can
     * move through blocks.
     */
    public static final GameMode SPECTATOR = GameMode
            .builder()
            .id(3)
            .name("spectator")
            .translationKey(TrKeys.MC_GAMEMODE_SPECTATOR)
            .abilities(EnumSet.of(PlayerAbility.MAY_FLY, PlayerAbility.FLYING, PlayerAbility.NO_CLIP))
            .build();

    private final int id;

    /**
     * The name for the game mode.
     */
    private final String name;

    /**
     * The translation key for the game mode.
     */
    private final @MayContainTrKey String translationKey;

    /**
     * The default abilities associated with this game mode that should be tracked server-side.
     */
    private final EnumSet<PlayerAbility> abilities;

    private static final GameMode[] VALUES = {SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR};

    /**
     * The vanilla game mode used when communicating with the client.
     */
    private final GameMode vanillaGameMode;

    private GameMode(int id,String name, @MayContainTrKey String translationKey, EnumSet<PlayerAbility> abilities, GameMode vanillaGameMode) {
        this.id = id;
        this.name = name;
        this.translationKey = translationKey;
        this.abilities = abilities.clone();
        this.vanillaGameMode = vanillaGameMode == null ? this : vanillaGameMode;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public EnumSet<PlayerAbility> getAbilities() {
        return abilities.clone();
    }

    /**
     * Gets the vanilla game mode that should be sent to the client.
     */
    public GameMode getVanillaGameMode() {
        return vanillaGameMode;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Looks up a game mode by the id passed.
     *
     * @param id the id to look up
     * @return the game mode, or {@code null} if failed to find a game mode
     */
    public static GameMode from(int id) {
        if (id < 0 || id >= VALUES.length) {
            return null;
        }
        return VALUES[id];
    }

    public static class Builder {

        private int id;
        private String name;
        private @MayContainTrKey String translationKey;
        private final EnumSet<PlayerAbility> abilities = EnumSet.noneOf(PlayerAbility.class);
        private GameMode vanillaGameMode;

        private Builder() {
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder translationKey(@MayContainTrKey String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public Builder abilities(EnumSet<PlayerAbility> abilities) {
            this.abilities.addAll(abilities);
            return this;
        }

        /**
         * Sets the vanilla game mode that this custom game mode
         * should appear as to the Minecraft client.
         */
        public Builder vanillaGameMode(GameMode vanillaGameMode) {
            this.vanillaGameMode = vanillaGameMode;
            return this;
        }

        public GameMode build() {
            return new GameMode(id,name, translationKey, EnumSet.copyOf(abilities), vanillaGameMode);
        }
    }
}