package io.github.jeongtaehanim.region

enum class RegionEvent {
    PlayerMoveEvent,
    PlayerTeleportEvent,
    PlayerPortalEvent,
    PlayerChunkChangeEvent,

    PlayerInteractEvent,
    PlayerInteractEntityEvent,
    PlayerInteractAtEntityEvent,
    PlayerArmorStandManipulateEvent,
    PlayerSwapHandItemsEvent,
    PlayerItemConsumeEvent,
    PlayerFishEvent,
    PlayerBedEvent,
    PlayerEggThrowEvent,
    PlayerVehicleEvent,
    PlayerLeashEntityEvent,
    PlayerBucketEvent,

    BlockPlaceEvent,
    BlockMultiPlaceEvent,
    BlockBreakEvent,
    BlockDamageEvent,

    BlockFromToEvent,
    BlockSpreadEvent,
    BlockBurnEvent,
    BlockIgniteEvent,
    BlockFadeEvent,
    BlockFormEvent,
    BlockGrowEvent,
    BlockPistonEvent,
    BlockRedstoneEvent,
    BlockDispenseEvent,

    BlockExplodeEvent,
    EntityExplodeEvent,
    ExplosionPrimeEvent,

    EntityChangeBlockEvent,
    EntityBlockFormEvent,
    EntityInteractEvent,
    EntityDamageEvent,
    EntityDamageByEntityEvent,
    EntityPickupItemEvent,
    EntityPlaceEvent,
    ProjectileLaunchEvent,
    ProjectileHitEvent,
    HangingBreakByEntityEvent,
    HangingPlaceEvent,
    CreatureSpawnEvent,
    SpawnerSpawnEvent,

    InventoryOpenEvent,
    InventoryClickEvent,
    InventoryMoveItemEvent,
    InventoryDragEvent,
    PrepareAnvilEvent,
    PrepareSmithingEvent,
    PrepareItemCraftEvent;

    val bit: Long get() = 1L shl ordinal

    companion object {
        val DEFAULT_MASK: Long = entries.fold(0L) { acc, e -> acc or e.bit }
    }
}