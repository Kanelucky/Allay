package org.allaymc.server.entity.ai.executor;

import org.allaymc.api.entity.EntityInitInfo;
import org.allaymc.api.entity.ai.behavior.BehaviorExecutor;
import org.allaymc.api.entity.ai.memory.MemoryType;
import org.allaymc.api.entity.interfaces.EntityArrow;
import org.allaymc.api.entity.interfaces.EntityIntelligent;
import org.allaymc.api.entity.interfaces.EntityLiving;
import org.allaymc.api.eventbus.event.entity.EntityShootBowEvent;
import org.allaymc.api.item.interfaces.ItemBowStack;
import org.allaymc.api.item.type.ItemTypes;
import org.allaymc.api.math.location.Location3dc;
import org.allaymc.api.player.GameMode;
import org.allaymc.api.server.Server;
import org.joml.Vector3dc;

/**
 * @author Kanelucky
 */
public class BowShootExecutor implements BehaviorExecutor {

    protected final MemoryType<Long> memoryType;
    protected final float speed;
    protected int maxShootDistanceSquared;
    protected int attackTick;
    protected EntityLiving target;

    public BowShootExecutor(MemoryType<Long> memoryType, float speed, int maxShootDistance) {
        this.memoryType = memoryType;
        this.speed = speed;
        this.maxShootDistanceSquared = maxShootDistance * maxShootDistance;
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        attackTick++;
        if (!entity.isPitchEnabled()) entity.setPitchEnabled(true);
        if (entity.getMemoryStorage().isEmpty(memoryType)) return false;
        var newTarget = entity.getDimension().getEntityManager().getEntity(entity.getRuntimeId());
        if (this.target == null) return null == newTarget;
        if (!target.isAlive()) {
            return false;
        } else if (target.isPlayer()
                && target.asPlayer().getGameMode() == GameMode.CREATIVE
                && !entity.getWorld()
                        .getName()
                        .equals(target.asPlayer().getWorld().getName())) return false;
        if (!target.getLocation().equals(newTarget)) {
            target = newTarget.asPlayer();
        }
        if (entity.getMovementSpeed() != speed) entity.setMovementSpeed(speed);
        Location3dc clone = this.target.getLocation();
        EntityControlHelper.setRouteTarget(entity, clone);
        EntityControlHelper.setLookTarget(entity, clone);
        bowShoot(ItemTypes.BOW.createItemStack(), entity);
        return true;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        EntityControlHelper.removeRouteTarget(entity);
        EntityControlHelper.removeLookTarget(entity);
        entity.setPitchEnabled(false);
        this.target = null;
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        EntityControlHelper.removeRouteTarget(entity);
        EntityControlHelper.removeLookTarget(entity);
        entity.setPitchEnabled(false);
        this.target = null;
    }

    protected void bowShoot(ItemBowStack bow, EntityLiving entity) {
        EntityArrow arrow = (EntityArrow) entity.getEntityType().createEntity(EntityInitInfo.builder()
                                  .dimension(entity.getDimension())
                                  .loc(entity.getLocation())
                                  .build());
        EntityShootBowEvent entityShootBowEvent = new EntityShootBowEvent(entity, bow, arrow);
        Server.getInstance().getEventBus().callEvent(entityShootBowEvent);
        if (!entityShootBowEvent.call()) {
            return;
        }
        entityShootBowEvent.getArrow().setMotion(1.0, 1.0, 1.0);
        entityShootBowEvent.getArrow().spawnTo(entity.getViewers());
    }
}
