package com.kakaouo.bot.mochi.command.sender

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.utils.Utils.toCoroutine
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder

class DiscordInteractionSender(val interaction: CommandInteraction): IDiscordCommandSender {
    override val author get() = interaction.user
    override val member get() = interaction.member
    override val channel get() = interaction.messageChannel
    override val guild get() = interaction.guild
    override val source get() = null
    override val i18n get() = Mochi.instance.getI18nFor(interaction.userLocale)

    private var deferred = false

    suspend fun defer() {
        if (deferred) return

        interaction.deferReply().submit().toCoroutine()
        deferred = true
    }

    override suspend fun respond(
        text: String?,
        ephemeral: Boolean,
        embed: MessageEmbed?,
        component: LayoutComponent?,
        allowedMentions: Collection<Message.MentionType>?
    ) {
        if (!deferred) {
            interaction.deferReply(ephemeral).submit().toCoroutine()
            deferred = true
        }

        val builder = MessageEditBuilder()
            .setContent(text)
            .setAllowedMentions(allowedMentions)

        if (embed != null) {
            builder.setEmbeds(embed)
        }

        if (component != null) {
            builder.setComponents(component)
        }

        interaction.hook.editOriginal(builder.build()).submit().toCoroutine()
    }
}