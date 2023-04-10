package com.kakaouo.bot.mochi.managers

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.config.GuildConfig
import com.kakaouo.bot.mochi.managers.chat.GuildChatBotManager
import com.kakaouo.bot.mochi.managers.player.PlayerManager
import com.kakaouo.mochi.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import java.io.File

class GuildManager(val guild: Guild) {
    val selfMember get() = guild.selfMember
    val mainColor get() = selfMember.color
    val i18n get() = Mochi.instance.getI18nFor(guild.locale)
    val chatBotManager = GuildChatBotManager(this)
    val config = GuildConfig(this)

    val rootPath get() = "guilds/${guild.id}/"
    val rootDir get() = File(Utils.getRootDirectory(), rootPath)

    // Managers
    val playerManager = PlayerManager(this)

    init {
        config.load()
    }

    fun createStyledEmbed(block: EmbedBuilder.() -> Unit): EmbedBuilder {
        return Mochi.instance.createStyledEmbed(guild, block)
    }
}