package com.kakaouo.bot.mochi.i18n

import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.mochi.i18n.IPlaceholder

class SenderPlaceholder(val sender: IDiscordCommandSender, val name: String): IPlaceholder {
    override fun writePlaceholders(map: MutableMap<String, Any>) {
        map[name] = this
    }

    override fun toString(): String {
        return sender.getAsMention()
    }
}
