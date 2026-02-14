package com.extendedae_plus.util.keyBuilder

import com.extendedae_plus.ExtendedAEPlus
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.material.Fluid
import net.neoforged.neoforge.data.loading.DatagenModLoader
import net.neoforged.neoforge.fluids.FluidType
import net.neoforged.neoforge.registries.DeferredHolder
import java.util.*

private typealias Adder = (Component) -> Any?
private typealias BiAdder<T> = (T, Component) -> Any?
private typealias Customizer<T> = (T) -> T

object UtilKeyBuilder {
    @JvmStatic
    fun of(pattern: IKeyPattern): BuilderGeneric<*> {
        val builder = BuilderGeneric(null)
        builder.pattern = pattern
        return builder
    }

    @JvmStatic
    fun of(holder: DeferredHolder<*, *>): BuilderGeneric<*> {
        val builder = BuilderGeneric(null)
        this.applyMain(builder, holder)
        return builder
    }

    @JvmStatic
    fun dataGen(pattern: IKeyPattern): BuilderDataGen {
        ContainerDataGen.checkEnv()
        val builder = BuilderDataGen(null, false)
        builder.pattern = pattern
        return builder
    }

    @JvmStatic
    fun dataGen(holder: DeferredHolder<*, *>): BuilderDataGen {
        ContainerDataGen.checkEnv()
        val builder = BuilderDataGen(null, false)
        this.applyMain(builder, holder)
        return builder
    }

    private fun applyMain(builder: BuilderGeneric<*>, item: Any): String {
        builder.keyMain = when (item) {
            is Item -> item.asItem().descriptionId
            is FluidType -> item.descriptionId
            is Fluid -> applyMain(builder, item.fluidType)
            is DeferredHolder<*, *> -> applyMain(builder, item.get())
            else -> throw IllegalArgumentException("Unsupported value type ${item.javaClass.name}")
        }
        return builder.keyMain
    }
}

open class BuilderGeneric<TBuilder : BuilderGeneric<TBuilder>> internal constructor(
    original: BuilderGeneric<TBuilder>?
) {
    internal var pattern = "%s%s".toKeyPattern()
    internal var keyMain = ExtendedAEPlus.MODID
    internal var keyAdditional = ""
    internal var args: Array<Any>? = null

    init {
        this.copyFrom(original)
    }

    protected fun copyFrom(original: BuilderGeneric<TBuilder>?) {
        if (original == null) return
        this.pattern = original.pattern
        this.keyMain = original.keyMain
        this.keyAdditional = original.keyAdditional
        this.args = original.args
    }

    @Suppress("unchecked_cast")
    internal fun cast() = this as TBuilder

    @Suppress("unchecked_cast")
    internal fun <T> cast() = this as T

    // TODO Check
    fun bindAdder(adder: Adder): BuilderAdder<*> {
        class Simple : BuilderAdder<Simple>(
            this.cast<BuilderGeneric<Simple>>(),
            adder,
            true
        )
        return Simple()
    }

    init {
        this.bindAdder {}
    }

    fun <V> bindBiAdder(biAdder: BiAdder<V>): BuilderBiAdder<*, V> {
        class Simple : BuilderBiAdder<Simple, V>(
            this.cast<BuilderGeneric<Simple>>(),
            biAdder,
            true
        )
        return Simple()
    }

    fun bindCollection(target: MutableCollection<Component>): BuilderAdder<*> {
        class Simple : BuilderAdder<Simple>(
            this.cast<BuilderGeneric<Simple>>(),
            target::add,
            true
        ), HolderTarget<MutableCollection<Component>> {
            override val value = target
        }
        return Simple()
    }

    fun <V> bindMap(target: MutableMap<V, Component>): BuilderBiAdder<*, V> {
        class Simple : BuilderBiAdder<Simple, V>(
            this.cast<BuilderGeneric<Simple>>(),
            target::put,
            true
        ), HolderTarget<MutableMap<V, Component>> {
            override val value = target
        }
        return Simple()
    }

    fun newArrayList() = this.bindCollection(ArrayList())

    fun <T> newHashMap() = this.bindMap(HashMap<T, Component>())

    fun pattern(pattern: IKeyPattern): TBuilder {
        this.pattern = pattern
        return this.cast()
    }

    fun item(item: ItemLike): TBuilder {
        this.keyMain = item.asItem().descriptionId
        return this.cast()
    }

    fun addStr(keyAdditional: String): TBuilder {
        if (keyAdditional.isBlank()) return this.cast()
        if (!this.keyAdditional.endsWith("."))
            this.keyAdditional += "."
        this.keyAdditional += keyAdditional
        return this.cast()
    }

    fun addStr(condition: Boolean, keyA: String, keyB: String = "") =
        this.addStr(if (condition) keyA else keyB)

    fun args(vararg args: Any?): TBuilder {
        this.args = Arrays.stream(args)
            .map {
                return@map if (it == null) ""
                else if (!TranslatableContents.isAllowedPrimitiveArgument(it))
                    it.toString()
                else it
            }.toArray()
        return this.cast()
    }

    fun buildRaw() = String.format(this.pattern.pattern, this.keyMain, this.keyAdditional)

    fun build(): MutableComponent {
        val keyRaw = this.buildRaw()

        return this.args
            ?.let { Component.translatable(keyRaw, it) }
            ?: Component.translatable(keyRaw)
    }
}

