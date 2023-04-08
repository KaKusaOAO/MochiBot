package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ConsoleCommandSender
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

object ExecuteCommand : Command(), IDiscordCommand {
    const val COMMAND_NAME = "execute"
    const val ARG_SUBCOMMAND = "subcommand"

    private object L {
        private const val PREFIX = "command.$COMMAND_NAME"
        const val CMD_DESC = "$PREFIX.description"
        const val CMD_ARG_SUBCOMMAND_DESC = "$PREFIX.$ARG_SUBCOMMAND.description"
    }

    override fun registerLocalizations(generator: ILanguageGenerator) {
        generator
            .addEntry(L.CMD_DESC, "<nickname>想玩指令！！")
            .addEntry(L.CMD_ARG_SUBCOMMAND_DESC, "喔天啊... <nickname>要打什麼東西啊... 好難打喔...")
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        val root = dispatcher.register(literal<CommandSource?>(COMMAND_NAME)
            .requires { it.sender is ConsoleCommandSender })
    }

    override fun registerDiscord(): List<CommandData> {
        val bot = Mochi.instance
        val map = mutableMapOf<DiscordLocale, String>()

        val locales = arrayOf(
            DiscordLocale.CHINESE_TAIWAN,
            DiscordLocale.ENGLISH_US
        )
        for (l in locales) {
            map[l] = bot.getI18nFor(l).of(L.CMD_DESC).trim()
        }

        return listOf(
            Commands.slash(COMMAND_NAME, Mochi.i18n.of(L.CMD_DESC))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(
                    Permission.ADMINISTRATOR
                ))
                .setDescriptionLocalizations(map)
                .addOption(OptionType.STRING, ARG_SUBCOMMAND, Mochi.i18n.of(L.CMD_ARG_SUBCOMMAND_DESC), true, true)
        )
    }
}

