package org.allaymc.server.container.impl;

import lombok.Getter;
import lombok.Setter;
import org.allaymc.api.block.type.BlockTypes;
import org.allaymc.api.container.ContainerTypes;
import org.allaymc.api.container.interfaces.SmithingTableContainer;
import org.allaymc.api.item.recipe.input.RecipeInput;
import org.allaymc.api.item.recipe.input.SmithingRecipeInput;
import org.allaymc.api.math.position.Position3ic;
import org.allaymc.api.player.Player;
import org.joml.Vector3ic;

public class FakeSmithingTableContainerImpl extends FakeContainerImpl implements SmithingTableContainer {

    @Getter
    @Setter
    protected Position3ic blockPos;

    public FakeSmithingTableContainerImpl() {
        super(ContainerTypes.SMITHING_TABLE);
    }

    @Override
    public RecipeInput createRecipeInput() {
        return new SmithingRecipeInput(getTemplate(), getInput(), getMaterial());
    }

    @Override
    protected void sendFakeBlocks(Player player) {
        var pos = computeFakeBlockPos(player);
        player.viewBlockUpdate(pos, 0, BlockTypes.SMITHING_TABLE.getDefaultState());
        this.fakeBlockPositions.put(player, new Vector3ic[]{pos});
    }
}