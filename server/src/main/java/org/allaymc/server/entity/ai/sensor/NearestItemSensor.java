package org.allaymc.server.entity.ai.sensor;

import org.allaymc.api.entity.Entity;
import org.allaymc.api.entity.ai.memory.MemoryTypes;
import org.allaymc.api.entity.ai.sensor.Sensor;
import org.allaymc.api.entity.interfaces.EntityIntelligent;
import org.allaymc.api.entity.interfaces.EntityItem;
import org.allaymc.api.world.Dimension;

/**
 * @author Kanelucky
 */
public class NearestItemSensor implements Sensor {
    protected final double range;
    protected final double minRange;
    protected final int period;

    public NearestItemSensor(double range, double minRange, int period) {
        this.range = range;
        this.minRange = minRange;
        this.period = period;
    }

    public NearestItemSensor(double range) {
        this(range, 0, 1);
    }

    @Override
    public void sense(EntityIntelligent entity) {
        var lookingItem = entity.getMemoryStorage().get(MemoryTypes.LOOKING_ITEM);
        if (lookingItem == null) {
            return;
        }
        var itemClass = lookingItem.getClass();
        if (itemClass == null) return;

        EntityItem item = null;
        double nearestSquared = 0;
        double rangeSquared = this.range * this.range;
        double minRangeSquared = this.minRange * this.minRange;
        Dimension dimension = entity.getDimension();
        int minChunkX = (int) Math.floor((entity.getLocation().x() - this.range - 2) * 0.0625);
        int maxChunkX = (int) Math.ceil((entity.getLocation().x() + this.range + 2) * 0.0625);
        int minChunkZ = (int) Math.floor((entity.getLocation().z() - this.range - 2) * 0.0625);
        int maxChunkZ = (int) Math.ceil((entity.getLocation().z() + this.range + 2) * 0.0625);
        // Find the nearest player within range
        for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                for (Entity e : dimension
                        .getEntityManager()
                        .getEntitiesInChunk(chunkX, chunkZ)
                        .values()) {
                    if (e instanceof EntityItem entityItem) {
                        if (itemClass.isAssignableFrom(entityItem.getItemStack().getClass())) {
                            double distanceSquared = entity.getLocation().distanceSquared(e.getLocation());
                            if (distanceSquared <= rangeSquared && distanceSquared >= minRangeSquared) {
                                if (item == null || distanceSquared < nearestSquared) {
                                    item = entityItem;
                                    nearestSquared = distanceSquared;
                                }
                            }
                        }
                    }
                }
            }
        }
        entity.getMemoryStorage().put(MemoryTypes.NEAREST_ITEM, item.getRuntimeId());
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
