package io.github.jeongtaehanim.region.internal

import io.github.jeongtaehanim.region.ChunkRegion
import io.github.jeongtaehanim.region.util.data.PersistentDataKey
import io.github.jeongtaehanim.region.util.data.PersistentDataKeychain
import io.github.jeongtaehanim.region.util.data.persistentData
import org.bukkit.Chunk
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

object RegionConfigKey : PersistentDataKeychain() {
    val config: PersistentDataKey<Long, Long> = primitive("region_config")
    val owner: PersistentDataKey<ByteArray, UUID> = uuid("region_owner")
}

class ChunkRegionImpl private constructor(
    private val plugin: JavaPlugin,
    private val chunk: Chunk,
    private val worldConfig: RegionConfigImpl,
    owner: UUID?,
) : ChunkRegion {

    private val log = plugin.logger
    private val debug: Boolean get() = plugin.config.getBoolean("debug", false)

    private var overrideValue: Long? = chunk.persistentData[RegionConfigKey.config]

    override val config: RegionConfigImpl =
        RegionConfigImpl(overrideValue ?: worldConfig.value)

    override var owner: UUID? = owner ?: chunk.persistentData[RegionConfigKey.owner]
        set(value) {
            field = value
            if (value == null) {
                chunk.persistentData.remove(RegionConfigKey.owner)
            } else {
                chunk.persistentData[RegionConfigKey.owner] = value
            }
        }

    companion object {
        fun create(
            plugin: JavaPlugin,
            chunk: Chunk,
            worldConfig: RegionConfigImpl,
            owner: UUID? = null
        ): ChunkRegionImpl = ChunkRegionImpl(plugin, chunk, worldConfig, owner)
    }

    override fun isOverride(): Boolean = overrideValue != null

//    fun setOverrideMask(mask: Long) {
//        overrideValue = mask
//        config.value = mask
//    }
//
//    fun setOverride(event: RegionEvent, allowed: Boolean) {
//        if (overrideValue == null) {
//            overrideValue = config.value
//        }
//        config.set(event, allowed)
//        overrideValue = config.value
//    }

    override fun clearOverride() {
        overrideValue = null
        chunk.persistentData.remove(RegionConfigKey.config)
        config.value = worldConfig.value
    }

    fun onWorldDefaultChanged(newDefault: Long) {
        if (overrideValue != null) return
        config.value = newDefault
    }

//    fun isDirty(): Boolean {
//        val mem = config.value
//        val stored = overrideValue
//        return if (stored == null) mem != worldConfig.value else mem != stored
//    }

    override fun save() {
        val v = config.value

        if (v == worldConfig.value) {
            overrideValue = null
            chunk.persistentData.remove(RegionConfigKey.config)
            if (debug) log.fine("ChunkRegion saved (cleared override): ${chunk.world.name} (${chunk.x}, ${chunk.z})")
            return
        }

        overrideValue = v
        chunk.persistentData[RegionConfigKey.config] = v
        if (debug) log.fine("ChunkRegion saved: ${chunk.world.name} (${chunk.x}, ${chunk.z}) mask=$v")
    }
}