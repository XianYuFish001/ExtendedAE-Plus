package com.extendedae_plus.common.wireless;

import com.extendedae_plus.common.wireless.linkApi.ILinkHost;
import com.extendedae_plus.common.wireless.linkApi.LinkRegistry;
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

    public long getFrequency() { return this.host.getFrequency(); }

    public void updateInfo(long frequency, @Nullable UUID placer) {
        if (this.host.getFrequency() == frequency
                && this.host.getPlacer() == placer) return;

        this.unregister();
        this.host.setFrequency(frequency);
        this.host.setPlacer(placer);
        this.register();
    }

    public void setPlacer(@Nullable UUID placer) {
        this.host.setPlacer(placer);
    }

    public void setFrequency(long frequency) {
        // 如果频率发生变化，先撤销旧频率的注册
        if (this.host.getFrequency() != frequency) {
            if (this.registered) {
                unregister();
            }
            this.host.setFrequency(frequency);
        }

        // 频率未变的情况下也要校正注册状态：
        // - 当从"从端"切回"主端"时，registered 可能为 false，需要重新注册；
        // - 当频率为 0 或端点被移除时，确保处于未注册。
        if (frequency != 0L && !this.host.isEndpointRemoved()) {
            if (!this.registered) {
                register();
            }
        } else {
            if (this.registered) {
                unregister();
            }
        }
    }

    public boolean register() {
        ServerLevel level = this.host.getServerLevel();
        if (level == null || this.host.getFrequency() == 0L) return false;
        boolean succeed = LinkRegistry.registerMaster(this.host);
        this.registered = succeed;
        return succeed;
    }

    public void unregister() {
        ServerLevel level = this.host.getServerLevel();
        if (!this.registered || level == null || this.host.getFrequency() == 0L) return;
        LinkRegistry.unregisterMaster(this.host);
        this.registered = false;
    }

    public void onUnloadOrRemove() {
        unregister();
    }
}
