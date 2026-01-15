package com.extendedae_plus.client.render.widgets.button;

import appeng.client.gui.AEBaseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EAEPCycleButton extends EAEPButton {
    protected final List<EAEPActionItems> states;
    protected final IteratorState iteratorState;
    protected int stateIndex = 0;

    public EAEPCycleButton(List<EAEPActionItems> states,
                           BiConsumer<Integer, EAEPActionItems> statedOnPress,
                           @Nullable IteratorState iteratorState) {
        super(button -> {
            if (!(button instanceof EAEPCycleButton cycleButton)) return;
            var right = false;
            if (Minecraft.getInstance().screen instanceof AEBaseScreen<?> screen)
                right = screen.isHandlingRightClick();
            statedOnPress.accept(cycleButton.iterateState(right), cycleButton.getAction());
        });

        this.states = states;
        this.iteratorState = iteratorState;

        this.updateTooltip();
    }

    @Override
    public EAEPActionItems getAction() {
        if (this.states == null) return EAEPActionItems.BACKING_OUT;
        return this.states.get(this.stateIndex);
    }

    public void setStateIndex(int stateIndex) {
        this.setStateIndex(stateIndex, false);
    }

    public void setStateIndex(int stateIndex, boolean triggerEvent) {
        this.stateIndex = stateIndex;
        if (triggerEvent) this.onPress();
        else this.updateTooltip();
    }

    public int getStateIndex() {
        return this.stateIndex;
    }

    /// @return 被迭代过的stateIndex
    public int iterateState(boolean reversed) {
        if (this.iteratorState != null)
            this.stateIndex = this.iteratorState.iterate(this.stateIndex, reversed);
        else {
            var size = this.states.size();
            this.stateIndex = (this.stateIndex + (reversed ? -1 : 1) + size) % size;
        }
        return this.stateIndex;
    }

    public static final class Builder {
        private final List<EAEPActionItems> states = new ArrayList<>();
        private final List<Consumer<EAEPActionItems>> tasks = new ArrayList<>();
        private Consumer<EAEPActionItems> task = $ -> {};
        private IteratorState iteratorState = null;

        public Builder addPart(EAEPActionItems action) {
            return this.addPart(action, $ -> {});
        }

        public Builder addPart(EAEPActionItems action, CustomPacketPayload packet) {
            return this.addPart(action, () -> PacketDistributor.sendToServer(packet));
        }

        public Builder addPart(EAEPActionItems action, Runnable onPress) {
            return this.addPart(action, ignored -> onPress.run());
        }

        public Builder addPart(EAEPActionItems action, @Nullable Consumer<EAEPActionItems> onPress) {
            if (onPress == null) onPress = $ -> {};

            this.states.add(action);
            this.tasks.add(onPress);
            return this;
        }

        public Builder globalTask(Consumer<EAEPActionItems> task) {
            this.task = task;
            return this;
        }

        public Builder setIterator(IteratorState iteratorState) {
            this.iteratorState = iteratorState;
            return this;
        }

        public EAEPCycleButton build() {
            return new EAEPCycleButton(this.states,
                    (index, action) -> {
                        this.task.accept(action);
                        this.tasks.get(index).accept(action);
                    }, this.iteratorState);
        }
    }

    @FunctionalInterface
    public interface IteratorState {
        int iterate(int prev, boolean reversed);
    }
}
