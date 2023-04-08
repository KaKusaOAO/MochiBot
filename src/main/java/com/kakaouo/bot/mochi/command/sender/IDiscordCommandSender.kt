package com.kakaouo.bot.mochi.command.sender

import com.kakaouo.bot.mochi.Mochi
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.components.LayoutComponent

interface IDiscordCommandSender : ICommandSender {
    val author: User
    val member: Member?
    val channel: MessageChannel
    val source: Message?
    val guild: Guild?

    val guildManager get() = Mochi.instance.getGuildManager(guild)

    override val name get() = author.name + "#" + author.discriminator

    suspend fun respond(text: String? = null,
                ephemeral: Boolean = false,
                embed: MessageEmbed? = null,
                component: LayoutComponent? = null,
                allowedMentions: Collection<MentionType>? = null)

    fun createStyledEmbed(block: EmbedBuilder.() -> Unit): EmbedBuilder {
        return Mochi.instance.createStyledEmbed(this, block)
    }
}

