package io.github.jeongtaehanim.region

import java.util.UUID

fun interface RegionPrivilegeResolver {
    fun isPrivileged(playerId: UUID, region: ChunkRegion): Boolean
}