package io.github.jeongtaehanim.region.plugin

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.BooleanArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import io.github.jeongtaehanim.region.RegionEvent
import io.github.jeongtaehanim.region.RegionServer
import org.bukkit.plugin.java.JavaPlugin

class RegionPlugin : JavaPlugin() {
    lateinit var server: RegionServer
        private set

    override fun onLoad() {
    }

    override fun onEnable() {
        server = RegionServer.create(this)
        server.enable()

        CommandAPICommand("region")
            .withSubcommand(
                CommandAPICommand("chunk")
                    .withSubcommand(
                        CommandAPICommand("config")
                            .withSubcommand(
                                CommandAPICommand("set")
                                    .withArguments(
                                        StringArgument("event")
                                            .replaceSuggestions(
                                                ArgumentSuggestions.strings(
                                                    *RegionEvent.entries.map { it.name }.toTypedArray()
                                                )
                                            ),
                                        BooleanArgument("value")
                                    )
                                    .executesPlayer(PlayerCommandExecutor { sender, args ->
                                        val eventName = args["event"] as String
                                        val value = args["value"] as Boolean

                                        val event = try {
                                            RegionEvent.valueOf(eventName)
                                        } catch (e: IllegalArgumentException) {
                                            sender.sendMessage("§cInvalid event.")
                                            return@PlayerCommandExecutor
                                        }

                                        val region = server.chunk(sender.chunk)
                                        region.config.set(event, value)
                                        region.save()

                                        sender.sendMessage("§a[chunk] ${event.name} -> $value")
                                    })
                            )
                            .withSubcommand(
                                CommandAPICommand("get")
                                    .withArguments(
                                        StringArgument("event")
                                            .replaceSuggestions(
                                                ArgumentSuggestions.strings(
                                                    *RegionEvent.entries.map { it.name }.toTypedArray()
                                                )
                                            )
                                    )
                                    .executesPlayer(PlayerCommandExecutor { sender, args ->
                                        val eventName = args["event"] as String

                                        val event = try {
                                            RegionEvent.valueOf(eventName)
                                        } catch (e: IllegalArgumentException) {
                                            sender.sendMessage("§cInvalid event.")
                                            return@PlayerCommandExecutor
                                        }

                                        sender.sendMessage(
                                            "§e[chunk] ${event.name} = ${
                                                server.chunk(sender.chunk).config.get(event)
                                            }"
                                        )
                                    })
                            )
                    )
            )
            .withSubcommand(
                CommandAPICommand("world")
                    .withSubcommand(
                        CommandAPICommand("config")
                            .withSubcommand(
                                CommandAPICommand("set")
                                    .withArguments(
                                        StringArgument("event")
                                            .replaceSuggestions(
                                                ArgumentSuggestions.strings(
                                                    *RegionEvent.entries.map { it.name }.toTypedArray()
                                                )
                                            ),
                                        BooleanArgument("value")
                                    )
                                    .executesPlayer(PlayerCommandExecutor { sender, args ->
                                        val eventName = args["event"] as String
                                        val value = args["value"] as Boolean

                                        val event = try {
                                            RegionEvent.valueOf(eventName)
                                        } catch (e: IllegalArgumentException) {
                                            sender.sendMessage("§cInvalid event.")
                                            return@PlayerCommandExecutor
                                        }

                                        val worldMgr = server.world(sender.world)
                                        worldMgr.setConfig(event, value)
                                        worldMgr.save()

                                        sender.sendMessage(
                                            "§a[world:${sender.world.name}] ${event.name} -> $value"
                                        )
                                    })
                            )
                            .withSubcommand(
                                CommandAPICommand("get")
                                    .withArguments(
                                        StringArgument("event")
                                            .replaceSuggestions(
                                                ArgumentSuggestions.strings(
                                                    *RegionEvent.entries.map { it.name }.toTypedArray()
                                                )
                                            )
                                    )
                                    .executesPlayer(PlayerCommandExecutor { sender, args ->
                                        val eventName = args["event"] as String

                                        val event = try {
                                            RegionEvent.valueOf(eventName)
                                        } catch (e: IllegalArgumentException) {
                                            sender.sendMessage("§cInvalid event.")
                                            return@PlayerCommandExecutor
                                        }

                                        sender.sendMessage(
                                            "§e[world:${sender.world.name}] ${event.name} = ${
                                                server.world(sender.world).config.get(event)
                                            }"
                                        )
                                    })
                            )
                    )
            )
            .register()
    }

    override fun onDisable() {
        server.disable()
    }
}
