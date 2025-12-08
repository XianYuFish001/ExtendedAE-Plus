package com.extendedae_plus.client.screen;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.ClientDisplaySlot;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.widgets.ConfirmableTextField;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.me.items.PatternEncodingTermMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class ScreenStacksRename<TMenu extends PatternEncodingTermMenu>
        extends AESubScreen<TMenu, PatternEncodingTermScreen<TMenu>> {
    public static final String STYLE_PATH = "/screens/extendedae_plus/stacks_rename.json";

    private final ItemStack stack;
    private final Consumer<ItemStack> confirmer;

    private final ConfirmableTextField textField;

    public ScreenStacksRename(PatternEncodingTermScreen<TMenu> parent,
                              ItemStack stack,
                              Consumer<ItemStack> confirmer) {
        super(parent, STYLE_PATH);

        this.stack = stack;
        this.confirmer = confirmer;

        this.widgets.addButton("save", GuiText.Set.text(), this::confirm);
        this.widgets.add("back",
                new TabButton(Icon.BACK,
                        getMenu().getHost().getMainMenuIcon().getHoverName(),
                        btn -> returnToParent()
                )
        );

        var font = Minecraft.getInstance().font;
        var fieldStyle = this.getStyle().getWidget("field_stacks_rename");
        this.textField = new ConfirmableTextField(this.getStyle(),
                font,
                fieldStyle.getLeft() == null ? 0 : fieldStyle.getLeft(),
                fieldStyle.getTop() == null ? 0 : fieldStyle.getTop(),
                fieldStyle.getWidth(),
                fieldStyle.getHeight());
        this.textField.setBordered(false);
        this.textField.setMaxLength(50);
        this.textField.setTextColor(0xFFFFFF);
        this.textField.setSelectionColor(0xFF000080);
        this.textField.setVisible(true);
        this.textField.setOnConfirm(this::confirm);

        var bracketedName = stack.getDisplayName().getString();
        this.textField.setValue(bracketedName.substring(1, bracketedName.length() - 1));

        this.widgets.add("field_stacks_rename", this.textField);

        this.addClientSideSlot(new ClientDisplaySlot(GenericStack.fromItemStack(stack)),
                SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    protected void init() {
        super.init();

        this.setSlotsHidden(SlotSemantics.TOOLBOX, true);
    }

    private void confirm() {
        var value = this.textField.getValue();
        if (value.isBlank()) return;

        var newStack = this.stack.copy();
        newStack.set(DataComponents.CUSTOM_NAME, Component.literal(value));

        this.confirmer.accept(newStack);
        this.returnToParent();
    }

    @Override
    public void onClose() {
        this.returnToParent();
    }
}
