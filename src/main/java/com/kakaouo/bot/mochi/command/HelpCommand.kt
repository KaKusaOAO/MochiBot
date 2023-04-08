package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ConsoleCommandSender
import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.bot.mochi.config.MochiConfig
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.bot.mochi.utils.Logger
import com.kakaouo.bot.mochi.utils.Utils
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.context.CommandContext
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

object HelpCommand : Command(), IDiscordCommand {
    const val COMMAND_NAME = "help"

    private object L {
        private const val PREFIX = "command.$COMMAND_NAME"
        const val CMD_DESC = "$PREFIX.description"
        const val HELP_MESSAGE = "$PREFIX.success"
    }

    override fun registerLocalizations(generator: ILanguageGenerator) {
        generator
            .addEntry(L.CMD_DESC, "知道如何和<nickname>溝通就已經踏出第一步了喔！")
            .addEntry(L.HELP_MESSAGE,
                Pair("base", "想更認識<nickname>對吧！[快來吧！！](<link>)"),
                Pair("base-nsfw", "要再[深入<nickname>](<Link>)一點點嗎...！！<3"))
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal<CommandSource?>(COMMAND_NAME)
            .executes { execute(it) })
    }

    private fun execute(context: CommandContext<CommandSource>): Int {
        val source = context.source
        val sender = source.sender

        when (sender) {
            is IDiscordCommandSender -> {
                Utils.asyncDiscard {
                    sender.respond(embed = sender.createStyledEmbed {
                        val config = MochiConfig.instance.data
                        setDescription(sender.i18n.of(L.HELP_MESSAGE, mapOf(
                            Pair("link", "${config.repository.url}/blob/master/docs/help.md")
                        )))
                    }.build())
                }
            }

            is ConsoleCommandSender -> {
                val d = Mochi.instance.dispatcher
                val usages = d.getSmartUsage(d.root, source)
                for (str in usages.values) {
                    Logger.info("Usage: /$str")
                }
            }
        }

        return 1
    }

    override fun registerDiscord(): List<CommandData> {
        val bot = Mochi.instance
        val map = mutableMapOf<DiscordLocale, String>()

        val locales = arrayOf(
            DiscordLocale.CHINESE_TAIWAN,
            DiscordLocale.ENGLISH_US
        )
        for (l in locales) {
            map[l] = bot.getI18nFor(l).of(L.CMD_DESC)
        }

        return listOf(
            Commands.slash(COMMAND_NAME, Mochi.i18n.of(L.CMD_DESC))
                .setDescriptionLocalizations(map)
        )
    }
}

