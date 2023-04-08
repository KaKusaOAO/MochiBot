package com.kakaouo.bot.mochi.command.exceptions

import com.kakaouo.bot.mochi.MochiException
class CommandMisuseException(message: String,
                             val usage: String,
                             cause: Throwable) : MochiException(message, false, cause)