package org.allaymc.server.command.defaults;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.Entity;
import org.allaymc.api.entity.EntityInitInfo;
import org.allaymc.api.entity.interfaces.EntityIntelligent;
import org.allaymc.api.entity.type.EntityType;
import org.allaymc.api.entity.type.EntityTypes;
import org.allaymc.api.eventbus.event.world.LightningStrikeEvent;
import org.allaymc.api.message.TrKeys;
import org.allaymc.api.permission.Permissions;
import org.allaymc.api.world.Dimension;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

/**
 * @author daoge_cmd, Kanelucky
 */
public class SummonCommand extends Command {

    public SummonCommand() {
        super("summon", TrKeys.MC_COMMANDS_SUMMON_DESCRIPTION, Permissions.COMMAND_SUMMON);
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        var entityTypeNode = tree.getRoot().entityType("entityType");
        entityTypeNode
                .pos("spawnPos").optional()
                .intNum("count", 1)
                .optional()
                .exec(
                        (context, sender) -> {
                            var dim = sender.getCommandExecuteLocation().dimension();
                            EntityType<?> entityType = context.getResult(0);
                            if (entityType == EntityTypes.PLAYER) {
                                context.addError("%" + TrKeys.MC_COMMANDS_SUMMON_FAILED);
                                return context.fail();
                            }
                            Vector3dc spawnPos = context.getResult(1);
                            if (spawnPos == null) {
                                spawnPos = sender.getCommandExecuteLocation();
                            }
                            var floorPos = spawnPos.floor(new Vector3d());
                            if (dim.getChunkManager().getChunkByDimensionPos((int) floorPos.x(), (int) floorPos.z())
                                    == null) {
                                context.addError("%" + TrKeys.MC_COMMANDS_SUMMON_OUTOFWORLD);
                                return context.fail();
                            }

                            int count = context.getResult(2);
                            for (var i = 1; i <= count; i++) {
                                // Special handling for lightning bolt - use strikeLightning with COMMAND cause
                                if (entityType == EntityTypes.LIGHTNING_BOLT) {
                                    dim.strikeLightning(spawnPos.x(), spawnPos.y(), spawnPos.z(), LightningStrikeEvent.Cause.COMMAND);
                                    continue;
                                }
                                summon(sender, dim, entityType, spawnPos, "");
                            }

                            context.addOutput(TrKeys.MC_COMMANDS_SUMMON_SUCCESS);
                            return context.success();
                        },
                        SenderType.ENTITY);
        entityTypeNode
                .str("nameTag")
                .pos("spawnPos").optional()
                .exec(
                        (context, sender) -> {
                            var dim = sender.getCommandExecuteLocation().dimension();
                            EntityType<?> entityType = context.getResult(0);
                            if (entityType == EntityTypes.PLAYER) {
                                context.addError("%" + TrKeys.MC_COMMANDS_SUMMON_FAILED);
                                return context.fail();
                            }
                            String nameTag = context.getResult(1);
                            Vector3dc spawnPos = context.getResult(2);
                            if (spawnPos == null) {
                                spawnPos = sender.getCommandExecuteLocation();
                            }
                            var floorPos = spawnPos.floor(new Vector3d());
                            if (dim.getChunkManager().getChunkByDimensionPos((int) floorPos.x(), (int) floorPos.z())
                                    == null) {
                                context.addError("%" + TrKeys.MC_COMMANDS_SUMMON_OUTOFWORLD);
                                return context.fail();
                            }

                            // Special handling for lightning bolt - use strikeLightning with COMMAND cause
                            if (entityType == EntityTypes.LIGHTNING_BOLT) {
                                dim.strikeLightning(spawnPos.x(), spawnPos.y(), spawnPos.z(), LightningStrikeEvent.Cause.COMMAND);
                            }
                            summon(sender, dim, entityType, spawnPos, nameTag);
                            context.addOutput(TrKeys.MC_COMMANDS_SUMMON_SUCCESS);
                            return context.success();
                        },
                        SenderType.ENTITY);
        var posAndFacingNode = entityTypeNode.pos("spawnPos").key("facing");
        posAndFacingNode
                .target("lookAtEntity")
                .exec(
                        (context, sender) -> {
                            var dim = sender.getCommandExecuteLocation().dimension();
                            EntityType<?> entityType = context.getResult(0);
                            if (entityType == EntityTypes.PLAYER) {
                                context.addError("%" + TrKeys.MC_COMMANDS_SUMMON_FAILED);
                                return context.fail();
                            }
                            Vector3dc spawnPos = context.getResult(1);
                            if (spawnPos == null) {
                                spawnPos = sender.getCommandExecuteLocation();
                            }
                            var floorPos = spawnPos.floor(new Vector3d());
                            if (dim.getChunkManager().getChunkByDimensionPos((int) floorPos.x(), (int) floorPos.z())
                                    == null) {
                                context.addError("%" + TrKeys.MC_COMMANDS_SUMMON_OUTOFWORLD);
                                return context.fail();
                            }

                            List<Entity> targets = context.getResult(3);
                            Vector3dc target = targets == null || targets.isEmpty()
                                    ? null
                                    : targets.getFirst().getLocation();

                            if (entityType == EntityTypes.LIGHTNING_BOLT) {
                                dim.strikeLightning(spawnPos.x(), spawnPos.y(), spawnPos.z(), LightningStrikeEvent.Cause.COMMAND);
                            }
                            summon(sender, dim, entityType, spawnPos, "", target);

                            context.addOutput(TrKeys.MC_COMMANDS_SUMMON_SUCCESS);
                            return context.success();
                        },
                        SenderType.ENTITY);
        posAndFacingNode
                .pos("lookAtPos")
                .exec(
                        (context, sender) -> {
                            var dim = sender.getCommandExecuteLocation().dimension();
                            EntityType<?> entityType = context.getResult(0);
                            if (entityType == EntityTypes.PLAYER) {
                                context.addError("%" + TrKeys.MC_COMMANDS_SUMMON_FAILED);
                                return context.fail();
                            }
                            Vector3dc spawnPos = context.getResult(1);
                            if (spawnPos == null) {
                                spawnPos = sender.getCommandExecuteLocation();
                            }
                            var floorPos = spawnPos.floor(new Vector3d());
                            if (dim.getChunkManager().getChunkByDimensionPos((int) floorPos.x(), (int) floorPos.z())
                                    == null) {
                                context.addError("%" + TrKeys.MC_COMMANDS_SUMMON_OUTOFWORLD);
                                return context.fail();
                            }

                            Vector3dc target = context.getResult(3);

                            if (entityType == EntityTypes.LIGHTNING_BOLT) {
                                dim.strikeLightning(spawnPos.x(), spawnPos.y(), spawnPos.z(), LightningStrikeEvent.Cause.COMMAND);
                            }
                            summon(sender, dim, entityType, spawnPos, "", target);

                            context.addOutput(TrKeys.MC_COMMANDS_SUMMON_SUCCESS);
                            return context.success();
                        },
                        SenderType.ENTITY);
    }

    private void summon(
            Entity sender,
            Dimension dimension,
            EntityType entityType,
            Vector3dc pos,
            @Nullable String nameTag,
            @Nullable Vector3dc targetOrPos) {
        var entity = entityType.createEntity(EntityInitInfo.builder()
                .pos(pos)
                .rot(sender.getLocation().yaw(), sender.getLocation().pitch())
                .dimension(dimension)
                .build());

        if (nameTag != null) {
            entity.setNameTag(nameTag);
        }

        if (targetOrPos != null && entity instanceof EntityIntelligent intelligent) {
            intelligent.setLookTarget(targetOrPos);
        }

        dimension.getEntityManager().addEntity(entity);
    }

    private void summon(
            Entity sender, Dimension dimension, EntityType entityType, Vector3dc pos, @Nullable String nameTag) {
        summon(sender, dimension, entityType, pos, nameTag, null);
    }
}
