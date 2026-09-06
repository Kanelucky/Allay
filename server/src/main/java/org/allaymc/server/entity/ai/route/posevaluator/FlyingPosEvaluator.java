package org.allaymc.server.entity.ai.route.posevaluator;

import org.allaymc.api.block.data.BlockTags;
import org.allaymc.api.entity.interfaces.EntityIntelligent;
import org.joml.Vector3dc;

public class FlyingPosEvaluator implements SpacePosEvaluator {

    @Override
    public boolean evaluate(EntityIntelligent entity, Vector3dc pos) {
        var aabb = entity.getAABB();
        var height = aabb.maxY() - aabb.minY();
        var blocksTall = (int) Math.ceil(height);

        var dimension = entity.getDimension();
        int x = (int) Math.floor(pos.x());
        int y = (int) Math.floor(pos.y());
        int z = (int) Math.floor(pos.z());

        for (int offset = 0; offset < blocksTall; offset++) {
            var blockState = dimension.getBlockState(x, y + offset, z);
            if (blockState == null) {
                return false;
            }

            var blockType = blockState.getBlockType();
            if (blockType.hasBlockTag(BlockTags.WATER) || blockType.hasBlockTag(BlockTags.LAVA)) {
                return false;
            }

            if (offset > 0 && blockState.getBlockStateData().hasCollision()) {
                return false;
            }
        }

        return true;
    }
}
