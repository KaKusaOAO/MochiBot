package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.arguments.AttachmentArgument
import com.kakaouo.bot.mochi.command.arguments.MentionableArgument
import com.kakaouo.bot.mochi.command.arguments.MessageArgument
import com.kakaouo.bot.mochi.command.exceptions.CommandMisuseException
import com.kakaouo.bot.mochi.command.exceptions.DispatcherParseFailureMessage
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.DiscordInteractionSender
import com.kakaouo.bot.mochi.command.sender.DiscordMessageSender
import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.bot.mochi.i18n.ILocalizable
import com.kakaouo.bot.mochi.texts.LiteralText
import com.kakaouo.bot.mochi.texts.LiteralText.Companion.toText
import com.kakaouo.bot.mochi.texts.TextColor
import com.kakaouo.bot.mochi.texts.Texts
import com.kakaouo.bot.mochi.texts.TranslateText
import com.kakaouo.bot.mochi.utils.Logger
import com.kakaouo.bot.mochi.utils.Utils.toCoroutine
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.CommandContextBuilder
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction

abstract class Command : ILocalizable {
    object L {
        const val PREFIX = "command"
        const val ERR_COMMAND_USAGE = "$PREFIX.error.commandUsage"
        const val HINT_SLASH_FOR_FULL_USAGE = "$PREFIX.hint.fullUsageBySlash"
        const val ERR_COMMAND_CURSOR_HERE = "$PREFIX.error.commandCursor.here"
        const val ERR_TITLE_DETAILS = "$PREFIX.error.title.details"
        const val ERR_TITLE_USAGE = "$PREFIX.error.title.usage"
        const val ERR_COMMAND_INVALID = "$PREFIX.error.command.invalid"
        const val ERR_COMMAND_INCOMPLETE = "$PREFIX.error.command.incomplete"
        const val ERR_INSUFFICIENT_PERMISSION = "$PREFIX.error.permission.insufficient"
    }

