package org.allaymc.server.entity.component;

import org.allaymc.api.entity.EntityInitInfo;
import org.allaymc.api.entity.component.EntityAIComponent;
import org.allaymc.server.component.annotation.Dependency;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class EntityAllayBaseComponentImpl extends EntityBaseComponentImpl {

    @Dependency
    protected EntityAIComponent aiComponent;

    public EntityAllayBaseComponentImpl(EntityInitInfo info) {
        super(info);
    }

    @Override
    public AABBdc getBaseAABB() {
        return new AABBd(-0.175, 0.0, -0.175, 0.175, 0.6, 0.175);
    }
}
