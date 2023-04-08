package com.kakaouo.bot.mochi.managers

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.config.GuildConfig
import com.kakaouo.bot.mochi.managers.chat.GuildChatBotManager
import com.kakaouo.bot.mochi.utils.Utils
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import java.io.File

class GuildManager(val guild: Guild) {
    val selfMember get() = guild.selfMember
    val mainColor get() = selfMember.color
    val i18n get() = Mochi.instance.getI18nFor(guild.locale)
    val chatBotManager = GuildChatBotManager(this)
    val config: GuildConfig

    val rootPath get() = "guilds/${guild.id}/"
    val rootDir get() = File(Utils.getRootDirectory(), rootPath)

    init {
        config = GuildConfig(this)
        config.load()
    }
}