package com.extendedae_plus.common.init

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforgespi.language.ModFileScanData
import org.slf4j.LoggerFactory
import java.lang.annotation.ElementType
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class InitObject(
    val priority: Int = 100,
    val dist: Array<Dist> = [Dist.CLIENT, Dist.DEDICATED_SERVER]
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger("[EAEP/Initializer]")

        private lateinit var objects: List<ModFileScanData.AnnotationData>

        operator fun invoke(eventBus: IEventBus, containerMod: ModContainer) {
            if (!::objects.isInitialized) {
                objects = ModList.get()
                    .allScanData
                    .asSequence()
                    .flatMap { it.annotations.asSequence() }
                    .sortedBy { it.annotationData.getOrDefault("priority", 100) as Int }
                    .toList()
            }
            objects
                .filter {
                    AutomaticEventSubscriber.getSides(it.annotationData["dist"])
                        .contains(FMLEnvironment.dist)
                }
                .forEach { this.doInit(eventBus, containerMod, it) }
        }

        private fun doInit(eventBus: IEventBus, containerMod: ModContainer, data: ModFileScanData.AnnotationData) {
            val clazzObject: Class<*>?
            try {
                clazzObject = Class.forName(data.clazz().className)
            } catch (exception: ClassNotFoundException) {
                LOGGER.warn("Failed to load class {}", data.clazz().className, exception)
                return
            }

            when (data.targetType) {
                ElementType.FIELD -> {
                    try {
                        val register = clazzObject.getDeclaredField(data.memberName()).get(null)
                        if (register !is DeferredRegister<*>) return
                        register.register(eventBus)
                    } catch (exception: NoSuchFieldException) {
                        LOGGER.error(
                            "Failed to load field register {}",
                            data.clazz().className,
                            exception
                        )
                        throw exception
                    } catch (exception: IllegalAccessException) {
                        LOGGER.error(
                            "Failed to load field register {}",
                            data.clazz().className,
                            exception
                        )
                        throw exception
                    }
                }

                ElementType.METHOD -> {
                    val valueMethod = data.memberName()
                    val nameMethod = valueMethod.substring(0, valueMethod.indexOf("("))

                    try {
                        val method: Method
                        val params: Array<Any?>
                        if (valueMethod.contains("IEventBus") && valueMethod.contains("ModContainer")) {
                            method = clazzObject.getDeclaredMethod(
                                nameMethod,
                                IEventBus::class.java,
                                ModContainer::class.java
                            )
                            params = arrayOf(eventBus, containerMod)
                        } else if (valueMethod.contains("IEventBus")) {
                            method = clazzObject.getDeclaredMethod(
                                nameMethod,
                                IEventBus::class.java
                            )
                            params = arrayOf(eventBus)
                        } else {
                            method = clazzObject.getDeclaredMethod(nameMethod)
                            params = arrayOfNulls(0)
                        }
                        method.isAccessible = true
                        method(null, *params)
                    } catch (exception: InvocationTargetException) {
                        LOGGER.error(
                            "Failed to load method init {}",
                            data.clazz().className,
                            exception
                        )
                        throw exception
                    } catch (exception: IllegalAccessException) {
                        LOGGER.error(
                            "Failed to load method init {}",
                            data.clazz().className,
                            exception
                        )
                        throw exception
                    } catch (exception: NoSuchMethodException) {
                        LOGGER.error(
                            "Failed to load method init {}",
                            data.clazz().className,
                            exception
                        )
                        throw exception
                    }
                }

                else -> Unit
            }
        }
    }
}
