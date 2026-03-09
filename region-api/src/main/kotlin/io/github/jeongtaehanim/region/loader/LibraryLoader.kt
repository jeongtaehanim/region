/*
 * Copyright (C) 2022 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.jeongtaehanim.region.loader

import org.bukkit.Bukkit
import java.lang.reflect.InvocationTargetException

@Suppress("unused")
object LibraryLoader {
    private fun <T> loadClass(className: String, type: Class<T>, vararg initArgs: Any?): T {
        val parameterTypes = initArgs.map { it?.javaClass }.toTypedArray()

        return try {
            val clazz = Class.forName(className, true, type.classLoader).asSubclass(type)
            val constructor = clazz.getConstructor(*parameterTypes)
            constructor.newInstance(*initArgs) as T
        } catch (e: ClassNotFoundException) {
            throw UnsupportedOperationException("${type.name} does not have implement", e)
        } catch (e: IllegalAccessException) {
            throw UnsupportedOperationException("${type.name} constructor is not visible", e)
        } catch (e: InstantiationException) {
            throw UnsupportedOperationException("${type.name} is abstract class", e)
        } catch (e: InvocationTargetException) {
            throw UnsupportedOperationException("${type.name} has an error occurred while creating the instance", e)
        }
    }

    fun <T> loadImplement(type: Class<T>, vararg initArgs: Any?): T {
        val packageName = type.`package`.name
        val className = "${type.simpleName}Impl"
        return loadClass("$packageName.internal.$className", type, *initArgs)
    }

    fun <T> loadNMS(type: Class<T>, vararg initArgs: Any?): T {
        val packageName = type.`package`.name
        val className = "NMS${type.simpleName}"
        val candidates = listOf(
            "$packageName.$libraryVersion.$className",
            "${
                packageName.substringBeforeLast('.', "") + ".$libraryVersion." +
                        packageName.substringAfterLast('.')
            }.$className"
        )

        candidates.forEach { candidate ->
            runCatching { return loadClass(candidate, type, *initArgs) }
        }

        throw UnsupportedOperationException("${type.name} does not support this version: $libraryVersion")
    }

    private val minecraftVersion by lazy { Bukkit.getServer().minecraftVersion }
    private val libraryVersion by lazy { "v${minecraftVersion.replace('.', '_')}" }
}