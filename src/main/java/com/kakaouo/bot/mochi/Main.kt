package com.kakaouo.bot.mochi

import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ConsoleCommandSender
import com.kakaouo.mochi.texts.AsciiColor
import com.kakaouo.mochi.texts.LiteralText
import com.kakaouo.mochi.texts.RgbAsciiColor
import com.kakaouo.mochi.texts.TextColor
import com.kakaouo.mochi.utils.Logger
import com.kakaouo.mochi.utils.Utils
import com.kakaouo.mochi.utils.terminal.BrigadierTerminal
import com.kakaouo.mochi.utils.terminal.Terminal
import com.mojang.brigadier.CommandDispatcher
import org.jline.reader.UserInterruptException
import org.jline.reader.impl.DefaultHighlighter
import org.jline.utils.AttributedStyle
import java.io.InterruptedIOException
import kotlin.system.exitProcess

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        if (System.console() == null) {
            System.setProperty("jansi.passthrough", "true")
        }

        Utils.setDefaultRootDirName("bot")
        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            Logger.error(ex.stackTraceToString())
            Logger.error("Exiting...")
            exitProcess(-1)
        }

        Logger.setLevel(Logger.Level.VERBOSE)
        Logger.addLogListener(Logger::logToEmulatedTerminalAsync)
        Logger.runThreaded()

        val bot = Mochi()

        while (true) {
            try {
                val dispatcher = bot.dispatcher
                val source = CommandSource(ConsoleCommandSender())
                val line = Terminal.readLine(
                    LiteralText.of("> "),
                    BrigadierTerminal.createCompleter(dispatcher, source),
                    BrigadierTerminal.createHighlighter(dispatcher, source)
                )
                bot.handleCommandLine(line)
            } catch (ex: Throwable) {
                when (ex) {
                    is UserInterruptException, is InterruptedIOException -> {
                        exitProcess(0)
                    }
                }
                Logger.error("An error occurred trying to read and handle command line.")
                Logger.error(ex)
            }
        }
    }
}
