package com.extendedae_plus.common.wireless;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.me.service.helpers.ConnectionWrapper;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.wireless.linkApi.ILinkHost;
import com.extendedae_plus.common.wireless.linkApi.ILinkListener;
import com.extendedae_plus.common.wireless.linkApi.LinkRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class LinkSlave {
    private final ILinkHost host;
    private final ListenerSlave listener;

    private ConnectionWrapper wrapper = new ConnectionWrapper(null);

    public LinkSlave(ILinkHost host) {
        this.host = Objects.requireNonNull(host, "host");
        this.listener = new ListenerSlave();
    }

    public void register() {
        if (checkInfo()) return;
        LinkRegistry.registerListener(this.listener, this.host.getServerLevel(),
                this.host.getFrequency(), this.host.getPlacer());
    }

    public void unregister() {
        if (checkInfo()) return;
        LinkRegistry.unregisterListener(this.listener, this.host.getServerLevel(),
                this.host.getFrequency(), this.host.getPlacer());
    }

    private void destroyConnection() {
        IGridNode nodeA;
        IGridNode nodeB = null;

        var connection = this.wrapper.getConnection();
        if (connection != null) {
            nodeA = connection.a();
            nodeB = connection.b();
            connection.destroy();
        } else {
            nodeA = this.host.getGridNode();
            if (nodeA == null) return;

            for (IGridConnection hostConnection : nodeA.getConnections()) {
                if (hostConnection == null || hostConnection.isInWorld()) continue;
                var uncheckedNodeB = hostConnection.getOtherSide(nodeA);
                if (uncheckedNodeB.getOwner() instanceof ILinkHost) {
                    hostConnection.destroy();
                    nodeB = uncheckedNodeB;
                }
            }
        }
        try {
            if (nodeA != null && nodeA.getGrid() != null) {
                nodeA.getGrid().getTickManager().wakeDevice(nodeA);
            }
        } catch (Throwable ignored) {
        }
        try {
            if (nodeB != null && nodeB.getGrid() != null) {
                nodeB.getGrid().getTickManager().wakeDevice(nodeB);
            }
        } catch (Throwable ignored) {
        }
        this.wrapper.setConnection(null);
        this.host.updateBlockState();
    }

    private boolean checkInfo() {
        return this.host.getFrequency() <= 0L || this.host.isEndpointRemoved() || this.host.getServerLevel() == null;
    }

    public void onUnloadOrRemove() {
        this.unregister();
        if (this.connected()) this.destroyConnection();
    }

    public boolean connected() {
        return this.wrapper.getConnection() != null;
    }

    public long getFrequency() {
        return this.host.getFrequency();
    }

    public @Nullable UUID getPlacer() {
        return this.host.getPlacer();
    }

    public void updateInfo(long frequency, @Nullable UUID placer) {
        if (this.host.getFrequency() == frequency
                && this.host.getPlacer() == placer) return;

        this.unregister();
        this.host.setFrequency(frequency);
        this.host.setPlacer(placer);
        this.register();
    }

    public void setFrequency(long frequency) {
        if (this.host.getFrequency() == frequency) return;

        this.unregister();
        this.host.setFrequency(frequency);
        this.register();
    }

    public void setPlacer(@Nullable UUID placer) {
        if (placer != null && placer.equals(this.host.getPlacer())) return;

        this.unregister();
        this.host.setPlacer(placer);
        this.register();
    }

    private static IGridConnection findConnection(IGridNode nodeA, IGridNode nodeB) {
        try {
            for (var connection : nodeA.getConnections()) {
                var connectionNodeA = connection.a();
                var connectionNodeB = connection.b();
                if ((connectionNodeA == nodeA || connectionNodeB == nodeA)
                        && (connectionNodeA == nodeB || connectionNodeB == nodeB))
                    return connection;
            }
        } catch (Throwable ignore) {
        }
        return null;
    }

    private class ListenerSlave implements ILinkListener {
        @Override
        public void onMasterAvailable(ILinkHost master) {
            if (LinkSlave.this.host.isEndpointRemoved() || master.isEndpointRemoved()) return;
            var distance = Math.sqrt(master.getBlockPos().distSqr(LinkSlave.this.host.getBlockPos()));
            if (!EAEPConfig.WIRELESS_CROSS_DIM_ENABLE.getAsBoolean()
                    && distance > EAEPConfig.WIRELESS_MAX_RANGE.getAsDouble()) return;

            LinkSlave.this.destroyConnection();

            try {
                var nodeA = LinkSlave.this.host.getGridNode();
                var nodeB = master.getGridNode();
                if (nodeA == null || nodeB == null) return;

                var connection = LinkSlave.findConnection(nodeA, nodeB);
                if (connection == null)
                    connection = GridHelper.createConnection(nodeA, nodeB);
                LinkSlave.this.wrapper.setConnection(connection);
                LinkSlave.this.host.updateBlockState();
            } catch (Throwable ignore) {
            }
        }

        @Override
        public void onMasterUnavailable(ILinkHost master) {
            LinkSlave.this.destroyConnection();
        }

        @Override
        public void onListenerRemoved() {
            LinkSlave.this.destroyConnection();
        }
    }
}
