package org.allaymc.server.entity.component;

import org.allaymc.api.entity.component.EntityFlyingPhysicsComponent;

public class EntityFlyingPhysicsComponentImpl extends EntityPhysicsComponentImpl implements EntityFlyingPhysicsComponent {

    @Override
    public double getGravity() {
        return 0;
    }

    @Override
    public double getDragFactorInAir() {
        return 0.09;
    }

    @Override
    public boolean computeLiquidPhysics() {
        return false;
    }
}
