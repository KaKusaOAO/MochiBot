package com.kakaouo.bot.mochi.command.sender

import com.kakaouo.bot.mochi.i18n.MochiI18n
import net.dv8tion.jda.api.entities.Message.MentionType

interface ICommandSender {
    val name: String
    val i18n: MochiI18n

    fun getAsMention() = name

    data class RespondOption(
        val preferEmbed: Boolean = false,
        val preferEphemeral: Boolean = false,
        val preferNoAuthor: Boolean = false,
        val embedTitle: String? = null,
        val embedAuthorName: String? = null,
        val allowedMentions: List<MentionType>? = null)

    suspend fun respond(message: String, option: RespondOption = RespondOption())
    suspend fun respondError(message: String, option: RespondOption = RespondOption())
}