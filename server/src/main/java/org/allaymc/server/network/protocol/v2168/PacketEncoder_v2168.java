package org.allaymc.server.network.protocol.v2168;

import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.player.Player;
import org.allaymc.server.network.protocol.ProtocolData;
import org.allaymc.server.network.protocol.v1001.PacketEncoder_v1001;
import org.cloudburstmc.protocol.bedrock.data.inventory.crafting.recipe.*;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.CraftingDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket;

import java.util.Collection;

/**
 * Packet encoder for protocol v2168.
 *
 * <p>The wire-format changes for this version are handled by the Bedrock codec;
 * the domain-to-packet mappings remain compatible with v1001.</p>
 */
public class PacketEncoder_v2168 extends PacketEncoder_v1001 {

    public PacketEncoder_v2168(ProtocolData data) {
        super(data);
    }

    @Override
    public CraftingDataPacket encodeCraftingData() {
        var packet = super.encodeCraftingData();

        for (RecipeData recipe : packet.getCraftingData()) {
            switch (recipe.getType()) {
                case SHAPED -> packet.getShapedData().add((ShapedRecipeData) recipe);
                case SHAPELESS -> packet.getShapelessData().add((ShapelessRecipeData) recipe);
                case MULTI -> packet.getMultiData().add((MultiRecipeData) recipe);
                case SHULKER_BOX -> packet.getShapelessUserData().add((ShapelessRecipeData) recipe);
                case SHAPELESS_CHEMISTRY -> packet.getShapelessChemistryData().add((ShapelessRecipeData) recipe);
                case SHAPED_CHEMISTRY -> packet.getShapedChemistryData().add((ShapedRecipeData) recipe);
                case SMITHING_TRANSFORM -> packet.getSmithingTransformData().add((SmithingTransformRecipeData) recipe);
                case SMITHING_TRIM -> packet.getSmithingTrimData().add((SmithingTrimRecipeData) recipe);
                case FURNACE, FURNACE_DATA -> {
                }
            }
        }
        packet.getCraftingData().clear();
        return packet;
    }

    @Override
    public PlayerListPacket encodePlayerList(
            Collection<? extends Player> players,
            boolean add,
            boolean trustSkins
    ) {
        return stampEntryActions(super.encodePlayerList(players, add, trustSkins));
    }

    @Override
    public Collection<BedrockPacket> encodePlayerSkin(EntityPlayer player, boolean trustSkin) {
        var packets = super.encodePlayerSkin(player, trustSkin);
        for (var packet : packets) {
            if (packet instanceof PlayerListPacket listPacket) {
                stampEntryActions(listPacket);
            }
        }
        return packets;
    }

    private static PlayerListPacket stampEntryActions(PlayerListPacket packet) {
        if (packet == null) {
            return null;
        }
        for (var entry : packet.getEntries()) {
            if (entry.getAction() == null) {
                entry.setAction(packet.getAction());
            }
        }
        return packet;
    }
}
