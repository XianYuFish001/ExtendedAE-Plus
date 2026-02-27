package com.extendedae_plus.common.init

import appeng.menu.AEBaseMenu
import appeng.menu.implementations.MenuTypeBuilder
import appeng.menu.implementations.MenuTypeBuilder.MenuFactory
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.ExtendedAEPlus.Companion.getLocation
import com.extendedae_plus.common.registry.menu.MenuPriorityTool
import com.extendedae_plus.common.registry.menu.MenuProviderController
import com.extendedae_plus.common.registry.menu.MenuTicker
import com.extendedae_plus.common.registry.menu.host.HostPriorityTool
import com.extendedae_plus.common.registry.menu.host.link.HostLabelLink
import com.extendedae_plus.common.registry.menu.labelLink.MenuLabelLink
import com.extendedae_plus.common.registry.part.ticker.PartTicker
import com.fish.fishlib.common.InitObject
import net.minecraft.core.registries.Registries
import net.minecraft.world.inventory.MenuType
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object EAEPMenuTypes {
    @InitObject
    val Register: DeferredRegister<MenuType<*>> = DeferredRegister.create(Registries.MENU, ExtendedAEPlus.MODID)

    @JvmField
    val ControllerProvider: DeferredHolder<MenuType<*>, MenuType<MenuProviderController>> = Register.register(
        "provider_controller"
    ) { -> IMenuTypeExtension.create(::MenuProviderController) }

    @JvmField
    val Ticker = this.regAEMenu(
        "ticker",
        ::MenuTicker,
        PartTicker::class.java
    )

    @JvmField
    val PriorityTool = this.regAEMenu(
        "priority_tool",
        ::MenuPriorityTool,
        HostPriorityTool::class.java
    )

    @JvmField
    val LabelLink = this.regAEMenu(
        "label_link",
        ::MenuLabelLink,
        HostLabelLink::class.java
    )

    @JvmField
    val LabelLinkManageable = this.regAEMenu(
        "label_link_manageable",
        { id, playerInventory, host -> MenuLabelLink(id, playerInventory, host, true) },
        HostLabelLink::class.java,
        MenuLabelLink.DataManagementSerializer
    )

    private inline fun <TMenuType : AEBaseMenu, THost> regAEMenu(
        name: String,
        factory: MenuFactory<TMenuType, THost>,
        clazzHost: Class<THost>,
        customizer: (MenuTypeBuilder<TMenuType, THost>) -> Unit
    ): DeferredHolder<MenuType<*>, MenuType<TMenuType>> {
        val builder = MenuTypeBuilder.create(factory, clazzHost)
        customizer(builder)
        return Register.register(name) { -> builder.buildUnregistered(getLocation(name)) }
    }

    private fun <TMenuType : AEBaseMenu, THost> regAEMenu(
        name: String,
        factory: MenuFactory<TMenuType, THost>,
        clazzHost: Class<THost>
    ) = this.regAEMenu(name, factory, clazzHost) { }
}
