package org.allaymc.server.entity.ai.controller;

import org.allaymc.api.entity.ai.controller.Controller;
import org.allaymc.api.entity.component.EntityClimbableComponent;
import org.allaymc.api.entity.interfaces.EntityIntelligent;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;

/**
 * @author Kanelucky
 */
public class ClimbController implements Controller {

    protected static final double PRECISION = 1.0E-4;
    protected static final double MAX_CLIMB_SPEED = 0.16;

    @Override
    public boolean control(EntityIntelligent entity) {
        if (!(entity instanceof EntityClimbableComponent climbable)) {
            return false;
        }

        Vector3dc target = entity.getMoveTarget();
        if (target == null) {
            climbable.setClimbing(false);
            return false;
        }

        var loc = entity.getLocation();
        double dx = target.x() - loc.x();
        double dz = target.z() - loc.z();
        double xzLengthSquared = dx * dx + dz * dz;

        if (xzLengthSquared < PRECISION) {
            climbable.setClimbing(false);
            return false;
        }

        double xzLength = Math.sqrt(xzLengthSquared);
        double speedBase = entity.getMovementSpeed();
        double stepX = dx / xzLength;
        double stepZ = dz / xzLength;
        boolean collidedHorizontally = collidesBlocks(entity, stepX * speedBase, stepZ * speedBase);

        double speed = collidedHorizontally ? speedBase * 0.33 : speedBase * 0.1;
        climbable.setClimbing(collidedHorizontally);
        double climbSpeed = Math.min(speed, MAX_CLIMB_SPEED);
        double dy = collidedHorizontally ? Math.max(-climbSpeed, Math.min(climbSpeed, target.y() - loc.y())) : 0;
        entity.addMotion(stepX * speed, dy, stepZ * speed);
        return true;
    }

    protected boolean collidesBlocks(EntityIntelligent entity, double dx, double dz) {
        var aabb = new AABBd(entity.getOffsetAABB()).translate(dx, 0, dz);
        return entity.getDimension().getCollidingBlockStates(aabb) != null;
    }
}
