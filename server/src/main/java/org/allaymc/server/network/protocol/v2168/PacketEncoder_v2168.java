package org.allaymc.server.network.protocol.v2168;

import org.allaymc.server.network.protocol.ProtocolData;
import org.allaymc.server.network.protocol.v1001.PacketEncoder_v1001;
import org.cloudburstmc.protocol.bedrock.data.inventory.crafting.recipe.*;
import org.cloudburstmc.protocol.bedrock.packet.CraftingDataPacket;

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
}
