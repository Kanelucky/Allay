package org.allaymc.server.entity.ai.executor;

import org.allaymc.api.entity.Entity;
import org.allaymc.api.entity.EntityInitInfo;
import org.allaymc.api.entity.ai.behavior.BehaviorExecutor;
import org.allaymc.api.entity.ai.memory.MemoryType;
import org.allaymc.api.entity.ai.memory.MemoryTypes;
import org.allaymc.api.entity.component.EntityLivingComponent;
import org.allaymc.api.entity.effect.EffectInstance;
import org.allaymc.api.entity.effect.EffectTypes;
import org.allaymc.api.entity.interfaces.EntityIntelligent;
import org.allaymc.api.entity.interfaces.EntityLiving;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.entity.type.EntityTypes;
import org.allaymc.api.item.data.PotionType;
import org.allaymc.api.player.GameMode;
import org.allaymc.api.world.sound.SimpleSound;
import org.joml.Vector3d;

import java.util.concurrent.ThreadLocalRandom;

public class PotionAttackExecutor implements BehaviorExecutor {

    protected final MemoryType<Long> targetIdMemory;
    protected final float speed;
    protected final double maxSenseRangeSquared;
    protected final double preferredRangeSquared;
    protected final double minRangeSquared;
    protected final boolean clearTargetAfterLose;
    protected final int coolDown;

    protected int tick;
    protected Vector3d lastMoveTarget;

    protected Brew brew;
    protected int drinkTicks;
    protected int selfBuffCooldown;

    protected final Setting setting;

    public PotionAttackExecutor(
            MemoryType<Long> targetIdMemory,
            float speed,
            double maxSenseRange,
            double preferredRange,
            double minRange,
            boolean clearTargetAfterLose,
            int coolDown,
            Setting setting) {
        this.targetIdMemory = targetIdMemory;
        this.speed = speed;
        this.maxSenseRangeSquared = maxSenseRange * maxSenseRange;
        this.preferredRangeSquared = preferredRange * preferredRange;
        this.minRangeSquared = minRange * minRange;
        this.clearTargetAfterLose = clearTargetAfterLose;
        this.coolDown = coolDown;
        this.setting = setting;
    }

    public PotionAttackExecutor(
            MemoryType<Long> targetIdMemory,
            float speed,
            double maxSenseRange,
            double preferredRange,
            double minRange,
            boolean clearTargetAfterLose,
            int coolDown) {
        this.targetIdMemory = targetIdMemory;
        this.speed = speed;
        this.maxSenseRangeSquared = maxSenseRange * maxSenseRange;
        this.preferredRangeSquared = preferredRange * preferredRange;
        this.minRangeSquared = minRange * minRange;
        this.clearTargetAfterLose = clearTargetAfterLose;
        this.coolDown = coolDown;
        this.setting = Setting.defaults();
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        tick = 0;
        lastMoveTarget = null;
        entity.setMovementSpeed(speed);
        entity.setPitchEnabled(true);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        tick++;

        var targetId = entity.getMemoryStorage().get(targetIdMemory);
        if (targetId == null) {
            return false;
        }

        var target = entity.getDimension().getEntityManager().getEntity(targetId);
        if (!(target instanceof EntityLiving targetLiving) || !isTargetValid(target)) {
            return false;
        }

        var entityLoc = entity.getLocation();
        var targetLoc = target.getLocation();
        var distanceSquared = entityLoc.distanceSquared(targetLoc);
        if (distanceSquared > maxSenseRangeSquared) {
            return false;
        }

        if (!entity.isPitchEnabled()) {
            entity.setPitchEnabled(true);
        }
        if (entity.getMovementSpeed() != speed) {
            entity.setMovementSpeed(speed);
        }

        EntityControlHelper.setLookTarget(
                entity, new Vector3d(targetLoc.x(), targetLoc.y() + target.getEyeHeight(), targetLoc.z()));

        if (selfBuffCooldown > 0) {
            selfBuffCooldown--;
        }
        if (brew != null) {
            EntityControlHelper.removeRouteTarget(entity);
            lastMoveTarget = null;
            if (--drinkTicks <= 0) {
                finishDrinking(entity);
            }
            return true;
        }
        Brew next = chooseBrew(entity, distanceSquared);
        if (next != null) {
            brew = next;
            drinkTicks = ThreadLocalRandom.current().nextInt(setting.drinkMinTicks(), setting.drinkMaxTicks() + 1);
            EntityControlHelper.removeRouteTarget(entity);
            lastMoveTarget = null;
            return true;
        }

        updateMovement(
                entity, entityLoc.x(), entityLoc.z(), targetLoc.x(), targetLoc.y(), targetLoc.z(), distanceSquared);

        if (tick > coolDown) {
            throwPotion(entity, target, choosePotion(targetLiving, Math.sqrt(distanceSquared)));
            tick = 0;
        }

        return true;
    }

    protected Brew chooseBrew(EntityIntelligent entity, double targetDistanceSquared) {
        if (selfBuffCooldown > 0) {
            return null;
        }
        if (entity.isOnFire() && !entity.hasEffect(EffectTypes.FIRE_RESISTANCE)) {
            return Brew.FIRE_RESISTANCE;
        }
        if (entity.getHealth() < entity.getMaxHealth() * setting.lowHealthRatio()) {
            return Brew.HEALING;
        }
        if (targetDistanceSquared >= setting.speedDistanceSquared() && !entity.hasEffect(EffectTypes.SPEED)) {
            return Brew.SPEED;
        }
        return null;
    }

