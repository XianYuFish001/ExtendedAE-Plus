package com.extendedae_plus.common.wireless.linkApi

interface IBlockEntityLabel {
    fun setLabel(label: Label, force: Boolean = false)

    val label: Label
}
