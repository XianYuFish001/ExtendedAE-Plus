@file:Suppress("FunctionName")

package com.extendedae_plus.util

import net.neoforged.fml.loading.FMLEnvironment

interface UtilClient {
    fun _shift() = false

    fun _ctrl() = false

    fun _alt() = false

    companion object {
        private val instance: UtilClient by lazy {
            if (FMLEnvironment.dist.isDedicatedServer) object : UtilClient {}
            else Class.forName("com.extendedae_plus.client.impl.ImplClientOnly")
                .kotlin
                .objectInstance
                    as UtilClient
        }

        @JvmStatic
        fun shift() = this.instance._shift()

        @JvmStatic
        fun ctrl() = this.instance._ctrl()

        @JvmStatic
        fun alt() = this.instance._alt()
    }
}