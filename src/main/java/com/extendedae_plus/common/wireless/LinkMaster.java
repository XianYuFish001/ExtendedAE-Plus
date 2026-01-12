package com.extendedae_plus.common.wireless;

import com.extendedae_plus.common.wireless.linkApi.ILinkHost;
import com.extendedae_plus.common.wireless.linkApi.Label;
import com.extendedae_plus.common.wireless.linkApi.RegistryLink;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 主收发器端逻辑：负责在频率变化/加载时向注册中心登记唯一主端，卸载时反注册。
 * 方块实体应在合适的生命周期中调用 register/unregister。
 */
public class LinkMaster {
    private final ILinkHost host;
    private boolean registered;

    public LinkMaster(ILinkHost host) {
        this.host = host;
    }

    public Label getLabel() {
        return this.host.getLabel();
    }

    public void updateInfo(Label label, @Nullable UUID placer) {
        if (this.host.getLabel().equals(label)
                && this.host.getPlacer() == placer) return;

        this.unregister();
        this.host.setLabel(label);
        this.host.setPlacer(placer);
        this.register();
    }

    public void setPlacer(@Nullable UUID placer) {
        this.host.setPlacer(placer);
    }

    public void setLabel(Label label) {
        if (this.host.getLabel().equals(label)) return;

        this.unregister();
        this.host.setLabel(label);
        this.register();
    }

    public boolean register() {
        ServerLevel level = this.host.getServerLevel();
        if (level == null || this.host.getLabel().data.isEmpty()) return false;
        boolean succeed = RegistryLink.registerMaster(this.host);
        this.registered = succeed;
        return succeed;
    }

    public void unregister() {
        ServerLevel level = this.host.getServerLevel();
        if (!this.registered || level == null || this.host.getLabel().data.isEmpty()) return;
        RegistryLink.unregisterMaster(this.host);
        this.registered = false;
    }

    public boolean connected() {
        return RegistryLink.countListener(this.host.getLabel()) > 0;
    }

    public void onUnloadOrRemove() {
        unregister();
    }
}
