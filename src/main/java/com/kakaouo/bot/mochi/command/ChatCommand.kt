package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.arguments.MessageArgument
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.mochi.utils.Logger
import com.kakaouo.mochi.utils.UtilsKt
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

object ChatCommand : Command(), IDiscordCommand {
    const val COMMAND_NAME = "chat"
    const val ARG_MESSAGE_NAME = "message"

    private object L {
        private const val PREFIX = "command.$COMMAND_NAME"
        const val CMD_NAME = "$PREFIX.description"
    }

    override fun registerLocalizations(generator: ILanguageGenerator) {
        generator
            .addEntry(L.CMD_NAME, "讓<nickname>回覆")
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal(COMMAND_NAME)
            .then(argument(ARG_MESSAGE_NAME, MessageArgument())
                .executes(ChatCommand::execute)
            )
        )
    }

    private fun execute(context: CommandContext<CommandSource>): Int {
        val source = context.source
        val sender = source.sender
        val message = context.getArgument<Message>("message")

        if (sender !is IDiscordCommandSender) {
            UtilsKt.asyncDiscard {
                source.respondError("Only Discord users can use the chat feature.")
            }
            return 0
        }

        val chatBot = Mochi.instance.chatBotManager
        chatBot.handleCheckedMessage(sender, message, sender.i18n)
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
            map[l] = bot.getI18nFor(l).of(L.CMD_NAME).trim()
        }

        return listOf(
            Commands.message(COMMAND_NAME)
                .setNameLocalizations(map)
        )
    }
}