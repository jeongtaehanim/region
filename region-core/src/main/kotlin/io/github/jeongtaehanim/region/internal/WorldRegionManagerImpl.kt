package io.github.jeongtaehanim.region.internal

import io.github.jeongtaehanim.region.RegionEvent
import io.github.jeongtaehanim.region.WorldRegionManager
import io.github.jeongtaehanim.region.util.data.PersistentDataKey
import io.github.jeongtaehanim.region.util.data.PersistentDataKeychain
import io.github.jeongtaehanim.region.util.data.persistentData
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin

object WorldRegionKey : PersistentDataKeychain() {
    val config: PersistentDataKey<Long, Long> = primitive("region_config")
}

class WorldRegionManagerImpl private constructor(
    private val plugin: JavaPlugin,
    private val world: World
) : WorldRegionManager {

    private val log = plugin.logger
    private val debug: Boolean get() = plugin.config.getBoolean("debug", false)

    private val caches: MutableMap<Long, ChunkRegionImpl> = HashMap()

    override var config: RegionConfigImpl = RegionConfigImpl(
        world.persistentData[WorldRegionKey.config] ?: RegionEvent.DEFAULT_MASK
    )

    companion object {
        fun create(plugin: JavaPlugin, world: World): WorldRegionManagerImpl {
            plugin.logger.info("WorldRegionManager created: ${world.name}")
            return WorldRegionManagerImpl(plugin, world)
        }
    }

    override fun chunk(chunk: Chunk): ChunkRegionImpl {
        require(world.uid == chunk.world.uid) { "Invalid chunk" }
        caches[chunk.chunkKey]?.let { return it }
        return create(chunk)
    }

    override fun chunk(location: Location): ChunkRegionImpl = chunk(location.chunk)
    
    override fun chunk(x: Int, z: Int): ChunkRegionImpl = chunk(world.getChunkAt(x, z))

    private fun create(chunk: Chunk): ChunkRegionImpl {
        val region = ChunkRegionImpl.create(plugin, chunk, config)
        caches[chunk.chunkKey] = region
        if (debug) log.fine("ChunkRegion cached: ${world.name} (${chunk.x}, ${chunk.z})")
        return region
    }

    override fun setConfig(event: RegionEvent, allowed: Boolean) {
        config.set(event, allowed)
        world.persistentData[WorldRegionKey.config] = config.value
        if (debug) log.fine("World default config updated: ${world.name} ${event.name}=$allowed mask=${config.value}")

        val v = config.value
        caches.values.forEach { region -> region.onWorldDefaultChanged(v) }
    }

    override fun save(chunk: Chunk) {
        caches[chunk.chunkKey]?.save()
    }

    override fun save(location: Location) {
        save(location.chunk)
    }

    override fun save(x: Int, z: Int) = save(world.getChunkAt(x, z))

    override fun save() {
        if (debug) log.fine("Saving ${caches.size} cached chunk regions: ${world.name}")
        caches.values.forEach { it.save() }
    }

    override fun invalidate(chunk: Chunk) {
        caches.remove(chunk.chunkKey)?.save()
    }

    override fun invalidate(location: Location) {
        invalidate(location.chunk)
    }

    override fun invalidate(x: Int, z: Int) {
        invalidate(world.getChunkAt(x, z))
    }

    override fun invalidate() {
        caches.values.forEach { it.save() }
        caches.clear()
    }
}