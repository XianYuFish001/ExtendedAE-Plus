package com.extendedae_plus.mixin.helper

import com.extendedae_plus.client.render.widgets.button.EAEPButton

/** 用于更新按钮布局, 在 `Provider & Interface` 的 `updateBeforeRender` 中调用 */
interface HelperProviderButtons {
    fun `eaep$updateButtonsStates`()

    fun `eaep$getButtons`(): MutableList<out EAEPButton>
}
