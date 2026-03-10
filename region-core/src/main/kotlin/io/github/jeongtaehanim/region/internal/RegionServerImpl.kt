package io.github.jeongtaehanim.region.internal

import io.github.jeongtaehanim.region.RegionEvent
import io.github.jeongtaehanim.region.RegionPrivilegeResolver
import io.github.jeongtaehanim.region.RegionServer
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class RegionServerImpl(private val plugin: JavaPlugin) : RegionServer {
    private val privilegeResolvers: MutableList<RegionPrivilegeResolver> = mutableListOf()
    private val caches: MutableMap<String, WorldRegionManagerImpl> = HashMap()

    private val l: RegionEventListener = RegionEventListener(this)

    override fun world(world: World): WorldRegionManagerImpl = caches[world.name] ?: create(world)
    override fun world(name: String): WorldRegionManagerImpl {
        Bukkit.getWorld(name)?.let { world ->
            world(world)
        }
        throw Error("World not found")
    }

    fun create(world: World): WorldRegionManagerImpl {
        plugin.logger.info("Creating region manager for world: ${world.name}")
        WorldRegionManagerImpl.create(plugin, world).let { worldRegionManager ->
            caches[world.name] = worldRegionManager
            return worldRegionManager
        }
    }

    override fun chunk(chunk: Chunk): ChunkRegionImpl = world(chunk.world).chunk(chunk)

    override fun registerPrivilegeResolver(resolver: RegionPrivilegeResolver) {
        privilegeResolvers += resolver
    }

    fun isPrivileged(uniqueId: UUID, region: ChunkRegionImpl, event: RegionEvent? = null): Boolean {
        val o = region.owner
        if (o != null && o == uniqueId) {
            return true
        }
        Bukkit.getPlayer(uniqueId)?.let { player ->
            if (player.isOp) {
                return true
            }
        }
        return privilegeResolvers.any { it.isPrivileged(uniqueId, region, event) }
    }

    override fun save() {
        plugin.logger.info("Saving ${caches.size} world region managers...")
        caches.forEach { (_, worldRegionManager) -> worldRegionManager.save() }
    }

    override fun invalidate(world: World) {
        invalidate(world.name)
    }

    override fun invalidate(name: String) {
        caches.remove(name)?.save()
    }

    override fun invalidate() {
        this.caches.values.forEach { it.save() }
        caches.clear()
    }

    override fun enable() {
        plugin.logger.info("RegionServer enabled")
        Bukkit.getPluginManager().registerEvents(l, plugin)
    }

    override fun disable() {
        plugin.logger.info("RegionServer disabling, saving regions...")
        HandlerList.unregisterAll(l)
        save()
        plugin.logger.info("RegionServer disabled")
    }
}