package com.kakaouo.bot.mochi.command.sender

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

class CommandSource {
    var sender: ICommandSender? private set
    var user: User?
    var guild: Guild?
    var channel: MessageChannel?
    var voiceChannel: AudioChannel?

    constructor(
        sender: ICommandSender? = null,
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

        if (author is Member) {
            voiceChannel = author.voiceState?.channel
        }
    }

    fun withSender(sender: ICommandSender): CommandSource {
        return clone().apply {
            this@apply.sender = sender
        }
    }

    fun clone(): CommandSource = CommandSource(this)
}

