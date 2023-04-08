package com.kakaouo.bot.mochi.command.sender

import com.kakaouo.bot.mochi.i18n.I18n

interface ICommandSender {
    val name: String
    val i18n: I18n
}