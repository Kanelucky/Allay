package org.allaymc.server.entity.component;

import org.allaymc.api.entity.Entity;
import org.allaymc.api.entity.ai.memory.MemoryTypes;
import org.allaymc.api.entity.component.EntityAIComponent;
import org.allaymc.api.entity.component.EntityLivingComponent;
import org.allaymc.api.entity.damage.DamageContainer;
import org.allaymc.api.entity.interfaces.EntityProjectile;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.item.type.ItemTypes;
import org.allaymc.server.component.annotation.Dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EntitySpiderLivingComponentImpl extends EntityLivingComponentImpl {

    @Dependency
    protected EntityAIComponent aiComponent;

    public EntitySpiderLivingComponentImpl() {
        setHealth(16);
    }

    @Override
    public List<ItemStack> getDrops(int lootingLevel) {
        var drops = new ArrayList<ItemStack>();
        var rand = ThreadLocalRandom.current();
        int stringCount = rand.nextInt(0, 3 + lootingLevel);
        if (stringCount > 0) {
            drops.add(ItemTypes.STRING.createItemStack(stringCount));
        }

        int spiderEyeCount = rand.nextInt(0, 2 + lootingLevel);
        Entity attacker = lastDamage != null ? lastDamage.getAttacker() : null;
        if (attacker != null && attacker.isPlayer()) {
            drops.add(ItemTypes.SPIDER_EYE.createItemStack(spiderEyeCount));
        }

        return drops;
    }

    @Override
    public boolean attack(DamageContainer damage, boolean ignoreCoolDown) {
        if (!super.attack(damage, ignoreCoolDown)) {
            return false;
        }

        var attacker = resolveAttacker(damage.getAttacker());
        if (attacker == null || attacker == thisEntity || !attacker.isAlive() || !(attacker instanceof EntityLivingComponent)) {
            return true;
        }

        aiComponent.getMemoryStorage().put(MemoryTypes.ATTACK_TARGET, attacker.getRuntimeId());
        return true;
    }

    protected Entity resolveAttacker(Object attacker) {
        if (attacker instanceof EntityProjectile projectile) {
            return projectile.getShooter();
        }
        return attacker instanceof Entity entity ? entity : null;
    }

    @Override
    public int getDropXpAmount() {
        return 5;
    }
}
