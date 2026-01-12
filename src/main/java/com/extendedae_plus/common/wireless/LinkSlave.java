package com.extendedae_plus.common.wireless;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.me.service.helpers.ConnectionWrapper;
import com.extendedae_plus.common.wireless.linkApi.ILinkHost;
import com.extendedae_plus.common.wireless.linkApi.ILinkListener;
import com.extendedae_plus.common.wireless.linkApi.Label;
import com.extendedae_plus.common.wireless.linkApi.RegistryLink;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class LinkSlave {
    private final ILinkHost host;
    private final ListenerSlave listener;

    private final ConnectionWrapper connection = new ConnectionWrapper(null);

    public LinkSlave(ILinkHost host) {
        this.host = Objects.requireNonNull(host, "host");
        this.listener = new ListenerSlave();
    }

    public void register() {
        if (checkInfo()) return;
        RegistryLink.registerListener(this.listener, this.host.getLabel());
    }

    public void unregister() {
        if (checkInfo()) return;
        RegistryLink.unregisterListener(this.listener, this.host.getLabel());
    }

    private void destroyConnection() {
        IGridNode nodeA;
        IGridNode nodeB = null;

        var connection = this.connection.getConnection();
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
        this.connection.setConnection(null);
        this.host.onConnectionChanged(false);
    }

    private boolean checkInfo() {
        return this.host.getLabel().data.isEmpty()
                || this.host.isRemoved()
                || this.host.getServerLevel() == null;
    }

    public void onUnloadOrRemove() {
        this.unregister();
        if (this.connected()) this.destroyConnection();
    }

    public boolean connected() {
        return this.connection.getConnection() != null;
    }

    public Label getLabel() {
        return this.host.getLabel();
    }

    public @Nullable UUID getPlacer() {
        return this.host.getPlacer();
    }

    public void updateInfo(Label label, @Nullable UUID placer) {
        if (this.host.getLabel().equals(label)
                && this.host.getPlacer() == placer) return;

        this.unregister();
        this.host.setLabel(label);
        this.host.setPlacer(placer);
        this.register();
    }

    public void setLabel(Label label) {
        if (this.host.getLabel().equals(label)) return;

        this.unregister();
        this.host.setLabel(label);
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
            if (LinkSlave.this.host.isRemoved() || master.isRemoved()) return;
            LinkSlave.this.destroyConnection();

            try {
                var nodeA = LinkSlave.this.host.getGridNode();
                var nodeB = master.getGridNode();
                if (nodeA == null || nodeB == null) return;

                var connection = LinkSlave.findConnection(nodeA, nodeB);
                if (connection == null)
                    connection = GridHelper.createConnection(nodeA, nodeB);
                LinkSlave.this.connection.setConnection(connection);
                LinkSlave.this.host.onConnectionChanged(true);
                master.onConnectionChanged(true);
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

        @Override
        public void emptyLabel() {
            LinkSlave.this.host.setLabel(Label.EMPTY);
        }
    }
}
