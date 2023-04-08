package com.kakaouo.bot.mochi.command.sender

import com.kakaouo.bot.mochi.Mochi

class ConsoleCommandSender : ICommandSender {
    override val name = "Console"
    override val i18n = Mochi.i18n
}