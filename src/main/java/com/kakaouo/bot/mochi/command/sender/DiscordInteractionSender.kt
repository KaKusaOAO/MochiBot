package com.kakaouo.bot.mochi.command.sender

import com.kakaouo.bot.mochi.Mochi
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder

class DiscordInteractionSender(private val interaction: CommandInteractionPayload): IDiscordCommandSender, IDeferrableSender {
    override val author get() = interaction.user
    override val member get() = interaction.member
    override val channel get() = interaction.messageChannel
    override val guild get() = interaction.guild
    override val source get() = null
    override val i18n get() = Mochi.instance.getI18nFor(interaction.userLocale)

    private var deferred = false
    private var isFollowup = false

    override suspend fun defer() {
        defer(false)
    }

    suspend fun defer(ephemeral: Boolean) {
        if (interaction !is CommandInteraction) return
        if (deferred) return

        interaction.deferReply(ephemeral).submit().await()
        deferred = true
    }

    override suspend fun respond(
        text: String?,
        ephemeral: Boolean,
        embed: MessageEmbed?,
        component: LayoutComponent?,
        allowedMentions: Collection<Message.MentionType>?
    ) {
        if (interaction !is CommandInteraction) return
        if (!deferred) {
            interaction.deferReply(ephemeral).submit().await()
            deferred = true
        }

        val builder = if (isFollowup) MessageCreateBuilder() else MessageEditBuilder()
        builder.setContent(text)
            .setAllowedMentions(allowedMentions)

        if (embed != null) {
            builder.setEmbeds(embed)
        }

        if (component != null) {
            builder.setComponents(component)
        }

        val hook = interaction.hook
        hook.setEphemeral(ephemeral)

        when (builder) {
            is MessageCreateBuilder -> {
                hook.sendMessage(builder.build()).submit().await()
            }
            is MessageEditBuilder -> {
                hook.editOriginal(builder.build()).submit().await()
                isFollowup = true
            }
        }
    }
}