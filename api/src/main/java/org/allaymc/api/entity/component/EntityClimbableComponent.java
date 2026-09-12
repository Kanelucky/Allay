package org.allaymc.api.entity.component;

public interface EntityClimbableComponent extends EntityPhysicsComponent {

    boolean isClimbing();

    void setClimbing(boolean climbing);
}
