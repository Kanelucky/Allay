package org.allaymc.server.entity.ai.executor;

import org.allaymc.api.entity.interfaces.EntityIntelligent;
import org.joml.Vector3d;

import java.util.concurrent.ThreadLocalRandom;

public class SpaceRandomRoamExecutor extends FlatRandomRoamExecutor {

    protected int maxYRoamRange;

    public SpaceRandomRoamExecutor(float speed, int maxXZRoamRange, int maxYRoamRange, int frequency) {
        this(speed, maxXZRoamRange, maxYRoamRange, frequency, false, 100);
    }

    public SpaceRandomRoamExecutor(
            float speed,
            int maxXZRoamRange,
            int maxYRoamRange,
            int frequency,
            boolean calNextTargetImmediately,
            int runningTime) {
        this(speed, maxXZRoamRange, maxYRoamRange, frequency, calNextTargetImmediately, runningTime, true, 10);
    }

    public SpaceRandomRoamExecutor(
            float speed,
            int maxXZRoamRange,
            int maxYRoamRange,
            int frequency,
            boolean calNextTargetImmediately,
            int runningTime,
            boolean avoidWater,
            int maxRetryTime) {
        super(speed, maxXZRoamRange, frequency, calNextTargetImmediately, runningTime, avoidWater, maxRetryTime);
        this.maxYRoamRange = maxYRoamRange;
    }

    protected Vector3d next(EntityIntelligent entity) {
        var random = ThreadLocalRandom.current();
        double x = random.nextInt(maxRoamRange * 2)
                - maxRoamRange
                + entity.getLocation().x();
        double z = random.nextInt(maxRoamRange * 2)
                - maxRoamRange
                + entity.getLocation().z();
        double y = random.nextInt(maxYRoamRange * 2)
                - maxYRoamRange
                + entity.getLocation().y();
        return new Vector3d(x, y, z);
    }
}
