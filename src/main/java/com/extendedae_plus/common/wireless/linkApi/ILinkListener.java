package com.extendedae_plus.common.wireless.linkApi;

public interface ILinkListener {
    void onMasterAvailable(ILinkHost master);

    void onMasterUnavailable(ILinkHost master);

    void onListenerRemoved();
}
