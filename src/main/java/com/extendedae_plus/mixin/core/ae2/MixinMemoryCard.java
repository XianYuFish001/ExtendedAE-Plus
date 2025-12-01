package com.extendedae_plus.mixin.core.ae2;

import appeng.api.util.IConfigurableObject;
import appeng.items.tools.MemoryCardItem;
import com.extendedae_plus.mixin.impl.ProviderSettingsImplementations;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(MemoryCardItem.class)
public class MixinMemoryCard {
    @Inject(method = "importGenericSettings", at = @At("HEAD"))
    private static void markSilent(Object importTo,
                                   DataComponentMap input,
                                   @Nullable Player player,
                                   CallbackInfoReturnable<Set<DataComponentType<?>>> cir) {
        if (!(importTo instanceof IConfigurableObject object)) return;
        ProviderSettingsImplementations.flagChanging.add(object.getConfigManager());
    }

    @Inject(method = "importGenericSettings", at = @At("TAIL"))
    private static void removeFlag(Object importTo,
                                   DataComponentMap input,
                                   @Nullable Player player,
                                   CallbackInfoReturnable<Set<DataComponentType<?>>> cir) {
        if (!(importTo instanceof IConfigurableObject object)) return;
        ProviderSettingsImplementations.flagChanging.add(object.getConfigManager());
    }
}
