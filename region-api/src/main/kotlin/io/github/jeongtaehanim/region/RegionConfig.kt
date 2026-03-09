package io.github.jeongtaehanim.region

interface RegionConfig {
    val value: Long

    fun get(event: RegionEvent): Boolean

    fun set(event: RegionEvent, allowed: Boolean)
}