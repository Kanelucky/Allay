package org.allaymc.server.entity.ai.route.posevaluator;

import org.allaymc.api.entity.interfaces.EntityIntelligent;
import org.joml.Vector3dc;

public class FlyingPosEvaluator implements SpacePosEvaluator {

    @Override
    public boolean evaluate(EntityIntelligent entity, Vector3dc pos) {
        return true;
    }
}
