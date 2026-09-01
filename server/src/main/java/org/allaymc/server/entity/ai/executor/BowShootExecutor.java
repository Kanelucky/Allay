package org.allaymc.server.entity.ai.executor;

import org.allaymc.api.entity.EntityInitInfo;
import org.allaymc.api.entity.ai.behavior.BehaviorExecutor;
import org.allaymc.api.entity.ai.memory.MemoryType;
import org.allaymc.api.entity.data.EntityAnimation;
import org.allaymc.api.entity.interfaces.EntityIntelligent;
import org.allaymc.api.entity.interfaces.EntityLiving;
import org.allaymc.api.entity.type.EntityTypes;
import org.allaymc.api.eventbus.event.entity.EntityShootBowEvent;
import org.allaymc.api.item.interfaces.ItemBowStack;
import org.allaymc.api.item.type.ItemTypes;
import org.allaymc.api.math.location.Location3dc;
import org.allaymc.api.player.GameMode;
import org.allaymc.api.server.Server;
import org.allaymc.api.world.sound.SimpleSound;
import org.joml.Vector3d;

/**
 * @author Kanelucky
 */
public class BowShootExecutor implements BehaviorExecutor {

    protected static final double ARROW_SPEED = 1.6;

    protected final MemoryType<Long> primaryMemory;
    protected final MemoryType<Long> fallbackMemory;
    protected final float speed;
    protected final double maxShootDistanceSquared;
    protected final double minShootDistanceSquared;
    protected final int coolDownTick;
    protected final int pullBowTick;

    protected EntityLiving target;
    protected Vector3d lastTargetPos;
    protected int tick1;
    protected int tick2;

    public BowShootExecutor(
            MemoryType<Long> primaryMemory, MemoryType<Long> fallbackMemory, float speed, int maxShootDistance) {
        this(primaryMemory, fallbackMemory, speed, maxShootDistance, 4, 20, 20);
    }

    public BowShootExecutor(
            MemoryType<Long> primaryMemory,
            MemoryType<Long> fallbackMemory,
            float speed,
            int maxShootDistance,
            int minShootDistance,
            int coolDownTick,
            int pullBowTick) {
        this.primaryMemory = primaryMemory;
        this.fallbackMemory = fallbackMemory;
        this.speed = speed;
        this.maxShootDistanceSquared = (double) maxShootDistance * maxShootDistance;
        this.minShootDistanceSquared = (double) minShootDistance * minShootDistance;
        this.coolDownTick = coolDownTick;
        this.pullBowTick = pullBowTick;
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        target = null;
        lastTargetPos = null;
        tick1 = 0;
        tick2 = 0;
        if (!entity.isPitchEnabled()) entity.setPitchEnabled(true);
        if (entity.getMovementSpeed() != speed) entity.setMovementSpeed(speed);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        var living = resolveTarget(entity);
        if (living == null) return false;

        if (living.isPlayer()
                && living.asPlayer().getGameMode() == GameMode.CREATIVE
                && !entity.getWorld()
                        .getName()
                        .equals(living.asPlayer().getWorld().getName())) {
            return false;
        }

        target = living;

        Location3dc targetLoc = target.getLocation();
        var loc = entity.getLocation();
        double distSq = loc.distanceSquared(targetLoc);
        EntityControlHelper.setLookTarget(entity, targetLoc);

        if (distSq > maxShootDistanceSquared) {
            var targetPos = new Vector3d(targetLoc.x(), targetLoc.y(), targetLoc.z());
            entity.setMoveTarget(targetPos);
            if (lastTargetPos == null || isInDifferentBlock(lastTargetPos, targetPos)) {
                entity.getBehaviorGroup().setRouteUpdateRequired(true);
            }
            lastTargetPos = targetPos;
            tick1 = 0;
            tick2 = 0;
            return true;
        }

        if (distSq < minShootDistanceSquared) {
            var away = new Vector3d(loc).sub(targetLoc.x(), targetLoc.y(), targetLoc.z());
            if (away.lengthSquared() < 1e-4) {
                away.set(1, 0, 0);
            } else {
                away.normalize();
            }
            var retreatPos = new Vector3d(loc).add(away.mul(4));
            EntityControlHelper.setRouteTarget(entity, retreatPos);
            entity.getBehaviorGroup().setRouteUpdateRequired(true);
            lastTargetPos = null;
            tick1 = 0;
            tick2 = 0;
            return true;
        }
        EntityControlHelper.removeRouteTarget(entity);
        lastTargetPos = null;

        if (tick2 == 0) {
            tick1++;
            if (tick1 > coolDownTick) {
                tick1 = 0;
                tick2 = 1;
                entity.applyAnimation(new EntityAnimation("animation.humanoid.bow_and_arrow.v1.0", "", "controller.animation.skeleton.attack", ""));
            }
            return true;
        }

        tick2++;
        if (tick2 > pullBowTick) {
            tick2 = 0;
            bowShoot(ItemTypes.BOW.createItemStack(), entity, target);
        }
        return true;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        EntityControlHelper.removeRouteTarget(entity);
        EntityControlHelper.removeLookTarget(entity);
        entity.setPitchEnabled(false);
        target = null;
        lastTargetPos = null;
        tick1 = 0;
        tick2 = 0;
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        onStop(entity);
    }

