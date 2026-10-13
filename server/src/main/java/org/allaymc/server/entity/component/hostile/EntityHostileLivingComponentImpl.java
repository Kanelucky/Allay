package org.allaymc.server.entity.component.hostile;

import org.allaymc.api.entity.Entity;
import org.allaymc.api.entity.component.EntityAIComponent;
import org.allaymc.api.entity.interfaces.EntityProjectile;
import org.allaymc.server.component.annotation.Dependency;
import org.allaymc.server.entity.component.EntityLivingComponentImpl;

public class EntityHostileLivingComponentImpl extends EntityLivingComponentImpl {

    @Dependency
    protected EntityAIComponent aiComponent;

    public EntityHostileLivingComponentImpl() {
        setMaxHealth(20);
    }

    protected Entity resolveAttacker(Object attacker) {
        if (attacker instanceof EntityProjectile projectile) {
            return projectile.getShooter();
        }
        return attacker instanceof Entity entity ? entity : null;
    }
}
