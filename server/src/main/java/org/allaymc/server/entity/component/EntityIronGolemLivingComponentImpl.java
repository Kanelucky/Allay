package org.allaymc.server.entity.component;

import org.allaymc.api.entity.Entity;
import org.allaymc.api.entity.ai.memory.MemoryTypes;
import org.allaymc.api.entity.component.EntityAIComponent;
import org.allaymc.api.entity.damage.DamageContainer;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.entity.interfaces.EntityProjectile;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.item.type.ItemTypes;
import org.allaymc.server.component.annotation.Dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EntityIronGolemLivingComponentImpl extends EntityLivingComponentImpl {

    @Dependency
    protected EntityAIComponent aiComponent;

    public EntityIronGolemLivingComponentImpl() {
        setMaxHealth(100);
    }

    @Override
    public boolean hasFallDamage() {
        return false;
    }

    protected Entity resolveAttacker(Object attacker) {
        if (attacker instanceof EntityProjectile projectile) {
            return projectile.getShooter();
        }

        return attacker instanceof Entity entity ? entity : null;
    }

    @Override
    public boolean attack(DamageContainer damage, boolean ignoreCoolDown) {
        damage.setFinalDamage(damage.getFinalDamage() * 2);

        if (!super.attack(damage, ignoreCoolDown)) {
            return false;
        }

        var attacker = resolveAttacker(damage.getAttacker());
        if (attacker instanceof EntityPlayer && attacker.isAlive()) {
            aiComponent.getMemoryStorage().put(MemoryTypes.ATTACK_TARGET, attacker.getRuntimeId());
        }

        return true;
    }

    @Override
    public List<ItemStack> getDrops(int lootingLevel) {
        var drops = new ArrayList<ItemStack>();
        var rand = ThreadLocalRandom.current();
        drops.add(ItemTypes.IRON_INGOT.createItemStack(3 + rand.nextInt(3)));
        int poppies = rand.nextInt(3);
        if (poppies > 0) {
            drops.add(ItemTypes.POPPY.createItemStack(poppies));
        }
        return drops;
    }

    @Override
    public int getDropXpAmount() {
        return 0;
    }
}
