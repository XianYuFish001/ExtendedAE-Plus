package com.extendedae_plus.mixin.impl.bridge;

import com.extendedae_plus.client.render.widgets.button.EAEPActionButton;

import java.util.List;

/// 用于更新按钮布局, 在 `Provider & Interface` 的 `updateBeforeRender` 中调用
public interface HelperProviderButtons {
    void eaep$updateButtonsStates();

    List<EAEPActionButton> eaep$getScalingButtons();
}
