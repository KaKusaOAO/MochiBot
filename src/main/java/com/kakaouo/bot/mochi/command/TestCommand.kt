package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ICommandSender
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.bot.mochi.utils.MochiUtils.setVoiceMessageFlag
import com.kakaouo.mochi.utils.UtilsKt
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.BodyHandlers

object TestCommand : Command(), IDiscordCommand {
    const val COMMAND_NAME = "test"
    const val ENABLED = false

    private object L {
        private const val PREFIX = "command.$COMMAND_NAME"
        const val CMD_DESC = "$PREFIX.description"
        const val SUCCESS_MESSAGE = "$PREFIX.success"
    }

    override fun registerLocalizations(generator: ILanguageGenerator) {
        generator
            .addEntry(L.CMD_DESC, "<nickname>的測試指令uwu")
            .addEntry(L.SUCCESS_MESSAGE, "早安世界uwu")
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal(COMMAND_NAME)
            .executes(TestCommand::execute))
    }

    private fun execute(context: CommandContext<CommandSource>): Int {
        val source = context.source
        val channel = source.channel ?: return 1

        UtilsKt.asyncDiscard i@{
            val stream = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                    .uri(URI("https://cdn.discordapp.com/attachments/1063755842886840350/1096690665414463548/voice-message.ogg"))
                    .GET().build(),
                BodyHandlers.ofInputStream()
            ).body()

            channel.sendMessage(MessageCreateBuilder()
                .addFiles(FileUpload.fromData(stream, "voice-message.ogg"))
                .setVoiceMessageFlag()
                .build()).submit().await()
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