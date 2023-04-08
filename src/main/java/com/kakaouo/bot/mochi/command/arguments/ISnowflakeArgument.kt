package com.kakaouo.bot.mochi.command.arguments

import com.mojang.brigadier.StringReader

interface ISnowflakeArgument {
    fun StringReader.readSnowflake(): String {
        var digits = ""
        while (canRead() && peek().isDigit()) {
            digits += read()
        }

        return digits
    }
}

