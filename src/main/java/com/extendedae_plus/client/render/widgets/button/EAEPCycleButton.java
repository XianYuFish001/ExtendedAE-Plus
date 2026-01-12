package com.extendedae_plus.client.render.widgets.button;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

public class EAEPCycleButton extends EAEPButton {
    protected final List<EAEPActionItems> states;
    protected final IntUnaryOperator stateIterator;
    protected int stateIndex = 0;

    public EAEPCycleButton(List<EAEPActionItems> states,
                           BiConsumer<Integer, EAEPActionItems> statedOnPress,
                           @Nullable IntUnaryOperator stateIterator) {
        super(button -> {
            if (!(button instanceof EAEPCycleButton cycleButton)) return;
            statedOnPress.accept(cycleButton.iterateState(), cycleButton.getAction());
        });

        this.states = states;
        this.stateIterator = stateIterator;

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
    public int iterateState() {
        if (this.stateIterator != null)
            this.stateIndex = this.stateIterator.applyAsInt(this.stateIndex);
        else this.stateIndex = (this.stateIndex + 1) % this.states.size();
        return this.stateIndex;
    }

    public static final class Builder {
        private final List<EAEPActionItems> states = new ArrayList<>();
        private final List<Consumer<EAEPActionItems>> tasks = new ArrayList<>();
        private IntUnaryOperator stateIterator = null;

        public Builder addPart(EAEPActionItems action) {
            return this.addPart(action, ignored -> {});
        }

        public Builder addPart(EAEPActionItems action, CustomPacketPayload packet) {
            return this.addPart(action, () -> PacketDistributor.sendToServer(packet));
        }

        public Builder addPart(EAEPActionItems action, Runnable onPress) {
            return this.addPart(action, ignored -> onPress.run());
        }

        public Builder addPart(EAEPActionItems action, @Nullable Consumer<EAEPActionItems> onPress) {
            if (onPress == null) onPress = ignored -> {};

            this.states.add(action);
            this.tasks.add(onPress);
            return this;
        }

        public Builder setIterator(IntUnaryOperator stateIterator) {
            this.stateIterator = stateIterator;
            return this;
        }

        public EAEPCycleButton build() {
            return new EAEPCycleButton(this.states,
                    (index, action) -> this.tasks.get(index).accept(action),
                    this.stateIterator);
        }
    }
}
