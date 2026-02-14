package com.extendedae_plus.util.keyBuilder

enum class Patterns(override val pattern: String) : IKeyPattern {
    creativeTab("creative_tab.%s%s"),
    tooltip("tooltip.%s%s"),
    screenTooltip("tooltip.screen.%s%s"),
    message("message.%s%s"),
    actionBar("message.actionbar.%s%s"),
    screen("screen.%s%s"),
    keywordGroup("keywordGroup.%s%s"),
    config("%s.configuration%s"),
    key("key.%s%s"),
    keyCategory("key.category.%s%s"),
    viewerInfo("recipe_viewer.info.%s%s"),
    viewerTooltip("recipe_viewer.tooltip.%s%s"),
    viewerCategory("recipe_viewer.category.%s%s"),
    jadeInfo("jade.info.%s%s"),
    jadeConfig("config.jade.plugin_%s%s"),
    ;
}

interface IKeyPattern {
    val pattern: String
}

fun String.toKeyPattern() = object : IKeyPattern {
    override val pattern = this@toKeyPattern
}