    protected boolean isInDifferentBlock(Vector3d oldTargetPos, Vector3d newTargetPos) {
        return Math.floor(oldTargetPos.x()) != Math.floor(newTargetPos.x())
                || Math.floor(oldTargetPos.y()) != Math.floor(newTargetPos.y())
                || Math.floor(oldTargetPos.z()) != Math.floor(newTargetPos.z());
    }

    protected EntityLiving resolveTarget(EntityIntelligent entity) {
        var primaryId = entity.getMemoryStorage().get(primaryMemory);
        if (primaryId != null) {
            var resolved = entity.getDimension().getEntityManager().getEntity(primaryId);
            if (resolved instanceof EntityLiving living && living.isAlive() && isWithinSenseRange(entity, living)) {
                return living;
            }
            entity.getMemoryStorage().clear(primaryMemory);
        }

        var fallbackId = entity.getMemoryStorage().get(fallbackMemory);
        if (fallbackId == null) return null;

        var resolved = entity.getDimension().getEntityManager().getEntity(fallbackId);
        return (resolved instanceof EntityLiving living && living.isAlive()) ? living : null;
    }

    protected boolean isWithinSenseRange(EntityIntelligent entity, EntityLiving candidate) {
        var loc = entity.getLocation();
        var candidateLoc = candidate.getLocation();
        var distSq = loc.distanceSquared(candidateLoc);
        return distSq <= maxShootDistanceSquared * 4;
    }

    protected void bowShoot(ItemBowStack bow, EntityLiving shooter, EntityLiving target) {
        var shooterLoc = shooter.getLocation();
        var eyePos = new Vector3d(shooterLoc.x(), shooterLoc.y() + shooter.getEyeHeight(), shooterLoc.z());
        var targetLoc = target.getLocation();
        var aimPos = new Vector3d(targetLoc.x(), targetLoc.y() + target.getEyeHeight(), targetLoc.z());
        var direction = new Vector3d(aimPos).sub(eyePos).normalize();
        var arrow = EntityTypes.ARROW.createEntity(EntityInitInfo.builder()
                .dimension(shooter.getDimension())
                .pos(eyePos)
                .rot(-shooterLoc.yaw(), -shooterLoc.pitch())
                .motion(direction.x * ARROW_SPEED, direction.y * ARROW_SPEED, direction.z * ARROW_SPEED)
                .build());
        arrow.setShooter(shooter);
        EntityShootBowEvent event = new EntityShootBowEvent(shooter, bow, arrow);
        Server.getInstance().getEventBus().callEvent(event);
        if (!event.call()) {
            return;
        }

        shooter.getDimension().getEntityManager().addEntity(event.getArrow());
        shooter.getDimension().addSound(arrow.getLocation(), SimpleSound.BOW_SHOOT);
    }
}
