package org.allaymc.api.entity.component;

/**
 * @author Kanelucky
 */
public interface EntityFlyableComponent extends EntityComponent {

    boolean isFlying();

    void setFlying(boolean flying);
}
