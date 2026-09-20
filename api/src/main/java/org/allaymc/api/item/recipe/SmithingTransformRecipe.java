package org.allaymc.api.item.recipe;

import lombok.Getter;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.item.component.ItemTrimmableComponent;
import org.allaymc.api.item.recipe.descriptor.ItemDescriptor;
import org.allaymc.api.item.recipe.input.RecipeInput;
import org.allaymc.api.item.recipe.input.SmithingRecipeInput;
import org.allaymc.api.utils.identifier.Identifier;

import java.util.ArrayList;
import java.util.function.BiFunction;

/**
 * @author IWareQ
 */
@Getter
public class SmithingTransformRecipe extends SmithingRecipe {

    protected final BiFunction<ItemStack, ItemStack, ItemStack> transformer;

    public SmithingTransformRecipe(Identifier identifier, ItemStack[] outputs, int priority, ItemDescriptor template, ItemDescriptor base, ItemDescriptor addition) {
        this(identifier, outputs, priority, template, base, addition, null);
    }

    public SmithingTransformRecipe(
            Identifier identifier, ItemStack[] outputs, int priority, ItemDescriptor template, ItemDescriptor base, ItemDescriptor addition, BiFunction<ItemStack, ItemStack, ItemStack> transformer) {
        super(identifier, outputs, priority, template, base, addition);
        this.transformer = transformer;
    }

    public ItemStack[] getOutputs(RecipeInput input) {
        if (outputs == null || outputs.length == 0) {
            return new ItemStack[0];
        }
        if (!(input instanceof SmithingRecipeInput smithingInput)) {
            return copyOutputs();
        }
        var base = smithingInput.base();
        var result = new ItemStack[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            var out = outputs[i].copy();
            if (transformer != null) {
                result[i] = transformer.apply(base, out);
            } else {
                result[i] = applyDefaultTransformation(base, out);
            }
        }
        return result;
    }

    public static ItemStack applyDefaultTransformation(ItemStack base, ItemStack output) {
        if (base == null || output == null) {
            return output;
        }

        if (base.hasEnchantments()) {
            output.addEnchantments(base.getEnchantments());
        }

        if (base.hasCustomName()) {
            output.setCustomName(base.getCustomName());
        }

        var lore = base.getLore();
        if (lore != null && !lore.isEmpty()) {
            output.setLore(new ArrayList<>(lore));
        }

        if (base.getDamage() > 0 && output.getMaxDamage() > 0) {
            output.setDamage(Math.min(base.getDamage(), output.getMaxDamage()));
        }

        if (base instanceof ItemTrimmableComponent baseTrim && output instanceof ItemTrimmableComponent outputTrim) {
            var pattern = baseTrim.getPattern();
            var material = baseTrim.getMaterial();
            if (pattern != null && material != null) {
                outputTrim.trim(pattern, material);
            }
        }

        return output;
    }

    private ItemStack[] copyOutputs() {
        var copy = new ItemStack[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            copy[i] = outputs[i].copy();
        }
        return copy;
    }
}