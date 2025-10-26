package com.extendedae_plus.integration.recipeViewer.emi.emiIntegrations.converter;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import com.extendedae_plus.integration.recipeViewer.emi.emiIntegrations.EmiStackConverter;
import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class EmiFluidStackConverter implements EmiStackConverter {
    @Override
    public Class<?> getKeyType() {
        return Fluid.class;
    }

    @Override
    public @Nullable EmiStack toEmiStack(GenericStack stack) {
        if (stack.what() instanceof AEFluidKey fluidKey) {
            return ForgeEmiStack.of(fluidKey.toStack(1)).setAmount(stack.amount());
        }
        return null;
    }

    @Override
    public @Nullable GenericStack toGenericStack(EmiStack stack) {
        var fluid = stack.getKeyOfType(Fluid.class);
        if (fluid != null && fluid != Fluids.EMPTY) {
            var fluidStack = new FluidStack(fluid.defaultFluidState().getType(), 1, stack.getNbt());
            var fluidKey = AEFluidKey.of(fluidStack);
            return new GenericStack(fluidKey, stack.getAmount());
        }
        return null;
    }
}
