package com.kakaouo.bot.mochi.utils.terminal

import com.kakaouo.bot.mochi.texts.LiteralText
import com.kakaouo.bot.mochi.texts.Text
import com.kakaouo.bot.mochi.texts.TextColor
import com.kakaouo.bot.mochi.utils.Utils.asNullable
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.ParsedCommandNode
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.utils.AttributedString
import java.lang.Exception
import java.lang.StringBuilder
import java.util.regex.Pattern

object BrigadierTerminal {
    fun <T> createCompleter(dispatcher: CommandDispatcher<T>, source: T): Completer {
        return Completer { _, line, candidates ->
            val input = line.line()
            val result = dispatcher.parse(input, source)
            val suggestions = dispatcher.getCompletionSuggestions(result).get()
                .list.map { it.apply(input) }
            candidates.addAll(suggestions.map { Candidate(it) })
        }
    }

    fun <T> createHighlighter(dispatcher: CommandDispatcher<T>, source: T): Highlighter {
        return object : Highlighter {
            override fun highlight(reader: LineReader?, buffer: String?): AttributedString {
                val colors = arrayOf(
                    TextColor.AQUA, TextColor.YELLOW, TextColor.GREEN, TextColor.PURPLE
                )
                var colorIndex = 0

                val result = dispatcher.parse(buffer, source)
                val reader = result.reader
                var context = result.context.asNullable()
                var lastProcessedNode: ParsedCommandNode<T>? = null

                var started = false
                var startFrom = 0
                val sb = StringBuilder()

                fun writeWithSuggestion(text: Text<*>) {
                    sb.append(text.toAscii())
                }

                fun build(): AttributedString {
                    return AttributedString.fromAnsi(sb.toString())
                }

                while (context != null) {
                    for (node in context.nodes) {
                        if (node == null) {
                            Terminal.writeLine("node is null??")
                            continue
                        }

                        val range = node.range
                        try {
                            if (started) sb.append(' ')
                            startFrom = range.end

                            val useColor = node.node !is LiteralCommandNode<T>
                            val text = LiteralText.of(range.get(reader)).setColor(if (useColor) colors[colorIndex++] else null)!!
                            sb.append(text.toAscii())

                            colorIndex %= colors.size
                            started = true
                            lastProcessedNode = node
                        } catch (ex: Exception) {
                            writeWithSuggestion(LiteralText.of(buffer!!.substring(range.start..range.end))
                                .setColor(TextColor.RED)!!)
                            return build()
                        }
                    }

                    val child = context.child
                    if (child == null && reader.canRead()) {
                        val last = context.nodes.lastOrNull()
                        val nextNode = last?.node?.children?.firstOrNull()
                        val usage = if (nextNode is ArgumentCommandNode<T, *>) nextNode.usageText else null

                        writeWithSuggestion(LiteralText.of(reader.string.substring(startFrom))
                            .setColor(TextColor.RED)!!)
                        if (usage != null) {
                            sb.append(LiteralText.of(" :$usage").setColor(TextColor.GRAY)!!.toAscii())
                        }

                        var errMsg = "Incorrect argument"
                        val err = result.exceptions
                        if (err.isNotEmpty()) {
                            errMsg = err.values.first().message!!
                        }

                        sb.append(LiteralText.of(" <- $errMsg").setColor(TextColor.RED)!!.toAscii())
                        return build()
                    }

                    context = child
                }

                if (lastProcessedNode != null && lastProcessedNode.node.command == null) {
                    sb.append(LiteralText.of(" <- Incomplete command").setColor(TextColor.RED)!!.toAscii())
                }

                return build()
            }

            override fun setErrorPattern(errorPattern: Pattern?) {

            }

            override fun setErrorIndex(errorIndex: Int) {

            }
        }
    }
}