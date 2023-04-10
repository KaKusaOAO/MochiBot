package com.kakaouo.bot.mochi.command.sender

import com.kakaouo.bot.mochi.Mochi
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

class DiscordMessageSender(override val source: Message) : IDiscordCommandSender {
    override val author = source.author
    override val member = source.member
    override val channel get() = source.channel
    override val guild = member?.guild
    override val i18n get() = guildManager?.i18n ?: Mochi.i18n

    override suspend fun respond(
        text: String?,
        ephemeral: Boolean,
        embed: MessageEmbed?,
        component: LayoutComponent?,
        allowedMentions: Collection<MentionType>?
    ) {
        val builder = MessageCreateBuilder()
            .setContent(text)
            .setAllowedMentions(allowedMentions)
            .mentionRepliedUser(false)

        if (embed != null) {
            builder.setEmbeds(embed)
        }

        if (component != null) {
            builder.setComponents(component)
        }

        source.reply(builder.build()).submit().await()
    }
}