package io.github.jeongtaehanim.region

import java.util.*

fun interface RegionPrivilegeResolver {
    fun isPrivileged(playerId: UUID, region: ChunkRegion, event: RegionEvent?): Boolean
}