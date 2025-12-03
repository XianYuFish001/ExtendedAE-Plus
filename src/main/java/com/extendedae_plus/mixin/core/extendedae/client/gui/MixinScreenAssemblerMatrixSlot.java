package com.extendedae_plus.mixin.core.extendedae.client.gui;

import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Mixin(targets = "com.glodblock.github.extendedae.client.gui.GuiAssemblerMatrix$PatternInfo")
public class MixinScreenAssemblerMatrixSlot {
    @Shadow
    @Final
    private List<Object> internalRows;

    @Unique
    private Constructor<?> eaep$constructorPatternRow = null;

    @Unique
    private Object eaep$newPatternRow(long patternID, int offset, int slots) {
        if (this.eaep$constructorPatternRow == null) {
            try {
                Class<?> clazzPatternRow = Class.forName("com.glodblock.github.extendedae.client.gui.GuiAssemblerMatrix$PatternRow");
                this.eaep$constructorPatternRow = clazzPatternRow.getDeclaredConstructor(long.class, int.class, int.class);
                this.eaep$constructorPatternRow.setAccessible(true);
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            }
        }
        try {
            return this.eaep$constructorPatternRow.newInstance(patternID, offset, slots);
        } catch (NullPointerException
                 | InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException exception) {
            return null;
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(long id, CallbackInfo ci) {

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var blockEntity = level.getBlockEntity(BlockPos.of(id));
        if (!(blockEntity instanceof TileAssemblerMatrixPattern corePattern)) return;

        int slotSize = corePattern.getTerminalPatternInventory().size();
        if (slotSize < TileAssemblerMatrixPattern.INV_SIZE) return;

        this.internalRows.clear();

        int left = slotSize;
        int offset = 0;
        do {
            this.internalRows.add(this.eaep$newPatternRow(id, offset, Math.min(left, 9)));
            left -= 9;
            offset += 9;
        } while (left > 0);
    }
}
