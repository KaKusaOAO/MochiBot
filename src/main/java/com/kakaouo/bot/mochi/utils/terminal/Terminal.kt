package com.kakaouo.bot.mochi.utils.terminal

import com.kakaouo.bot.mochi.texts.Text
import org.jline.reader.Completer
import org.jline.reader.Highlighter
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

object Terminal {
    val terminal: Terminal

    init {
        val builder = TerminalBuilder.builder()
        terminal = builder.build()
    }

    fun writeLine(text: Text<*>) {
        writeLine(text.toAscii())
    }

    fun writeLine(text: String) {
        // Hope it works?
        terminal.writer().println(text)
    }

    fun readLine(prompt: Text<*>? = null, completer: Completer, highlighter: Highlighter): String {
        val reader = LineReaderBuilder.builder()
            .highlighter(highlighter)
            .completer(completer)
            .terminal(terminal)
            .build()

        val p = prompt?.toAscii()
        val line = reader.readLine(p)

        // Should we clean up the reader in some way?
        return line
    }
}

