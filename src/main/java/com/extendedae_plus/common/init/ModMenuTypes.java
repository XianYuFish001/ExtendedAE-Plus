package com.extendedae_plus.common.init;

import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.registry.menu.MenuPriorityTool;
import com.extendedae_plus.common.registry.menu.MenuProviderController;
import com.extendedae_plus.common.registry.menu.MenuTicker;
import com.extendedae_plus.common.registry.menu.host.HostPriorityTool;
import com.extendedae_plus.common.registry.menu.host.linkLabel.HostLabelLink;
import com.extendedae_plus.common.registry.menu.labelLink.MenuLabelLink;
import com.extendedae_plus.common.registry.part.ticker.PartTicker;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU =
            DeferredRegister.create(Registries.MENU, ExtendedAEPlus.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MenuProviderController>> providerController =
            MENU.register("provider_controller",
                    () -> IMenuTypeExtension.create(MenuProviderController::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuTicker>> ticker =
            regAEMenu("ticker", MenuTicker::new, PartTicker.class);
    public static final DeferredHolder<MenuType<?>, MenuType<MenuPriorityTool>> priorityTool =
            regAEMenu("priority_tool", MenuPriorityTool::new, HostPriorityTool.class);
    public static final DeferredHolder<MenuType<?>, MenuType<MenuLabelLink>> labelLink =
            regAEMenu("label_link", MenuLabelLink::new, HostLabelLink.class);

    public static final DeferredHolder<MenuType<?>, MenuType<MenuLabelLink>> labelLinkManageable =
            regAEMenu("label_link_manageable", (id, playerInventory, host) ->
                    new MenuLabelLink(id, playerInventory, host, true), HostLabelLink.class,
                    builder -> builder.withInitialData(
                            (host, buffer) -> {
                                buffer.writeBoolean(host.isLockable());
                                buffer.writeBoolean(host.isMasterable());
                            }, (host, menu, buffer) -> {
                                menu.setLockable(host.isLockable());
                                menu.setMasterable(host.isMasterable());
                            }
                    ));

    private static <TMenuType extends AEBaseMenu, THost> DeferredHolder<MenuType<?>, MenuType<TMenuType>>
    regAEMenu(String name,
              MenuTypeBuilder.MenuFactory<TMenuType, THost> factory,
              Class<THost> clazzHost,
              Consumer<MenuTypeBuilder<TMenuType, THost>> customizer) {
        var builder = MenuTypeBuilder.create(factory, clazzHost);
        customizer.accept(builder);
        return MENU.register(name, () -> builder.buildUnregistered(ExtendedAEPlus.getLocation(name)));
    }
    private static <TMenuType extends AEBaseMenu, THost> DeferredHolder<MenuType<?>, MenuType<TMenuType>>
    regAEMenu(String name,
              MenuTypeBuilder.MenuFactory<TMenuType, THost> factory,
              Class<THost> clazzHost) {
        return regAEMenu(name, factory, clazzHost, $ -> {});
    }
}
