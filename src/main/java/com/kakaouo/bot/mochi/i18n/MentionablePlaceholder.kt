package com.kakaouo.bot.mochi.i18n

import com.kakaouo.mochi.i18n.IPlaceholder
import net.dv8tion.jda.api.entities.IMentionable

open class MentionablePlaceholder(val mentionable: IMentionable, val name: String) : IPlaceholder {
    override fun writePlaceholders(map: MutableMap<String, Any>) {
        map[name] = this
    }

    override fun toString(): String {
        return mentionable.asMention
    }
}