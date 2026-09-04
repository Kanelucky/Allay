package org.allaymc.server.entity.ai.controller;

import org.allaymc.api.entity.ai.controller.Controller;
import org.allaymc.api.entity.interfaces.EntityIntelligent;

public class FlyController implements Controller {

    protected static final double EXTERNAL_MOTION_THRESHOLD = 0.4756;

    @Override
    public boolean control(EntityIntelligent entity) {
        if (!entity.hasMoveDirection()) {
            return false;
        }
        var end = entity.getMoveDirectionEnd();
        if (end == null) {
            return false;
        }
        var motion = entity.getMotion();
        float speed = entity.getMovementSpeed();
        if (motion.lengthSquared() > speed * speed * EXTERNAL_MOTION_THRESHOLD) {
            return false;
        }
        var loc = entity.getLocation();
        double dx = end.x() - loc.x();
        double dy = end.y() - loc.y();
        double dz = end.z() - loc.z();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 0.01) {
            return false;
        }
        double factor = Math.min(speed, distance) / distance;
        entity.addMotion(dx * factor - motion.x(), dy * factor - motion.y(), dz * factor - motion.z());
        return true;
    }
}
