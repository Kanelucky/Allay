package org.allaymc.server.entity.component;

import org.allaymc.api.entity.component.EntityClimbableComponent;

public class EntityClimbableComponentImpl extends EntityPhysicsComponentImpl implements EntityClimbableComponent {

    private boolean climbing;

    @Override
    public boolean isClimbing() {
        return climbing;
    }

    @Override
    public void setClimbing(boolean climbing) {
        this.climbing = climbing;
    }
}
