package com.extendedae_plus.network.helper

import net.neoforged.fml.loading.FMLEnvironment

enum class HandlersClient {
    EncodeFinished,
    SlotPatternHighlight,
    LabelList,
    ProvidersInfo,
//    ProviderPage

    ;

    operator fun invoke() = (impl.getOrNull(this.ordinal) ?: HolderHandler.Empty)()

    companion object {
        @Suppress("unchecked_cast")
        private val impl by lazy {
            if (FMLEnvironment.dist.isDedicatedServer) emptyArray()
            else Class.forName("com.extendedae_plus.network.helper.ImplHandlersClient")
                .enumConstants as Array<HolderHandler>
        }
    }
}