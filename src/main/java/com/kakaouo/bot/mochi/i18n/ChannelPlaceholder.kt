package com.kakaouo.bot.mochi.i18n

import com.kakaouo.mochi.i18n.IPlaceholder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel

class ChannelPlaceholder(val channel: Channel) : IPlaceholder {
    override fun writePlaceholders(map: MutableMap<String, Any>) {
        map["channel"] = this
    }

    override fun toString(): String {
        return "<#${channel.id}>"
    }
}

class UserPlaceholder(val user: User, val name: String = "user") : IPlaceholder {
    override fun writePlaceholders(map: MutableMap<String, Any>) {
        map[name] = user
    }

    override fun toString(): String {
        return "<@${user.id}>"
    }
}