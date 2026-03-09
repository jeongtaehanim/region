package io.github.jeongtaehanim.region.internal

import io.github.jeongtaehanim.region.RegionEvent
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.*
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.event.world.StructureGrowEvent
import java.util.*

class RegionEventListener(private val server: RegionServerImpl) : Listener {
    private val access = RegionAccessController(server)

    private fun canPlayerTarget(player: Player, target: Chunk, vararg flags: RegionEvent): Boolean {
        val rt = access.chunk(target)
        return access.canPlayer(player.uniqueId, rt, null, *flags)
    }

    private fun canPlayerTarget(player: Player, target: Location, vararg flags: RegionEvent): Boolean =
        canPlayerTarget(player, target.chunk, *flags)

    private fun canPlayerTransfer(player: Player, from: Chunk, to: Chunk, vararg flags: RegionEvent): Boolean {
        val rFrom = access.chunk(from)
        return if (!access.isCrossChunk(from, to)) {
            access.canPlayer(player.uniqueId, rFrom, null, *flags)
        } else {
            val rTo = access.chunk(to)
            access.canPlayer(player.uniqueId, rFrom, rTo, *flags, RegionEvent.PlayerChunkChangeEvent)
        }
    }

    private fun canPlayerTransfer(player: Player, from: Location, to: Location, vararg flags: RegionEvent): Boolean =
        canPlayerTransfer(player, from.chunk, to.chunk, *flags)

    private fun canRegionTarget(target: Chunk, vararg flags: RegionEvent): Boolean {
        val rt = access.chunk(target)
        return access.canRegion(rt, null, *flags)
    }

    private fun canRegionTarget(target: Block, vararg flags: RegionEvent): Boolean =
        canRegionTarget(target.chunk, *flags)

    private fun canRegionTarget(target: Location, vararg flags: RegionEvent): Boolean =
        canRegionTarget(target.chunk, *flags)

    private fun canRegionTransferToOnly(from: Chunk, to: Chunk, vararg flags: RegionEvent): Boolean {
        return canRegionTarget(to, *flags)
    }

    private fun canRegionTransferToOnly(from: Block, to: Block, vararg flags: RegionEvent): Boolean =
        canRegionTransferToOnly(from.chunk, to.chunk, *flags)

    private fun canRegionTransferBoth(from: Chunk, to: Chunk, vararg flags: RegionEvent): Boolean {
        val r1 = access.chunk(from)
        return if (!access.isCrossChunk(from, to)) {
            access.canRegion(r1, null, *flags)
        } else {
            val r2 = access.chunk(to)
            access.canRegion(r1, r2, *flags)
        }
    }

    @EventHandler
    fun onChunkLoadEvent(e: ChunkLoadEvent) {
        server.chunk(e.chunk)
    }

