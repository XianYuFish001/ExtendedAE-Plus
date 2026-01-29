package com.extendedae_plus.common.registry.menu.labelLink;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import com.extendedae_plus.common.init.ModMenuTypes;
import com.extendedae_plus.common.registry.menu.host.linkLabel.HostLabelLink;
import com.extendedae_plus.common.wireless.linkApi.Label;
import com.extendedae_plus.common.wireless.linkApi.RegistryLink;
import com.extendedae_plus.network.SPacketLabelList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.util.List;
import java.util.function.Consumer;

public class MenuLabelLink extends AEBaseMenu {
    public static final Consumer<MenuTypeBuilder<MenuLabelLink, HostLabelLink>> dataManagementSerializer =
            builder -> builder.withInitialData(
                    (host, buffer) -> {
                        buffer.writeBoolean(host.isLockable());
                        buffer.writeBoolean(host.isMasterable());
                    }, (host, menu, buffer) -> {
                        menu.setLockable(host.isLockable());
                        menu.setMasterable(host.isMasterable());
                    }
            );

    private static final String ACTION_SELECT = "select";
    private static final String ACTION_ADD = "add";
    private static final String ACTION_REMOVE = "remove";
    private static final String ACTION_LOCK = "lock";
    private static final String ACTION_MASTER = "master";

    private final HostLabelLink host;

    @GuiSync(101)
    private Label.Data selectedLabel;
    @GuiSync(102)
    private boolean locked;
    @GuiSync(103)
    private boolean master;

    // Server
    private List<LabelMapped> labels;

    // Client
    private boolean lockable;
    private boolean masterable;

    public MenuLabelLink(int id, Inventory playerInventory, HostLabelLink host) {
        this(id, playerInventory, host, false);
    }

    public MenuLabelLink(int id, Inventory playerInv, HostLabelLink host, boolean manageable) {
        this(manageable ? ModMenuTypes.labelLinkManageable.get() : ModMenuTypes.labelLink.get(), id, playerInv, host);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (this.isClientSide()) return;
        SPacketLabelList.send(this);
    }

    public MenuLabelLink(MenuType<?> menuType,
                         int id,
                         Inventory playerInventory,
                         HostLabelLink host) {
        super(menuType, id, playerInventory, host);
        this.host = host;
        this.selectedLabel = host.getLabelData();
        this.locked = host.isLocked();
        this.master = host.isMaster();

        this.registerClientAction(ACTION_SELECT, Integer.class, this::selectLabel);
        this.registerClientAction(ACTION_ADD, Label.Data.class, this::registerLabel);
        this.registerClientAction(ACTION_REMOVE, Integer.class, this::unregisterLabel);
        this.registerClientAction(ACTION_LOCK, this::toggleLock);
        this.registerClientAction(ACTION_MASTER, this::toggleMaster);
    }

    public void selectLabel(int serial) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SELECT, serial);
            return;
        }

        this.labels.stream()
                .filter(labelMapped -> labelMapped.serial == serial)
                .findAny()
                .map(LabelMapped::data)
                .ifPresent(label -> {
                    if (this.host.setLabelData(label, false))
                        this.selectedLabel = label;
                });
        this.getPlayer().closeContainer();
    }

    public void registerLabel(Label.Data data) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_ADD, data);
            return;
        }

        data.pack();
        if (this.host.setLabelData(data, false))
            this.selectedLabel = data;
        this.broadcastChanges();
    }

    public void unregisterLabel(int serial) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_REMOVE, serial);
            return;
        }

        this.labels.stream()
                .filter(labelMapped -> labelMapped.serial == serial)
                .findAny()
                .map(LabelMapped::data)
                .ifPresent(label -> {
                    if (label.equals(this.host.getLabelData())) {
                        this.host.setLabelData(Label.Data.EMPTY, true);
                        this.selectedLabel = Label.Data.EMPTY;
                    }

                    RegistryLink.removeLabel(label);
                    this.broadcastChanges();
                });
    }

    public void toggleLock() {
        if (this.isClientSide()) {
            if (!this.lockable) return;
            this.sendClientAction(ACTION_LOCK);
            return;
        }
        this.host.toggleLock();
        this.locked = this.host.isLocked();
    }

    public void toggleMaster() {
        if (this.isClientSide()) {
            if (!this.masterable) return;
            this.sendClientAction(ACTION_MASTER);
            return;
        }
        this.host.toggleMaster();
        this.master = this.host.isMaster();
    }

    public Label.Data getSelectedLabel() {
        return this.selectedLabel;
    }

    public void setLabels(List<LabelMapped> labels) {
        this.labels = labels;
    }

    public boolean isLockable() {
        return lockable;
    }

    public void setLockable(boolean lockable) {
        this.lockable = lockable;
    }

    public boolean isMasterable() {
        return masterable;
    }

    public void setMasterable(boolean masterable) {
        this.masterable = masterable;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isMaster() {
        return master;
    }

    public record LabelMapped(int serial, Label.Data data) {
        public static final StreamCodec<RegistryFriendlyByteBuf, LabelMapped> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.INT, LabelMapped::serial,
                        Label.Data.STREAM_CODEC, LabelMapped::data,
                        LabelMapped::new);
    }
}
