package io.github.jeongtaehanim.region

import org.bukkit.Chunk
import org.bukkit.Location

interface WorldRegionManager {
    val config: RegionConfig

    fun chunk(chunk: Chunk): ChunkRegion
    fun chunk(location: Location): ChunkRegion
    fun chunk(x: Int, z: Int): ChunkRegion

    fun setConfig(event: RegionEvent, allowed: Boolean)

    fun save(chunk: Chunk)
    fun save(location: Location)
    fun save(x: Int, z: Int)
    fun save()

    fun invalidate(chunk: Chunk)
    fun invalidate(location: Location)
    fun invalidate(x: Int, z: Int)
    fun invalidate()
}