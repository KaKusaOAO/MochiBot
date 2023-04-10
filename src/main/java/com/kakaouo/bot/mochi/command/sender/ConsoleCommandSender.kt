package com.kakaouo.bot.mochi.command.sender

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.mochi.utils.Logger

class ConsoleCommandSender : ICommandSender {
    override val name = "Console"
    override val i18n = Mochi.i18n

    override suspend fun respond(message: String, option: ICommandSender.RespondOption) {
        Logger.info(message)
    }

    override suspend fun respondError(message: String, option: ICommandSender.RespondOption) {
        Logger.error(message)
    }
}