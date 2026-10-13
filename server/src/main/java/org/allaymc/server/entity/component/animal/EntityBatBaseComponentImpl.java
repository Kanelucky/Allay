package org.allaymc.server.entity.component.animal;

import org.allaymc.api.entity.EntityInitInfo;
import org.allaymc.server.entity.component.EntityBaseComponentImpl;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class EntityBatBaseComponentImpl extends EntityBaseComponentImpl {

    public EntityBatBaseComponentImpl(EntityInitInfo initInfo) {
        super(initInfo);
    }

    @Override
    public AABBdc getBaseAABB() {
        return new AABBd(-0.25, 0.0, -0.25, 0.25, 0.9, 0.25);
    }
}
