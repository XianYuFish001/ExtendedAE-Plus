package com.extendedae_plus.common.wireless.linkApi

interface ILinkListener {
    fun onMasterAvailable(master: ILinkHost)

    fun onMasterUnavailable(master: ILinkHost)

    fun onListenerRemoved()

    fun emptyLabel()
}