    protected void finishDrinking(EntityIntelligent entity) {
        Brew finished = brew;
        brew = null;
        drinkTicks = 0;
        selfBuffCooldown = setting.selfBuffCooldownTicks();
        tick = Math.max(0, coolDown - setting.postDrinkThrowDelay());
        if (finished == null) {
            return;
        }
        switch (finished) {
            case FIRE_RESISTANCE ->
                entity.addEffect(
                        new EffectInstance(EffectTypes.FIRE_RESISTANCE, 0, setting.fireResistanceTicks(), false, true));
            case HEALING ->
                entity.setHealth(Math.min(entity.getMaxHealth(), entity.getHealth() + setting.healAmount()));
            case SPEED ->
                entity.addEffect(new EffectInstance(EffectTypes.SPEED, 0, setting.speedTicks(), false, false));
        }
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        brew = null;
        drinkTicks = 0;
        EntityControlHelper.removeRouteTarget(entity);
        EntityControlHelper.removeLookTarget(entity);
        entity.setPitchEnabled(false);
        entity.setMovementSpeed(MemoryTypes.MOVEMENT_SPEED.defaultData().get());
        lastMoveTarget = null;
        if (clearTargetAfterLose) {
            entity.getMemoryStorage().clear(targetIdMemory);
        }
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        onStop(entity);
    }

    protected PotionType choosePotion(EntityLivingComponent target, double distance) {
        if (distance > setting.slownessRange() && !target.hasEffect(EffectTypes.SLOWNESS)) {
            return PotionType.SLOWNESS;
        }

        if (target.getHealth() >= setting.poisonHealthThreshold() && !target.hasEffect(EffectTypes.POISON)) {
            return PotionType.POISON;
        }

        if (distance <= setting.weaknessRange()
                && !target.hasEffect(EffectTypes.WEAKNESS)
                && ThreadLocalRandom.current().nextInt(4) == 0) {
            return PotionType.WEAKNESS;
        }

        return PotionType.HARMING;
    }

    protected void updateMovement(
            EntityIntelligent entity,
            double entityX,
            double entityZ,
            double targetX,
            double targetY,
            double targetZ,
            double distanceSquared) {
        Vector3d moveTarget;
        if (distanceSquared > preferredRangeSquared) {
            moveTarget = new Vector3d(targetX, targetY, targetZ);
        } else if (distanceSquared < minRangeSquared) {
            var dx = entityX - targetX;
            var dz = entityZ - targetZ;
            var length = Math.sqrt(dx * dx + dz * dz);
            if (length < 1e-4) {
                dx = 1;
                dz = 0;
                length = 1;
            }
            var retreat = Math.sqrt(preferredRangeSquared);
            moveTarget = new Vector3d(entityX + dx / length * retreat, targetY, entityZ + dz / length * retreat);
        } else {
            EntityControlHelper.removeRouteTarget(entity);
            lastMoveTarget = null;
            return;
        }

        entity.setMoveTarget(moveTarget);
        if (lastMoveTarget == null || isInDifferentBlock(lastMoveTarget, moveTarget)) {
            entity.getBehaviorGroup().setRouteUpdateRequired(true);
        }
        lastMoveTarget = moveTarget;
    }

    protected void throwPotion(EntityIntelligent entity, Entity target, PotionType potionType) {
        var dimension = entity.getDimension();
        var location = entity.getLocation();
        var throwPos = new Vector3d(location.x(), location.y() + entity.getEyeHeight() - 0.1, location.z());

        var targetLoc = target.getLocation();
        double dx = targetLoc.x() - throwPos.x();
        double dz = targetLoc.z() - throwPos.z();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double dy = targetLoc.y() - throwPos.y() + horizontalDistance * setting.arcLiftPerBlock();

        var direction = new Vector3d(dx, dy, dz);
        if (direction.lengthSquared() < 1e-6) {
            return;
        }
        direction.normalize();

        var potion = EntityTypes.SPLASH_POTION.createEntity(EntityInitInfo.builder()
                .dimension(dimension)
                .pos(throwPos)
                .rot(-location.yaw(), -location.pitch())
                .motion(direction.mul(setting.potionSpeed()))
                .build());
        potion.setShooter(entity);
        potion.setPotionType(potionType);
        dimension.getEntityManager().addEntity(potion);

        dimension.addSound(throwPos, SimpleSound.ITEM_THROW);
    }

    protected boolean isInDifferentBlock(Vector3d oldTarget, Vector3d newTarget) {
        return Math.floor(oldTarget.x()) != Math.floor(newTarget.x())
                || Math.floor(oldTarget.y()) != Math.floor(newTarget.y())
                || Math.floor(oldTarget.z()) != Math.floor(newTarget.z());
    }

    protected boolean isTargetValid(Entity target) {
        if (!target.isAlive()) return false;

        if (target instanceof EntityPlayer player) {
            var gameMode = player.getGameMode();
            return gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE;
        }

        return true;
    }

    protected enum Brew {
        FIRE_RESISTANCE,
        HEALING,
        SPEED
    }

    public record Setting(
            float potionSpeed,
            double arcLiftPerBlock,
            float poisonHealthThreshold,
            double slownessRange,
            double weaknessRange,
            int drinkMinTicks,
            int drinkMaxTicks,
            int selfBuffCooldownTicks,
            float lowHealthRatio,
            float healAmount,
            double speedDistanceSquared,
            int fireResistanceTicks,
            int speedTicks,
            int postDrinkThrowDelay) {
        public static Setting defaults() {
            return new Setting(0.5f, 0.22, 8, 8, 3, 35, 55, 20 * 6, 0.35f, 4f, 144.0, 20 * 200, 20 * 140, 10);
        }
    }
}
