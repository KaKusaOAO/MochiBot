package com.kakaouo.bot.mochi.texts

import java.util.*

class LiteralText(var text: String) : Text<LiteralText?>() {
    override fun resolveThis(): LiteralText {
        return this
    }

    override fun createCopy(): LiteralText {
        return LiteralText(text)
    }

    override fun toAscii(): String {
        val extra = super.toAscii()
        val color = Optional.ofNullable(color).orElse(this.parentColor).toAsciiCode()
        return color + text + extra
    }

    companion object {
        @JvmStatic
        fun of(text: String): LiteralText {
            return LiteralText(text)
        }

        fun String?.toText(): LiteralText {
            if (this == null) {
                return of("<null>").setColor(TextColor.RED)!!
            }
            return of(this)
        }
    }
}
