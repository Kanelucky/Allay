package org.allaymc.server.entity.component;

import org.allaymc.api.entity.component.EntityAIComponent;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.item.type.ItemTypes;
import org.allaymc.server.component.annotation.Dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Living component implementation for skeletons.
 */
public class EntitySkeletonLivingComponentImpl extends EntityLivingComponentImpl {

    @Dependency
    protected EntityAIComponent aiComponent;

    public EntitySkeletonLivingComponentImpl() {
        setMaxHealth(20);
    }

    @Override
    public List<ItemStack> getDrops(int lootingLevel) {
        var drops = new ArrayList<ItemStack>();
        var rand = ThreadLocalRandom.current();
        int boneCount = rand.nextInt(4 + lootingLevel);
        if (boneCount > 0) {
            drops.add(ItemTypes.BONE.createItemStack(boneCount));
        }

        float rareChance = (1f / 120f) + ((1f / 300f) * lootingLevel);
        if (rand.nextFloat() < rareChance) {
            switch (rand.nextInt(4)) {
                case 0 -> drops.add(ItemTypes.BOW.createItemStack());
                case 1 -> drops.add(ItemTypes.ARROW.createItemStack(rand.nextInt(2)));
                default -> {}
            }
        }

        return drops;
    }

    @Override
    public int getDropXpAmount() {
        return 5;
    }
}
