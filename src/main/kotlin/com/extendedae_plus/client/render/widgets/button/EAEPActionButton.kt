package com.extendedae_plus.client.render.widgets.button

class EAEPActionButton(
    override val action: EAEPActionItems, onPress: (EAEPActionItems) -> Unit
) : EAEPButton(
    onPress@{ onPress(it.action ?: return@onPress) }
) {
    init {
        this.updateTooltip()
    }
}
