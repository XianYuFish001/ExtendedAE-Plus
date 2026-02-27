package com.extendedae_plus.mixin

import com.fish.fishlib.mixin.DiscoverDependencies
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class EAEPPluginMixin : IMixinConfigPlugin {
    override fun onLoad(mixinPackage: String) = Unit

    override fun getRefMapperConfig() = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String) =
        DiscoverDependencies.check(mixinClassName)

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) = Unit

    override fun getMixins() = null

    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    )  = Unit

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) = Unit
}