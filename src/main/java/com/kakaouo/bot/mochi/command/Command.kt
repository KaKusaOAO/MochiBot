package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.arguments.*
import com.kakaouo.bot.mochi.command.exceptions.CommandMisuseException
import com.kakaouo.bot.mochi.command.exceptions.DispatcherParseFailureMessage
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.DiscordInteractionSender
import com.kakaouo.bot.mochi.command.sender.DiscordMessageSender
import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.bot.mochi.i18n.ILocalizable
import com.kakaouo.bot.mochi.texts.TextBuilder
import com.kakaouo.bot.mochi.texts.Texts
import com.kakaouo.mochi.texts.LiteralText
import com.kakaouo.mochi.texts.TextColor
import com.kakaouo.mochi.texts.TextKt.toText
import com.kakaouo.mochi.texts.TranslateText
import com.kakaouo.mochi.utils.Logger
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.CommandContextBuilder
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.DiscordLocale
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


            // Every localizable commands should also be registered here
            ExecuteCommand.registerLocalizations(generator)
            HelpCommand.registerLocalizations(generator)
            ChatCommand.registerLocalizations(generator)
            IPlayerBaseCommand.registerLocalizations(generator)
            PlayCommand.registerLocalizations(generator)
            PauseCommand.registerLocalizations(generator)
            TestCommand.registerLocalizations(generator)
        }

        fun registerToDispatcher() {
            val dispatcher = Mochi.instance.dispatcher

            Logger.info("Registering commands...")
            ExecuteCommand.register(dispatcher)
            HelpCommand.register(dispatcher)
            ChatCommand.register(dispatcher)
            PlayCommand.register(dispatcher)
            PauseCommand.register(dispatcher)
            if (TestCommand.ENABLED) {
                TestCommand.register(dispatcher)
            }
        }

        fun registerToDiscord(): List<CommandData> {
            val list = mutableListOf<CommandData>()
            ExecuteCommand.registerDiscord().apply { list.addAll(this) }
            HelpCommand.registerDiscord().apply { list.addAll(this) }
            ChatCommand.registerDiscord().apply { list.addAll(this) }
            PlayCommand.registerDiscord().apply { list.addAll(this) }
            PauseCommand.registerDiscord().apply { list.addAll(this) }
            if (TestCommand.ENABLED) {
                TestCommand.registerDiscord().apply { list.addAll(this) }
            }
            return list
        }

        fun makeLocaleMap(key: String): Map<DiscordLocale, String> {
            val bot = Mochi.instance
            val map = mutableMapOf<DiscordLocale, String>()

            val locales = arrayOf(
                DiscordLocale.CHINESE_TAIWAN,
                DiscordLocale.ENGLISH_US
            )
            for (l in locales) {
                map[l] = bot.getI18nFor(l).of(key)
            }
            return map
        }

        suspend fun registerCommandsForDiscordGuild(guild: Guild) {
            val createdCommands = ArrayList(registerToDiscord().filter {
                it.isGuildOnly
            })

            guild.updateCommands().addCommands(createdCommands).submit().await()

            Logger.log(
                TranslateText.of("Completed registering %s guild commands for guild %s.")
                    .addWith(createdCommands.size.toString().toText().setColor(TextColor.GREEN))
                    .addWith(Texts.ofGuild(guild))
            )

            Logger.log(Texts.translate("Completed registering %s guild commands for guild %s.") {
                with {
                    literal(createdCommands.size.toString()) {
                        color(TextColor.GREEN)
                    }
                }
                with(Texts.ofGuild(guild))
            })
        }

        suspend fun registerCommandsForDiscordGlobal() {
            val client = Mochi.instance.client
            val createdCommands = ArrayList(registerToDiscord().filter {
                !it.isGuildOnly
            })

            client.updateCommands().addCommands(createdCommands).submit().await()

            Logger.log(Texts.translate("Completed registering %s global commands.") {
                with {
                    literal(createdCommands.size.toString()) {
                        color(TextColor.GREEN)
                    }
                }
            })
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

            val args = mutableListOf<String>()
            for (opt in options) {
                // Handle every argument and register them if needed
                when (opt.type) {
                    OptionType.SUB_COMMAND -> {
                        continue
                    }
                    OptionType.SUB_COMMAND_GROUP -> {
                        continue
                    }
                    OptionType.UNKNOWN -> {
                        continue
                    }

                    OptionType.STRING -> {
                        args.add(opt.asString)
                    }
                    OptionType.INTEGER -> {
                        args.add("${opt.asLong}")
                    }
                    OptionType.BOOLEAN -> {
                        args.add("${opt.asBoolean}")
                    }
                    OptionType.ATTACHMENT -> {
                        val attachment = opt.asAttachment
                        AttachmentArgument.register(attachment)
                        args.add(AttachmentArgument.serialize(attachment))
                    }
                    OptionType.USER -> {
                        args.add(UserArgument.serialize(opt.asUser))
                    }
                    OptionType.CHANNEL -> {
                        args.add(ChannelArgument.serialize(opt.asChannel))
                    }
                    OptionType.ROLE -> {
                        args.add(RoleArgument.serialize(opt.asRole))
                    }
                    OptionType.MENTIONABLE -> {
                        val mentionable = opt.asMentionable
                        MentionableArgument.register(mentionable)
                        args.add(MentionableArgument.serialize(mentionable))
                    }
                    OptionType.NUMBER -> {
                        args.add("${opt.asDouble}")
                    }
                }
            }

            input += args.joinToString(" ")
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

        @JvmStatic
        protected fun literal(name: String): LiteralArgumentBuilder<CommandSource> {
            return LiteralArgumentBuilder.literal(name)
        }

        @JvmStatic
        protected fun <T> argument(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<CommandSource, T> {
            return RequiredArgumentBuilder.argument(name, type)
        }
    }

    abstract override fun registerLocalizations(generator: ILanguageGenerator)

    abstract fun register(dispatcher: CommandDispatcher<CommandSource>)
}