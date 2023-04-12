package com.kakaouo.bot.mochi.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException

fun StringReader.expect(str: String) {
    val cur = cursor
    for (c in str) {
        if (!canRead() || read() != c) {
            cursor = cur
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().createWithContext(this, str)
        }
    }
}