    companion object : ILocalizable {
        inline fun <reified T> CommandContext<CommandSource>.getArgument(name: String): T {
            return getArgument(name, T::class.java)
        }

        override fun registerLocalizations(generator: ILanguageGenerator) {
            generator
                .addEntry(L.ERR_COMMAND_USAGE,
                    Pair("base", "指令錯誤！請更正後再試一次！"),
                    Pair("en-US", "The command is used wrongly.")
                )
                .addEntry(L.HINT_SLASH_FOR_FULL_USAGE, "用斜線指令可以獲得完整說明")
                .addEntry(L.ERR_COMMAND_CURSOR_HERE,
                    Pair("base", "這裡"),
                    Pair("en-US", "here")
                )
                .addEntry(L.ERR_TITLE_DETAILS,
                    Pair("base", "詳細說明"),
                    Pair("en-US", "Details")
                )
                .addEntry(L.ERR_TITLE_USAGE,
                    Pair("base", "該指令的用法"),
                    Pair("en-US", "Usage")
                )
                .addEntry(L.ERR_COMMAND_INVALID,
                    Pair("base", "錯誤的指令！請更正後再試一次！"),
                    Pair("en-US", "Invalid command.")
                )
                .addEntry(L.ERR_COMMAND_INCOMPLETE,
                    Pair("base", "指令不完整！請更正後再試一次！"),
                    Pair("en-US", "Incomplete command.")
                )
                .addEntry(L.ERR_INSUFFICIENT_PERMISSION,
                    Pair("base", "<nickname>覺得不能幫你做這種事...zzZ"),
                    Pair("en-US", "<nickname> cannot do that for you... zzZ")
                )


            ExecuteCommand.registerLocalizations(generator)
            HelpCommand.registerLocalizations(generator)
            ChatCommand.registerLocalizations(generator)
        }

        fun registerToDispatcher() {
            val dispatcher = Mochi.instance.dispatcher

            Logger.info("Registering commands...")
            ExecuteCommand.register(dispatcher)
            HelpCommand.register(dispatcher)
            ChatCommand.register(dispatcher)
        }

        fun registerToDiscord(): List<CommandData> {
            val list = mutableListOf<CommandData>()
            ExecuteCommand.registerDiscord().apply { list.addAll(this) }
            HelpCommand.registerDiscord().apply { list.addAll(this) }
            ChatCommand.registerDiscord().apply { list.addAll(this) }
            return list
        }

        suspend fun registerCommandsForDiscordGuild(guild: Guild) {
            val createdCommands = ArrayList(registerToDiscord().filter {
                it.isGuildOnly
            })

            // Logger.info("Fetching existing guild commands...")
            val commands = guild.retrieveCommands().submit().toCoroutine()!!

            try {
                val removal = commands
                    .filterNotNull()
                    .filter { c ->
                        createdCommands.all { it.name != c.name }
                    }

                for (r in removal) {
                    try {
                        /*
                        Logger.info(
                            TranslateText.of(
                                "Removing non-existing guild command %s...",
                                LiteralText.of("/${r.name}").setColor(TextColor.AQUA)
                            )
                        )
                        */
                        guild.deleteCommandById(r.id).submit().toCoroutine()
                    } catch (ex: Throwable) {
                        Logger.warn(
                            TranslateText.of(
                                "Failed to delete guild command %s. This command will persist but will not be responded!",
                                LiteralText.of("/${r.name}").setColor(TextColor.AQUA)
                            )
                        )
                        Logger.warn(ex.stackTraceToString())
                    }
                }

                commands.removeAll(removal)
            } catch (ex: Throwable) {
                Logger.warn(
                    LiteralText.of(
                        "Failed to fetch guild commands. Old commands will persist but will not be responded (if exists)!"
                    ))
                Logger.warn(ex.stackTraceToString())
            }

            val newCommands = createdCommands
                .filter { n ->
                    commands.all { it.name != n.name }
                }
                .toList()

            val oldCommands = commands
                .filter { n ->
                    createdCommands.any { it.name == n.name }
                }
                .toList()

            for (c in oldCommands) {
                val new = createdCommands.find { it.name == c.name }!!

                /*
                Logger.info(
                    TranslateText.of(
                        "Patching existing guild command %s...",
                        LiteralText.of("/${c.name}").setColor(TextColor.AQUA)
                    )
                )
                 */

                // Edit the command
                guild.editCommandById(c.id).apply(new).submit().toCoroutine()
            }

            for (c in newCommands) {
                try {
                    /*
                    Logger.info(
                        TranslateText.of(
                            "Adding new guild command %s...",
                            LiteralText.of("/${c.name}").setColor(TextColor.AQUA)
                        )
                    )
                     */

                    guild.upsertCommand(c).submit().toCoroutine()
                } catch (ex: Throwable) {
                    Logger.warn(
                        TranslateText.of(
                            "Failed to create guild command %s. This command will not appear nor be updated!",
                            LiteralText.of("/${c.name}").setColor(TextColor.AQUA)
                        ))
                    Logger.warn(ex.stackTraceToString())
                }
            }

            Logger.log(
                TranslateText.of("Completed registering %s guild commands for guild %s.")
                    .addWith(createdCommands.size.toString().toText().setColor(TextColor.GREEN))
                    .addWith(Texts.ofGuild(guild))
            )
        }

        suspend fun registerCommandsForDiscordGlobal() {
            val client = Mochi.instance.client
            val createdCommands = ArrayList(registerToDiscord().filter {
                !it.isGuildOnly
            })

            // Logger.info("Fetching existing global commands...")
            val commands = client.retrieveCommands().submit().toCoroutine()!!

            try {
                val removal = commands
                    .filterNotNull()
                    .filter { c ->
                        createdCommands.all { it.name != c.name }
                    }

                for (r in removal) {
                    try {
                        /*
                        Logger.info(
                            TranslateText.of(
                                "Removing non-existing global command %s...",
                                LiteralText.of("/${r.name}").setColor(TextColor.AQUA)
                            )
                        )
                         */
                        client.deleteCommandById(r.id).submit().toCoroutine()
                    } catch (ex: Throwable) {
                        Logger.warn(
                            TranslateText.of(
                                "Failed to delete global command %s. This command will persist but will not be responded!",
                                LiteralText.of("/${r.name}").setColor(TextColor.AQUA)
                            )
                        )
                        Logger.warn(ex.stackTraceToString())
                    }
                }

                commands.removeAll(removal)
            } catch (ex: Throwable) {
                Logger.warn(
                    LiteralText.of(
                    "Failed to fetch global commands. Old commands will persist but will not be responded (if exists)!"
                ))
                Logger.warn(ex.stackTraceToString())
            }

            val newCommands = createdCommands
                .filter { n ->
                    commands.all { it.name != n.name }
                }
                .toList()

            val oldCommands = commands
                .filter { n ->
                    createdCommands.any { it.name == n.name }
                }
                .toList()

            for (c in oldCommands) {
                val new = createdCommands.find { it.name == c.name }!!

                /*
                Logger.info(
                    TranslateText.of(
                        "Patching existing global command %s...",
                        LiteralText.of("/${c.name}").setColor(TextColor.AQUA)
                    )
                )
                 */

                // Edit the command
                client.editCommandById(c.id).apply(new).submit().toCoroutine()
            }

            for (c in newCommands) {
                try {
                    /*
                    Logger.info(
                        TranslateText.of(
                            "Adding new command %s...",
                            LiteralText.of("/${c.name}").setColor(TextColor.AQUA)
                        )
                    )
                     */

                    client.upsertCommand(c).submit().toCoroutine()
                } catch (ex: Throwable) {
                    Logger.warn(
                        TranslateText.of(
                        "Failed to create command %s. This command will not appear nor be updated!",
                        LiteralText.of("/${c.name}").setColor(TextColor.AQUA)
                    ))
                    Logger.warn(ex.stackTraceToString())
                }
            }

            Logger.log(
                TranslateText.of("Completed registering %s global commands.")
                    .addWith(createdCommands.size.toString().toText().setColor(TextColor.GREEN))
            )
        }

        fun getCommandLineFromInteraction(interaction: CommandInteraction): String {
            var input = "/${interaction.name} "
            if (interaction is MessageContextInteraction) {
                val target = interaction.target
                MessageArgument.register(target)
                input += target.id
                return input.trim()
            }

            val options = interaction.options
            val group = interaction.subcommandGroup
            val subcmd = interaction.subcommandName
            if (!group.isNullOrEmpty()) {
                input += "$group "
            }
            if (!subcmd.isNullOrEmpty()) {
                input += "$subcmd "
            }

            for (opt in options) {
                // Handle every argument and register them if needed
                when (opt.type) {
                    OptionType.SUB_COMMAND -> continue
                    OptionType.SUB_COMMAND_GROUP -> continue
                    OptionType.UNKNOWN -> continue

                    OptionType.STRING -> {
                        input += "${opt.asString} "
                    }
                    OptionType.INTEGER -> {
                        input += "${opt.asLong} "
                    }
                    OptionType.BOOLEAN -> {
                        input += "${opt.asBoolean} "
                    }
                    OptionType.ATTACHMENT -> {
                        val attachment = opt.asAttachment
                        AttachmentArgument.register(attachment)
                        input += AttachmentArgument.serialize(attachment)
                    }
                    OptionType.USER -> {
                        input += "${opt.asUser.id} "
                    }
                    OptionType.CHANNEL -> {
                        input += "${opt.asChannel.id} "
                    }
                    OptionType.ROLE -> {
                        input += "${opt.asRole.id} "
                    }
                    OptionType.MENTIONABLE -> {
                        val mentionable = opt.asMentionable
                        MentionableArgument.register(mentionable)
                        input += "${mentionable.id} "
                    }
                    OptionType.NUMBER -> {
                        input += "${opt.asDouble} "
                    }
                }
            }

            return input.trim()
        }

        suspend fun executeByCommand(interaction: CommandInteraction) {
            val input = getCommandLineFromInteraction(interaction)
            val sender = DiscordInteractionSender(interaction)
            val source = CommandSource(sender)

            val result = Mochi.instance.dispatcher.parse(input.substring(1), source)
            executeParseResult(sender, result, input, 1)
        }

        suspend fun executeByCommand(message: Message) {
            val input = message.contentRaw
            val sender = DiscordMessageSender(message)
            val source = CommandSource(sender)

            val bot = Mochi.instance
            val prefix = bot.config.data.commandPrefix
            val result = bot.dispatcher.parse(input.substring(prefix.length), source)
            executeParseResult(sender, result, input, prefix.length)
        }

        private suspend fun executeParseResult(sender: IDiscordCommandSender, result: ParseResults<CommandSource>, input: String, cursorOffset: Int) {
            val guild = result.context.source.guild
            val dispatcher = Mochi.instance.dispatcher
            val i18n = sender.i18n

            fun addUsageField(builder: EmbedBuilder, context: CommandContextBuilder<CommandSource>) {
                val usage = getUsageText(dispatcher, context, input, cursorOffset)
                if (!usage.isNullOrEmpty()) {
                    builder.addField(
                        i18n.of(L.ERR_TITLE_USAGE),
                        "`$usage`",
                        false
                    )
                }
            }

            val exceptions = result.exceptions.values
            if (exceptions.isNotEmpty()) {
                sender.respond(embed = sender.createStyledEmbed {
                    setColor(Constants.ERROR_COLOR)
                    setDescription(i18n.of(L.ERR_COMMAND_USAGE))

                    val err = exceptions.first()
                    val errMessage = err.rawMessage
                    var messageString = errMessage.string
                    if (errMessage is DispatcherParseFailureMessage) {
                        messageString = errMessage.original.toString()
                    }

                    val cause = err.cause
                    if (cause is CommandMisuseException) {
                        if (sender is DiscordInteractionSender) {
                            messageString += "\n\n${cause.usage}"
                        } else {
                            val msg = i18n.of(L.HINT_SLASH_FOR_FULL_USAGE)
                            messageString += "\n-- $msg --"
                        }
                    }

                    val cursor = err.cursor + cursorOffset
                    if (messageString != null) {
                        addField(
                            i18n.of(L.ERR_TITLE_DETAILS),
                            "`${input.substring(0, cursor)}` <- " + i18n.of(L.ERR_COMMAND_CURSOR_HERE) + "\n$messageString",
                            false
                        )
                    }

                    addUsageField(this, result.context)
                }.build(), ephemeral = true)
                return
            }

            val reader = result.reader
            if (reader.canRead()) {
                val read = reader.read.length
                if (read == 0) {
                    // The dispatcher cannot find the requested command.
                    // We ignore this on Discord.
                    return;
                }

                sender.respond(embed = sender.createStyledEmbed {
                    setColor(Constants.ERROR_COLOR)
                    setDescription(i18n.of(L.ERR_COMMAND_INVALID))

                    val context = result.context.lastChild
                    addUsageField(this, context)
                }.build(), ephemeral = true)
                return
            }

            val context = result.context.lastChild
            if (context.command == null) {
                sender.respond(embed = sender.createStyledEmbed {
                    setColor(Constants.ERROR_COLOR)
                    setDescription(i18n.of(L.ERR_COMMAND_INCOMPLETE))

                    val ctx = context.lastChild
                    addUsageField(this, ctx)
                }.build(), ephemeral = true)
                return
            }

            Logger.info(
                TranslateText.of("%s issued a command: %s at %s")
                    .addWith(Texts.ofUser(sender.author))
                    .addWith(LiteralText.of(input).setColor(TextColor.AQUA))
                    .addWith(Texts.ofGuild(guild))
            )
            dispatcher.execute(result)
        }

        private fun getUsageText(dispatcher: CommandDispatcher<CommandSource>, context: CommandContextBuilder<CommandSource>, input: String, cursorOffset: Int): String? {
            val nodes = context.nodes
            if (nodes.isEmpty()) return ""

            var offset = cursorOffset
            val node = nodes.last()
            val parent = if (nodes.size > 1) nodes.takeLast(2)[0] else null
            offset += node.range.start

            val usage = dispatcher.getSmartUsage(parent?.node ?: dispatcher.root, context.source)
            return input.substring(0, offset) + usage[node.node]
        }
    }

    abstract override fun registerLocalizations(generator: ILanguageGenerator)

    abstract fun register(dispatcher: CommandDispatcher<CommandSource>)
}