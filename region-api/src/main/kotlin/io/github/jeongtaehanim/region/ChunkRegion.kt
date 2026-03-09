package io.github.jeongtaehanim.region

import java.util.UUID

interface ChunkRegion {
    var owner: UUID?
    val config: RegionConfig

    fun isOverride(): Boolean

    fun clearOverride()

    fun save()
}