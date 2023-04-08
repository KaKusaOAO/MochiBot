package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.arguments.MessageArgument
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ConsoleCommandSender
import com.kakaouo.bot.mochi.command.sender.DiscordInteractionSender
import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.bot.mochi.managers.chat.ChatBotManager
import com.kakaouo.bot.mochi.utils.Logger
import com.kakaouo.bot.mochi.utils.Utils
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

object ChatCommand : Command(), IDiscordCommand {
    const val COMMAND_NAME = "chat"
    const val ARG_CONTENT = "content"

    private object L {
        private const val PREFIX = "command.$COMMAND_NAME"
        const val CMD_NAME = "$PREFIX.description"
    }

    override fun registerLocalizations(generator: ILanguageGenerator) {
        generator
            .addEntry(L.CMD_NAME, "讓<nickname>回覆")
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal<CommandSource?>(COMMAND_NAME)
            .then(argument<CommandSource?, Message?>("message", MessageArgument())
                .executes {
                    execute(it)
                }
            )
        )
    }

    private fun execute(context: CommandContext<CommandSource>): Int {
        val source = context.source
        val sender = source.sender!!
        val message = context.getArgument<Message>("message")

        if (sender !is IDiscordCommandSender) {
            Logger.error("Only Discord users can use the chat feature.")
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