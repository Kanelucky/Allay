package org.allaymc.server.entity.ai.controller;

import org.allaymc.api.entity.ai.controller.Controller;
import org.allaymc.api.entity.interfaces.EntityIntelligent;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Dealing with physical movement in flight/swimming
 *
 * @author Kanelucky | PowerNukkitX
 */
public class SpaceMoveController implements Controller {
    @Override
    public boolean control(EntityIntelligent entity) {
        if (entity.hasMoveDirection() && !entity.shouldUpdateMoveDirection()) {
            Vector3dc direction = entity.getMoveDirectionEnd();
            var speed = entity.getMovementSpeed();
            if (entity.getMotion().x() * entity.getMotion().x()
                            + entity.getMotion().z() * entity.getMotion().z()
                    > speed * speed * 0.4756) {
                return false;
            }
            var location = entity.getLocation();
            var relativeVector = new Vector3d(
                    direction.x() - location.x(), direction.y() - location.y(), direction.z() - location.z());
            var xyzLength = Math.sqrt(relativeVector.x * relativeVector.x
                    + relativeVector.y * relativeVector.y
                    + relativeVector.z * relativeVector.z);
            var k = speed / xyzLength * 0.33;
            var dx = relativeVector.x * k;
            var dy = relativeVector.y * k;
            var dz = relativeVector.z * k;
            entity.setMotion(new Vector3d(dx, dy, dz));
            if (xyzLength < speed) {
                needNewDirection(entity);
                return false;
            }
            return true;
        }
        return false;
    }

    protected void needNewDirection(EntityIntelligent entity) {
        entity.setShouldUpdateMoveDirection(true);
    }
}