    @EventHandler
    fun onChunkUnloadEvent(e: ChunkUnloadEvent) {
        server.chunk(e.chunk).save()
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerMove(e: PlayerMoveEvent) {
        e.isCancelled = !canPlayerTransfer(e.player, e.from, e.to, RegionEvent.PlayerMoveEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (e.cause == PlayerTeleportEvent.TeleportCause.COMMAND ||
            e.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ||
            e.cause == PlayerTeleportEvent.TeleportCause.END_PORTAL ||
            e.cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY
        ) return

        e.isCancelled = !canPlayerTransfer(e.player, e.from, e.to, RegionEvent.PlayerTeleportEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerPortal(e: PlayerPortalEvent) {
        e.isCancelled = !canPlayerTransfer(e.player, e.from, e.to, RegionEvent.PlayerPortalEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val block = e.clickedBlock
        val target = e.clickedBlock?.location ?: e.player.location

        e.isCancelled = !if (block != null && block.type.name.endsWith("_BED")) {
            canPlayerTarget(e.player, target, RegionEvent.PlayerInteractEvent, RegionEvent.PlayerBedEvent)
        } else {
            canPlayerTarget(e.player, target, RegionEvent.PlayerInteractEvent)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteractEntity(e: PlayerInteractEntityEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.rightClicked.location, RegionEvent.PlayerInteractEntityEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteractAtEntity(e: PlayerInteractAtEntityEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.rightClicked.location, RegionEvent.PlayerInteractAtEntityEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onArmorStandManipulate(e: PlayerArmorStandManipulateEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.rightClicked.location, RegionEvent.PlayerArmorStandManipulateEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onSwapHand(e: PlayerSwapHandItemsEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.player.location, RegionEvent.PlayerSwapHandItemsEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onItemConsume(e: PlayerItemConsumeEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.player.location, RegionEvent.PlayerItemConsumeEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onFish(e: PlayerFishEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.hook.location, RegionEvent.PlayerFishEvent)
    }

//    @EventHandler(ignoreCancelled = true)
//    fun onBedEnter(e: PlayerBedEnterEvent) {
//        e.isCancelled = !canPlayerTarget(e.player, e.bed.location, RegionEvent.PlayerBedEvent)
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    fun onBedLeave(e: PlayerBedLeaveEvent) {
//        e.isCancelled = !canPlayerTarget(e.player, e.player.location, RegionEvent.PlayerBedEvent)
//    }

    @EventHandler(ignoreCancelled = true)
    fun onEggThrow(e: PlayerEggThrowEvent) {
        val ok = canPlayerTarget(e.player, e.player.location, RegionEvent.PlayerEggThrowEvent)
        if (!ok) {
            e.isHatching = false
            e.numHatches = 0
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onVehicleEnter(e: VehicleEnterEvent) {
        val p = e.entered as? Player ?: return
        e.isCancelled = !canPlayerTarget(p, e.vehicle.location, RegionEvent.PlayerVehicleEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onVehicleExit(e: VehicleExitEvent) {
        val p = e.exited as? Player ?: return
        e.isCancelled = !canPlayerTarget(p, e.vehicle.location, RegionEvent.PlayerVehicleEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onVehicleMove(e: VehicleMoveEvent) {
        val p = e.vehicle.passengers.firstOrNull() as? Player ?: return
        val ok = canPlayerTransfer(
            p,
            e.from.chunk,
            e.to.chunk,
            RegionEvent.PlayerMoveEvent
        )
        if (!ok) {
            e.vehicle.velocity = e.vehicle.velocity.zero()
            e.vehicle.teleport(e.from)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onLeash(e: PlayerLeashEntityEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.entity.location, RegionEvent.PlayerLeashEntityEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onUnleash(e: PlayerUnleashEntityEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.entity.location, RegionEvent.PlayerLeashEntityEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockPlace(e: BlockPlaceEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.blockPlaced.location, RegionEvent.BlockPlaceEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockMultiPlace(e: BlockMultiPlaceEvent) {
        val ok = e.replacedBlockStates.all { s ->
            canPlayerTarget(e.player, s.block.chunk, RegionEvent.BlockMultiPlaceEvent)
        }
        e.isCancelled = !ok
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(e: BlockBreakEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.block.location, RegionEvent.BlockBreakEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockDamage(e: BlockDamageEvent) {
        e.isCancelled = !canPlayerTarget(e.player, e.block.location, RegionEvent.BlockDamageEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBucketEmpty(e: PlayerBucketEmptyEvent) {
        val target = e.blockClicked.getRelative(e.blockFace).location
        e.isCancelled = !canPlayerTarget(e.player, target, RegionEvent.PlayerBucketEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBucketFill(e: PlayerBucketFillEvent) {
        val target = e.blockClicked.location
        e.isCancelled = !canPlayerTarget(e.player, target, RegionEvent.PlayerBucketEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBurn(e: BlockBurnEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.BlockBurnEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockIgnite(e: BlockIgniteEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.BlockIgniteEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockFade(e: BlockFadeEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.BlockFadeEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockForm(e: BlockFormEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.BlockFormEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockGrow(e: BlockGrowEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.BlockGrowEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onStructureGrow(e: StructureGrowEvent) {
        val ok = canRegionTarget(e.location, RegionEvent.BlockGrowEvent)
        if (!ok) {
            e.isCancelled = true
            return
        }
        e.blocks.removeIf { state -> !canRegionTarget(state.location, RegionEvent.BlockGrowEvent) }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockRedstone(e: BlockRedstoneEvent) {
        val ok = canRegionTarget(e.block, RegionEvent.BlockRedstoneEvent)
        if (!ok) e.newCurrent = 0
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockDispense(e: BlockDispenseEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.BlockDispenseEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockFromTo(e: BlockFromToEvent) {
        e.isCancelled = !canRegionTransferToOnly(e.block, e.toBlock, RegionEvent.BlockFromToEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockSpread(e: BlockSpreadEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.BlockSpreadEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPistonExtend(e: BlockPistonExtendEvent) {
        val ok = canRegionTarget(e.block.chunk, RegionEvent.BlockPistonEvent) &&
                e.blocks.all { moved ->
                    val to = moved.getRelative(e.direction)
                    canRegionTarget(to.chunk, RegionEvent.BlockPistonEvent)
                }
        e.isCancelled = !ok
    }

    @EventHandler(ignoreCancelled = true)
    fun onPistonRetract(e: BlockPistonRetractEvent) {
        val ok = canRegionTarget(e.block.chunk, RegionEvent.BlockPistonEvent) &&
                e.blocks.all { moved ->
                    val to = moved.getRelative(e.direction.oppositeFace)
                    canRegionTarget(to.chunk, RegionEvent.BlockPistonEvent)
                }
        e.isCancelled = !ok
    }

    @EventHandler(ignoreCancelled = true)
    fun onExplosionPrime(e: ExplosionPrimeEvent) {
        e.isCancelled = !canRegionTarget(e.entity.location, RegionEvent.ExplosionPrimeEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockExplode(e: BlockExplodeEvent) {
        if (!canRegionTarget(e.block.chunk, RegionEvent.BlockExplodeEvent)) {
            e.isCancelled = true
            return
        }
        e.blockList().removeIf { b ->
            !canRegionTarget(b.chunk, RegionEvent.BlockExplodeEvent)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityExplode(e: EntityExplodeEvent) {
        if (!canRegionTarget(e.entity.location, RegionEvent.EntityExplodeEvent)) {
            e.isCancelled = true
            return
        }
        e.blockList().removeIf { b ->
            !canRegionTarget(b.chunk, RegionEvent.EntityExplodeEvent)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityChangeBlock(e: EntityChangeBlockEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.EntityChangeBlockEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityBlockForm(e: EntityBlockFormEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.EntityBlockFormEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityInteract(e: EntityInteractEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.EntityInteractEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamage(e: EntityDamageEvent) {
        e.isCancelled = !canRegionTarget(e.entity.location, RegionEvent.EntityDamageEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageByEntity(e: EntityDamageByEntityEvent) {
        val damagerPlayer: Player? = when (val d = e.damager) {
            is Player -> d
            is Projectile -> (d.shooter as? Player)
            else -> null
        }

        val ok = if (damagerPlayer != null) {
            canPlayerTarget(damagerPlayer, e.entity.location, RegionEvent.EntityDamageByEntityEvent)
        } else {
            canRegionTarget(e.entity.location, RegionEvent.EntityDamageByEntityEvent)
        }

        e.isCancelled = !ok
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityPickupItem(e: EntityPickupItemEvent) {
        e.isCancelled = !canRegionTarget(e.entity.location, RegionEvent.EntityPickupItemEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityPlace(e: EntityPlaceEvent) {
        e.isCancelled = !canRegionTarget(e.block, RegionEvent.EntityPlaceEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onProjectileLaunch(e: ProjectileLaunchEvent) {
        val shooter = e.entity.shooter as? Player

        val ok = if (shooter != null) {
            canPlayerTarget(shooter, shooter.location, RegionEvent.ProjectileLaunchEvent)
        } else {
            canRegionTarget(e.entity.location, RegionEvent.ProjectileLaunchEvent)
        }

        e.isCancelled = !ok
    }

    @EventHandler(ignoreCancelled = true)
    fun onProjectileHit(e: ProjectileHitEvent) {
        e.isCancelled = !canRegionTarget(e.entity.location, RegionEvent.ProjectileHitEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onHangingPlace(e: HangingPlaceEvent) {
        val r = access.chunk(e.entity.location)
        val p = e.player
        val ok = if (p != null) access.canPlayer(p.uniqueId, r, null, RegionEvent.HangingPlaceEvent)
        else access.canRegion(r, null, RegionEvent.HangingPlaceEvent)
        e.isCancelled = !ok
    }

    @EventHandler(ignoreCancelled = true)
    fun onHangingBreakByEntity(e: HangingBreakByEntityEvent) {
        val r = access.chunk(e.entity.location)

        val breaker: Player? = when (val rm = e.remover) {
            is Player -> rm
            is Projectile -> (rm.shooter as? Player)
            else -> null
        }

        val ok = if (breaker != null) access.canPlayer(breaker.uniqueId, r, null, RegionEvent.HangingBreakByEntityEvent)
        else access.canRegion(r, null, RegionEvent.HangingBreakByEntityEvent)

        e.isCancelled = !ok
    }

    @EventHandler(ignoreCancelled = true)
    fun onCreatureSpawn(e: CreatureSpawnEvent) {
        e.isCancelled = !canRegionTarget(e.location, RegionEvent.CreatureSpawnEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onSpawnerSpawn(e: SpawnerSpawnEvent) {
        e.isCancelled = !canRegionTarget(e.location, RegionEvent.SpawnerSpawnEvent)
    }

    private fun invRegion(viewer: Player, inv: org.bukkit.inventory.Inventory): ChunkRegionImpl {
        val h = inv.holder
        val loc = when (h) {
            is org.bukkit.block.BlockState -> h.location
            is org.bukkit.entity.Entity -> h.location
            else -> viewer.location
        }
        return access.chunk(loc)
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryOpen(e: InventoryOpenEvent) {
        val p = e.player as? Player ?: return
        e.isCancelled = !access.canPlayer(p.uniqueId, invRegion(p, e.inventory), null, RegionEvent.InventoryOpenEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryClick(e: InventoryClickEvent) {
        val p = e.whoClicked as? Player ?: return
        e.isCancelled = !access.canPlayer(p.uniqueId, invRegion(p, e.inventory), null, RegionEvent.InventoryClickEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryDrag(e: InventoryDragEvent) {
        val p = e.whoClicked as? Player ?: return
        e.isCancelled = !access.canPlayer(p.uniqueId, invRegion(p, e.inventory), null, RegionEvent.InventoryDragEvent)
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryMoveItem(e: InventoryMoveItemEvent) {
        val src = when (val h = e.source.holder) {
            is org.bukkit.block.BlockState -> h.location
            is org.bukkit.entity.Entity -> h.location
            else -> null
        }
        val dst = when (val h = e.destination.holder) {
            is org.bukkit.block.BlockState -> h.location
            is org.bukkit.entity.Entity -> h.location
            else -> null
        }

        val ok = when {
            src != null && dst != null -> canRegionTransferBoth(
                src.chunk,
                dst.chunk,
                RegionEvent.InventoryMoveItemEvent
            )

            src != null -> canRegionTarget(src, RegionEvent.InventoryMoveItemEvent)
            dst != null -> canRegionTarget(dst, RegionEvent.InventoryMoveItemEvent)
            else -> true
        }

        e.isCancelled = !ok
    }

    @EventHandler(ignoreCancelled = true)
    fun onPrepareAnvil(e: PrepareAnvilEvent) {
        val p = e.viewers.firstOrNull() as? Player ?: return
        val ok = access.canPlayer(p.uniqueId, invRegion(p, e.inventory), null, RegionEvent.PrepareAnvilEvent)
        if (!ok) e.result = null
    }

    @EventHandler(ignoreCancelled = true)
    fun onPrepareSmithing(e: PrepareSmithingEvent) {
        val p = e.viewers.firstOrNull() as? Player ?: return
        val ok = access.canPlayer(p.uniqueId, invRegion(p, e.inventory), null, RegionEvent.PrepareSmithingEvent)
        if (!ok) e.result = null
    }

    @EventHandler(ignoreCancelled = true)
    fun onPrepareItemCraft(e: PrepareItemCraftEvent) {
        val p = e.viewers.firstOrNull() as? Player ?: return
        val ok = canPlayerTarget(p, p.location, RegionEvent.PrepareItemCraftEvent)
        if (!ok) e.inventory.result = null
    }
}

class RegionAccessController(private val server: RegionServerImpl) {
    fun canPlayer(
        playerId: UUID,
        r1: ChunkRegionImpl,
        r2: ChunkRegionImpl? = null,
        vararg flags: RegionEvent
    ): Boolean {
        val privileged = if (r2 == null) {
            server.isPrivileged(playerId, r1)
        } else {
            server.isPrivileged(playerId, r1) && server.isPrivileged(playerId, r2)
        }
        if (privileged) return true
        return flags.all { f -> r1.config.get(f) && (r2?.config?.get(f) ?: true) }
    }

    fun canRegion(
        r1: ChunkRegionImpl,
        r2: ChunkRegionImpl? = null,
        vararg flags: RegionEvent
    ): Boolean {
        return flags.all { f -> r1.config.get(f) && (r2?.config?.get(f) ?: true) }
    }

    fun chunk(c: Chunk): ChunkRegionImpl = server.chunk(c)
    fun chunk(loc: Location): ChunkRegionImpl = server.chunk(loc.chunk)

    fun sameChunk(a: Chunk, b: Chunk): Boolean =
        a.world.uid == b.world.uid && a.chunkKey == b.chunkKey

    fun isCrossChunk(a: Chunk, b: Chunk): Boolean =
        !sameChunk(a, b)
}