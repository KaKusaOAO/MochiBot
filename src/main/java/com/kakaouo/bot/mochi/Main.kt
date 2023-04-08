package com.kakaouo.bot.mochi

import com.kakaouo.bot.mochi.config.MochiConfig
import com.kakaouo.bot.mochi.texts.LiteralText
import com.kakaouo.bot.mochi.utils.Logger
import com.kakaouo.bot.mochi.utils.Utils
import com.kakaouo.bot.mochi.utils.terminal.BrigadierTerminal
import com.kakaouo.bot.mochi.utils.terminal.Terminal
import com.mojang.brigadier.CommandDispatcher
import kotlin.system.exitProcess

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            Logger.error(ex.stackTraceToString())
            Logger.error("Exiting...")
            exitProcess(-1)
        }

        Logger.level = Logger.Level.VERBOSE
        Logger.addLogListener { entry ->
            Logger.logToConsoleAsync(entry)
        }
        Logger.runThreaded()

        Mochi()

        while (true) {
            val dispatcher = CommandDispatcher<Any>()
            val source = this

            Terminal.readLine(LiteralText.of("> "),
                BrigadierTerminal.createCompleter(dispatcher, source),
                BrigadierTerminal.createHighlighter(dispatcher, source))
        }
    }
}
