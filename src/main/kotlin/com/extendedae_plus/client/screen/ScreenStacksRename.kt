package com.extendedae_plus.client.screen

import appeng.api.stacks.GenericStack
import appeng.client.gui.AESubScreen
import appeng.client.gui.Icon
import appeng.client.gui.me.common.ClientDisplaySlot
import appeng.client.gui.me.items.PatternEncodingTermScreen
import appeng.client.gui.widgets.ConfirmableTextField
import appeng.client.gui.widgets.TabButton
import appeng.core.localization.GuiText
import appeng.menu.SlotSemantics
import appeng.menu.me.items.PatternEncodingTermMenu
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW

class ScreenStacksRename<TMenu : PatternEncodingTermMenu>(
    parent: PatternEncodingTermScreen<TMenu>,
    private val stack: ItemStack,
    private val confirmer: (ItemStack) -> Unit
) : AESubScreen<TMenu, PatternEncodingTermScreen<TMenu>>(parent, PATH_STYLE) {
    private val fieldRename: ConfirmableTextField

    init {
        this.widgets.addButton("save", GuiText.Set.text(), this::confirm)
        this.widgets.add(
            "back",
            TabButton(
                Icon.BACK,
                this.menu.host.mainMenuIcon.hoverName
            ) { this.returnToParent() }
        )

        val font = Minecraft.getInstance().font
        val fieldStyle = this.style.getWidget("field_stacks_rename")
        this.fieldRename = ConfirmableTextField(
            this.style,
            font,
            fieldStyle.left ?: 0,
            fieldStyle.top ?: 0,
            fieldStyle.width,
            fieldStyle.height
        )
        this.fieldRename.isBordered = false
        this.fieldRename.setMaxLength(50)
        this.fieldRename.setTextColor(0xFFFFFF)
        this.fieldRename.setSelectionColor(-0xffff80)
        this.fieldRename.isVisible = true
        this.fieldRename.setOnConfirm(this::confirm)
        this.fieldRename.value = stack.hoverName.string
        this.fieldRename.placeholder = stack.item.getName(stack)
        this.widgets.add("field_stacks_rename", this.fieldRename)

        this.addClientSideSlot(
            ClientDisplaySlot(GenericStack.fromItemStack(stack)),
            SlotSemantics.MACHINE_OUTPUT
        )
    }

    override fun init() {
        super.init()
        this.setInitialFocus(this.fieldRename)
        this.setSlotsHidden(SlotSemantics.TOOLBOX, true)
    }

    override fun mouseClicked(xCoord: Double, yCoord: Double, button: Int): Boolean {
        if (button != GLFW.GLFW_MOUSE_BUTTON_RIGHT
            || !this.fieldRename.isMouseOver(xCoord, yCoord)
        ) return super.mouseClicked(xCoord, yCoord, button)
        this.fieldRename.value = ""
        this.setFocused(this.fieldRename)
        return true
    }

    private fun confirm() {
        val newStack = this.stack.copy()

        val name = this.fieldRename.value
        if (!(name.isBlank()
                    || name == newStack.getOrDefault(DataComponents.ITEM_NAME, Component.empty()).string
                    || name == newStack.item.getName(newStack).string)
        ) {
            newStack.set(DataComponents.CUSTOM_NAME, Component.literal(name))
        } else newStack.remove(DataComponents.CUSTOM_NAME)

        this.confirmer(newStack)
        this.returnToParent()
    }

    override fun onClose() = this.returnToParent()

    companion object {
        const val PATH_STYLE = "/screens/extendedae_plus/stacks_rename.json"
    }
}
