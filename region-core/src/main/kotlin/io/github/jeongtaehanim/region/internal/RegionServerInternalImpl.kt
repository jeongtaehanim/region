package io.github.jeongtaehanim.region.internal

import io.github.jeongtaehanim.region.RegionServer
import io.github.jeongtaehanim.region.RegionServerInternal
import org.bukkit.plugin.java.JavaPlugin

class RegionServerInternalImpl: RegionServerInternal {
    override fun create(plugin: JavaPlugin): RegionServer = RegionServerImpl(plugin)
}