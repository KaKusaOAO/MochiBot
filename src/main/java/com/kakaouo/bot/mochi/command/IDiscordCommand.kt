package com.kakaouo.bot.mochi.command

import net.dv8tion.jda.api.interactions.commands.build.CommandData

interface IDiscordCommand {
    fun registerDiscord(): List<CommandData>
}