package com.extendedae_plus.mixin.helper

/**
 * 由 GuiExPatternProviderMixin 实现，用于在客户端侧提供当前页号，避免反射读取 AE2 内部字段失败。
 */
interface ExPatternPageAccessor {
    fun `eap$getCurrentPage`(): Int
}
