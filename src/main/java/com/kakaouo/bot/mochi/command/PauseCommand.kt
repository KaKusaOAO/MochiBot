package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ICommandSender
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.mochi.utils.UtilsKt
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

object PauseCommand : Command(), IDiscordCommand, IPlayerBaseCommand {
    const val COMMAND_NAME = "pause"

    private object L {
        private const val PREFIX = "command.$COMMAND_NAME"
        const val CMD_DESC = "$PREFIX.description"
        const val SUCCESS_MESSAGE = "$PREFIX.success"
    }

    override fun registerLocalizations(generator: ILanguageGenerator) {
        generator
            .addEntry(L.CMD_DESC, "<nickname>要施展時間暫停術－－砸襪魯抖！！！")
            .addEntry(L.SUCCESS_MESSAGE, "砸襪魯抖——！！")
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal(COMMAND_NAME)
            .executes(PauseCommand::execute))
    }

    private fun execute(context: CommandContext<CommandSource>): Int {
        val source = context.source
        UtilsKt.asyncDiscard i@ {
            if (!canExecute(source)) return@i

            val player = source.guildManager?.playerManager
            if (player == null) {
                source.respondError(
                    "You have to be in a guild to do that.", ICommandSender.RespondOption(
                        preferEmbed = true
                    )
                )
                return@i
            }

            player.pause()
            source.respond(
                source.i18n.of(L.SUCCESS_MESSAGE), ICommandSender.RespondOption(
                    preferEmbed = true
                )
            )
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

