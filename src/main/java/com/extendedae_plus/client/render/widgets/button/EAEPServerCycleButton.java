package com.extendedae_plus.client.render.widgets.button;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EAEPServerCycleButton extends EAEPCycleButton {
    private final Supplier<Integer> syncerState;

    public EAEPServerCycleButton(List<EAEPActionItems> states,
                                 Consumer<EAEPActionItems> taskSingle,
                                 @Nullable IteratorState iteratorState,
                                 Supplier<Integer> syncerState) {
        super(states, (index, action) -> taskSingle.accept(action), iteratorState);
        this.syncerState = syncerState;
    }

    @Override
    public int iterateState(boolean reversed) {
        super.iterateState(reversed);
        if (this.stateIndex != this.syncerState.get())
            this.setStateIndex(this.syncerState.get());
        return this.stateIndex;
    }

    public void updateState() {
        this.setStateIndex(this.syncerState.get());
    }

    public static final class Builder {
        private final List<EAEPActionItems> states = new ArrayList<>();
        private Consumer<EAEPActionItems> task = ignored -> {};
        private IteratorState iteratorState = null;
        private Supplier<Integer> syncerState = () -> 0;

        public Builder setTask(CustomPacketPayload task) {
            return this.setTask(ignored -> PacketDistributor.sendToServer(task));
        }

        public Builder setTask(Runnable task) {
            return this.setTask(ignored -> task.run());
        }

        public Builder setTask(Consumer<EAEPActionItems> task) {
            this.task = task;
            return this;
        }

        public Builder addPart(EAEPActionItems action) {
            this.states.add(action);
            return this;
        }

        public Builder setIterator(IteratorState iteratorState) {
            this.iteratorState = iteratorState;
            return this;
        }

        public Builder setSyncer(Supplier<Integer> syncer) {
            this.syncerState = syncer;
            return this;
        }

        public Builder setSyncer(SyncerBooleanGeneric syncer) {
            this.syncerState = syncer;
            return this;
        }

        public <TEnum extends Enum<TEnum>> Builder setSyncer(SyncerEnumGeneric<TEnum> syncer) {
            this.syncerState = syncer;
            return this;
        }

        public EAEPServerCycleButton build() {
            return new EAEPServerCycleButton(this.states, this.task, this.iteratorState, this.syncerState);
        }
    }

    @FunctionalInterface
    public interface SyncerBooleanGeneric extends Supplier<Integer> {
        boolean getState();

        default Integer get() {
            return this.getState() ? 1 : 0;
        }
    }

    @FunctionalInterface
    public interface SyncerEnumGeneric<TEnum extends Enum<TEnum>> extends Supplier<Integer> {
        TEnum getState();

        default Integer get() {
            return this.getState().ordinal();
        }
    }
}