abstract class BuilderSnapshotable<TBuilder : BuilderGeneric<TBuilder>, TTarget> internal constructor(
    original: BuilderGeneric<TBuilder>?,
    protected val target: TTarget,
    snapshot: Boolean
) : BuilderGeneric<TBuilder>(original) {
    internal var snapshot: TBuilder? = null

    init {
        if (snapshot)
            this.snapshot()
    }

    abstract fun snapshot(): TBuilder

    open fun restore(): TBuilder {
        if (this.snapshot == null) return this.cast()
        return this.snapshot!!
    }
}

object ContainerDataGen {
    private val translators = HashMap<String, (String, String) -> Unit>()
    private val locale: ThreadLocal<String> = ThreadLocal.withInitial { "en_us" }

    @JvmStatic
    fun bind(locale: String, adder: (String, String) -> Unit) {
        this.translators[locale] = adder
        this.locale.set(locale)
    }

    @JvmStatic
    fun destroy(locale: String) {
        this.translators.remove(locale)
        this.locale.remove()
    }

    internal fun checkEnv() = if (!DatagenModLoader.isRunningDataGen())
        throw IllegalStateException("Cannot use data-only methods outside of the runData phase") else Unit

    internal val accept
        get() = this.translators.getOrDefault(
            this.locale.get()
        ) { _, _ -> }
}

class BuilderDataGen internal constructor(
    original: BuilderGeneric<BuilderDataGen>?,
    snapshot: Boolean
) : BuilderSnapshotable<BuilderDataGen, Any?>(original, null, snapshot) {
    override fun snapshot(): BuilderDataGen {
        val builder = BuilderDataGen(this, false)
        builder.snapshot = this
        return builder
    }

    fun buildInto(text: String): BuilderDataGen {
        ContainerDataGen.accept(this.buildRaw(), text)
        return this
    }

    fun branch(keyBranch: String, text: String) =
        this.snapshot()
            .addStr(keyBranch)
            .buildInto(text)
            .restore()
}

open class BuilderAdder<TBuilder : BuilderAdder<TBuilder>> internal constructor(
    original: BuilderGeneric<TBuilder>?,
    target: Adder,
    snapshot: Boolean
) : BuilderSnapshotable<TBuilder, Adder>(original, target, snapshot) {
    override fun snapshot(): TBuilder {
        val builder = BuilderAdder(this, this.target, false).cast()
        builder.snapshot = this.cast()
        return builder
    }

    fun buildInto(
        keyBranch: String = "",
        customizer: Customizer<MutableComponent> = { it }
    ): TBuilder {
        this.snapshot()
            .addStr(keyBranch)
            .let(BuilderGeneric<TBuilder>::build)
            .let(customizer)
            .let(this.target)
        return this.restore()
    }
}

open class BuilderBiAdder<TBuilder : BuilderBiAdder<TBuilder, TKey>, TKey> internal constructor(
    original: BuilderGeneric<TBuilder>?,
    target: BiAdder<TKey>,
    snapshot: Boolean
) : BuilderSnapshotable<TBuilder, BiAdder<TKey>>(original, target, snapshot) {
    override fun snapshot(): TBuilder {
        val builder = BuilderBiAdder(this, this.target, false).cast()
        builder.snapshot = this.cast()
        return builder
    }

    fun buildInto(
        keyBranch: TKey,
        customizer: Customizer<MutableComponent> = { it },
        plain: Boolean
    ): TBuilder {
        this.snapshot()
            .addStr(!plain, keyBranch.toString())
            .let(BuilderGeneric<TBuilder>::build)
            .let(customizer)
            .let { this.target(keyBranch, it) }
        return this.restore()
    }
}

interface HolderTarget<T> {
    val value: T
}

@Suppress("unchecked_cast")
fun BuilderAdder<*>.getCollection() = (this as? HolderTarget<*>)?.value as? MutableCollection<Component>

@Suppress("unchecked_cast")
fun <T> BuilderBiAdder<*, T>.getMap() = (this as? HolderTarget<*>)?.value as? MutableMap<Component, T>