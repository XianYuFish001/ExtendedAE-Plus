package com.extendedae_plus.common.init;

import appeng.menu.implementations.MenuTypeBuilder;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.menu.MenuProviderController;
import com.extendedae_plus.common.menu.MenuTicker;
import com.extendedae_plus.common.part.ticker.PartTicker;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU =
            DeferredRegister.create(Registries.MENU, ExtendedAEPlus.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MenuProviderController>> PROVIDER_CONTROLLER =
            MENU.register("provider_controller",
                    () -> IMenuTypeExtension.create(MenuProviderController::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuTicker>> TICKER =
            MENU.register("ticker",
                    () -> MenuTypeBuilder
                            .create(MenuTicker::new, PartTicker.class)
                            .build("ticker"));
}
