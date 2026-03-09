package io.github.jeongtaehanim.region

import io.github.jeongtaehanim.region.loader.LibraryLoader
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin

interface RegionServer {
    companion object : RegionServerInternal by LibraryLoader.loadImplement(RegionServerInternal::class.java)

    fun world(world: World): WorldRegionManager
    fun world(name: String): WorldRegionManager

    fun chunk(chunk: Chunk): ChunkRegion

    fun registerPrivilegeResolver(resolver: RegionPrivilegeResolver)

    fun save()

    fun invalidate(world: World)
    fun invalidate(name: String)
    fun invalidate()

    fun enable()
    fun disable()
}

interface RegionServerInternal {
    fun create(plugin: JavaPlugin): RegionServer
}