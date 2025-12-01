package com.extendedae_plus.common.init;

import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.item.priorityTool.HostPriorityTool;
import com.extendedae_plus.common.menu.MenuPriorityTool;
import com.extendedae_plus.common.menu.MenuProviderController;
import com.extendedae_plus.common.menu.MenuTicker;
import com.extendedae_plus.common.part.ticker.PartTicker;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU =
            DeferredRegister.create(Registries.MENU, ExtendedAEPlus.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MenuProviderController>> PROVIDER_CONTROLLER =
            MENU.register("provider_controller",
                    () -> IMenuTypeExtension.create(MenuProviderController::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuTicker>> TICKER =
            regAEMenu("ticker", MenuTicker::new, PartTicker.class);
//            MENU.register("ticker",
//                    () -> MenuTypeBuilder
//                            .create(MenuTicker::new, PartTicker.class)
//                            .build(ExtendedAEPlus.getLocation("ticker")));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuPriorityTool>> PRIORITY_TOOL =
            regAEMenu("priority_tool", MenuPriorityTool::new, HostPriorityTool.class);

    private static <TMenuType extends AEBaseMenu, THost> DeferredHolder<MenuType<?>, MenuType<TMenuType>>
    regAEMenu(String name,
              MenuTypeBuilder.MenuFactory<TMenuType, THost> factory,
              Class<THost> clazzHost,
              Consumer<MenuTypeBuilder<TMenuType, THost>> builderFunction) {
        var builder = MenuTypeBuilder.create(factory, clazzHost);
        builderFunction.accept(builder);
        return MENU.register(name, () -> builder.buildUnregistered(ExtendedAEPlus.getLocation(name)));
    }
    private static <TMenuType extends AEBaseMenu, THost> DeferredHolder<MenuType<?>, MenuType<TMenuType>>
    regAEMenu(String name,
              MenuTypeBuilder.MenuFactory<TMenuType, THost> factory,
              Class<THost> clazzHost) {
        return regAEMenu(name, factory, clazzHost, ignored -> {});
    }
}
