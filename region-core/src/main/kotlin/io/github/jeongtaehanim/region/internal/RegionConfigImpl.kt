package io.github.jeongtaehanim.region.internal

import io.github.jeongtaehanim.region.RegionConfig
import io.github.jeongtaehanim.region.RegionEvent

class RegionConfigImpl(default: Long?): RegionConfig {
    override var value: Long = default ?: RegionEvent.DEFAULT_MASK

    override fun get(event: RegionEvent): Boolean { return (value and (1L shl event.ordinal)) != 0L }

    override fun set(event: RegionEvent, allowed: Boolean) {
        val bit = 1L shl event.ordinal
        value = if (allowed) value or bit else value and bit.inv()
    }
}