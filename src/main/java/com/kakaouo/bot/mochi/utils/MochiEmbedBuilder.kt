package com.kakaouo.bot.mochi.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

class MochiEmbedBuilder : EmbedBuilder() {
    private data class AuthorData(
        var name: String? = null,
        var url: String? = null,
        var iconUrl: String? = null)

    private val authorData = AuthorData()

    override fun setAuthor(name: String?): EmbedBuilder {
        authorData.name = name
        return this
    }

    override fun setAuthor(name: String?, url: String?): EmbedBuilder {
        setAuthor(name)
        authorData.url = url
        return this
    }

    override fun setAuthor(name: String?, url: String?, iconUrl: String?): EmbedBuilder {
        setAuthor(name, url)
        authorData.iconUrl = iconUrl
        return this
    }

    override fun build(): MessageEmbed {
        super.setAuthor(authorData.name, authorData.url, authorData.iconUrl)
        return super.build()
    }
}