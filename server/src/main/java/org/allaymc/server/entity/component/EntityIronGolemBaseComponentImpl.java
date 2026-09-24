package org.allaymc.server.entity.component;

import org.allaymc.api.entity.EntityInitInfo;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class EntityIronGolemBaseComponentImpl extends EntityBaseComponentImpl {

    public EntityIronGolemBaseComponentImpl(EntityInitInfo initInfo) {
        super(initInfo);
    }

    @Override
    public AABBdc getBaseAABB() {
        return new AABBd(-0.7, 0.0, -0.7, 0.7, 2.7, 0.7);
    }
}
