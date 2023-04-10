package com.kakaouo.bot.mochi.command.sender

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.Constants
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
    val guild: Guild?
    val member: Member?
    val channel: MessageChannel
    val source: Message?

    val guildManager get() = Mochi.instance.getGuildManager(guild)

    override val name get() = author.name + "#" + author.discriminator

    override fun getMentionString(): String {
        return "<@${author.id}>"
    }

    private suspend fun respond(message: String, option: ICommandSender.RespondOption, error: Boolean) {
        val text = if (option.preferEmbed) null else message
        val embed = if (option.preferEmbed) {
            createStyledEmbed {
                setDescription(message)

                if (error) {
                    setColor(Constants.ERROR_COLOR)
                }

                if (option.preferNoAuthor) {
                    setAuthor(null)
                }

                val title = option.embedTitle
                if (!title.isNullOrEmpty()) {
                    setTitle(title)
                }

                val authorName = option.embedAuthorName
                if (!authorName.isNullOrEmpty() && !option.preferNoAuthor) {
                    setAuthor(option.embedAuthorName)
                }
            }.build()
        } else null
        respond(text = text, ephemeral = option.preferEphemeral, embed = embed, allowedMentions = option.allowedMentions)
    }

    override suspend fun respond(message: String, option: ICommandSender.RespondOption) {
        respond(message, option, false)
    }

    override suspend fun respondError(message: String, option: ICommandSender.RespondOption) {
        respond(message, option, true)
    }

    suspend fun respond(text: String? = null,
                ephemeral: Boolean = false,
                embed: MessageEmbed? = null,
                component: LayoutComponent? = null,
                allowedMentions: Collection<MentionType>? = null)

    fun createStyledEmbed(block: EmbedBuilder.() -> Unit): EmbedBuilder {
        return Mochi.instance.createStyledEmbed(this, block)
    }
}

