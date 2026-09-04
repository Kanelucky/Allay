package org.allaymc.server.entity.component;

import lombok.Getter;
import org.allaymc.api.entity.component.EntityFlyableComponent;
import org.allaymc.api.entity.component.EntityPhysicsComponent;
import org.allaymc.api.utils.identifier.Identifier;
import org.allaymc.server.component.annotation.Dependency;

/**
 * @author Kanelucky
 */
public class EntityFlyableComponentImpl implements EntityFlyableComponent {

    @Identifier.Component
    public static final Identifier IDENTIFIER = new Identifier("minecraft:entity_flyable_component");

    @Dependency
    protected EntityPhysicsComponent physicsComponent;

    @Getter
    private boolean flying;

    @Override
    public void setFlying(boolean flying) {
        this.flying = flying;
        physicsComponent.setHasGravity(!flying);
    }
}
