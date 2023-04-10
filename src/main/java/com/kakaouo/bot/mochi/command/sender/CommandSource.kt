package com.kakaouo.bot.mochi.command.sender

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.utils.MochiUtils.asMember
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

class CommandSource {
    var sender: ICommandSender private set
    var user: User?
    var guild: Guild?
    val member get() = user.asMember(guild)
    var channel: MessageChannel?
    var voiceChannel: AudioChannel?
    val guildManager get() = Mochi.instance.getGuildManager(guild)
    val i18n get() = sender.i18n

    constructor(
        sender: ICommandSender,
        user: User? = null,
        guild: Guild? = null,
        channel: MessageChannel? = null,
        voiceChannel: AudioChannel? = null
    ) {
        this.sender = sender
        this.user = user
        this.guild = guild
        this.channel = channel
        this.voiceChannel = voiceChannel

        if (sender is IDiscordCommandSender) {
            fillInfoDiscord(sender)
        }
    }

    constructor(other: CommandSource) {
        sender = other.sender
        user = other.user
        guild = other.guild
        channel = other.channel
        voiceChannel = other.voiceChannel
    }

    private fun fillInfoDiscord(sender: IDiscordCommandSender) {
        val author = sender.author
        user = author
        guild = sender.guild
        channel = sender.channel

        val member = member ?: return
        voiceChannel = member.voiceState?.channel
    }

    fun withSender(sender: ICommandSender): CommandSource {
        return clone().apply {
            this@apply.sender = sender
        }
    }

    fun clone(): CommandSource = CommandSource(this)

    suspend fun defer() {
        val sender = sender
        if (sender is IDeferrableSender) {
            sender.defer()
        }
    }

    suspend fun respond(message: String, option: ICommandSender.RespondOption = ICommandSender.RespondOption()) {
        sender.respond(message, option)
    }

    suspend fun respondError(message: String, option: ICommandSender.RespondOption = ICommandSender.RespondOption()) {
        sender.respondError(message, option)
    }
}

