package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ConsoleCommandSender
import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.bot.mochi.config.MochiConfig
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.mochi.utils.Logger
import com.kakaouo.mochi.utils.UtilsKt
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
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
                Pair("base-nsfw", "要再[深入<nickname>](<link>)一點點嗎...！！<3"))
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal(COMMAND_NAME)
            .executes(HelpCommand::execute))
    }

    private fun execute(context: CommandContext<CommandSource>): Int {
        val source = context.source
        val sender = source.sender

        when (sender) {
            is IDiscordCommandSender -> {
                UtilsKt.asyncDiscard {
                    // 帶使用者到 GitHub 說明頁面
                    sender.respond(embed = sender.createStyledEmbed {
                        val config = MochiConfig.instance.data
                        setDescription(sender.i18n.of(L.HELP_MESSAGE, mapOf(
                            Pair("link", "${config.repository.url}/blob/master/docs/help.md")
                        )))
                    }.build())
                }
            }

            is ConsoleCommandSender -> {
                // 直接輸出所有指令的用法
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
        return listOf(
            Commands.slash(COMMAND_NAME, Mochi.i18n.of(L.CMD_DESC))
                .setDescriptionLocalizations(makeLocaleMap(L.CMD_DESC))
        )
    }
}

