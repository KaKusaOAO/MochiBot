package com.kakaouo.bot.mochi.command.exceptions

import com.mojang.brigadier.Message

class DispatcherParseFailureMessage(val original: Any?) : Message {
    override fun getString(): String {
        return "無法解析指令: $original"
    }
}

