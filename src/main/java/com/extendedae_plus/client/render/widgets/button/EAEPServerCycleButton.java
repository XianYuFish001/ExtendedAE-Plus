package com.extendedae_plus.client.render.widgets.button;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

public class EAEPServerCycleButton extends EAEPCycleButton {
    private final Supplier<Integer> getterSyncedStateIndex;

    public EAEPServerCycleButton(List<EAEPActionItems> states,
                                 Consumer<EAEPActionItems> singleOnPress,
                                 @Nullable IntUnaryOperator stateIterator,
                                 Supplier<Integer> getterSyncedStateIndex) {
        super(states, (index, action) ->
                        singleOnPress.accept(action), stateIterator);
        this.getterSyncedStateIndex = getterSyncedStateIndex;
    }

    @Override
    public int iterateState() {
        super.iterateState();
        if (this.stateIndex != this.getterSyncedStateIndex.get())
            this.setStateIndex(this.getterSyncedStateIndex.get());
        return this.stateIndex;
    }

    public void updateState() {
        this.setStateIndex(this.getterSyncedStateIndex.get());
    }

    public static final class Builder {
        private final List<EAEPActionItems> states = new ArrayList<>();
        private Consumer<EAEPActionItems> task = ignored -> {};
        private IntUnaryOperator stateIterator = null;
        private Supplier<Integer> getterSyncedStateIndex = () -> 0;

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

        public Builder setIterator(IntUnaryOperator stateIterator) {
            this.stateIterator = stateIterator;
            return this;
        }

        public Builder setSyncedStateGetter(Supplier<Integer> getter) {
            this.getterSyncedStateIndex = getter;
            return this;
        }

        public Builder setSyncedStateGetter(SyncerGenericBooleanState getter) {
            this.getterSyncedStateIndex = getter;
            return this;
        }

        public <TEnum extends Enum<TEnum>> Builder
        setSyncedStateGetter(SyncerGenericEnumState<TEnum> getter) {
            this.getterSyncedStateIndex = getter;
            return this;
        }

        public EAEPServerCycleButton build() {
            return new EAEPServerCycleButton(this.states, this.task, this.stateIterator, this.getterSyncedStateIndex);
        }
    }

    @FunctionalInterface
    public interface SyncerGenericBooleanState extends Supplier<Integer> {
        boolean getState();

        default Integer get() {
            return this.getState() ? 1 : 0;
        }
    }

    @FunctionalInterface
    public interface SyncerGenericEnumState<TEnum extends Enum<TEnum>> extends Supplier<Integer> {
        TEnum getState();

        default Integer get() {
            return this.getState().ordinal();
        }
    }
}
