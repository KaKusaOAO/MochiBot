package com.kakaouo.bot.mochi.texts

import com.kakaouo.mochi.texts.LiteralText
import com.kakaouo.mochi.texts.Text
import com.kakaouo.mochi.texts.TextColor
import com.kakaouo.mochi.texts.TranslateText

class TextBuilder private constructor() {
    private var text: Text<*> = LiteralText.of("")

    open class GenericTextDsl(builder: TextBuilder) {
        private val text: Text<*> = builder.text

        fun color(color: TextColor) {
            text.setColor(color)
        }

        fun extra(content: Text<*>) {
            text.addExtra(content)
        }

        fun extra(block: Dsl.() -> Unit) {
            val e = TextBuilder(block)
            text.addExtra(e)
        }
    }

    class LiteralDsl(builder: TextBuilder) : GenericTextDsl(builder) {
        // empty?
    }

    class TranslatableDsl(builder: TextBuilder) : GenericTextDsl(builder) {
        private val text: TranslateText = builder.text as TranslateText

        fun with(content: Text<*>) {
            text.addWith(content)
        }

        fun with(block: Dsl.() -> Unit) {
            val w = TextBuilder(block)
            text.addWith(w)
        }
    }

    class Dsl(private val builder: TextBuilder) {
        private var isBound = false

        fun literal(content: String, block: LiteralDsl.() -> Unit = {}) {
            if (isBound) return

            builder.text = LiteralText.of(content)
            val dsl = LiteralDsl(builder)
            dsl.block()
            isBound = true
        }

        fun translate(format: String, block: TranslatableDsl.() -> Unit = {}) {
            if (isBound) return

            builder.text = TranslateText.of(format)
            val dsl = TranslatableDsl(builder)
            dsl.block()
            isBound = true
        }
    }

    companion object {
        operator fun invoke(block: Dsl.() -> Unit): Text<*> {
            val builder = TextBuilder()
            val dsl = Dsl(builder)
            dsl.block()
            return builder.text
        }
    }
}