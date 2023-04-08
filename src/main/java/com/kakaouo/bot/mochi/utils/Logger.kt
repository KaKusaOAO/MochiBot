package com.kakaouo.bot.mochi.utils

import com.kakaouo.bot.mochi.texts.LiteralText
import com.kakaouo.bot.mochi.texts.LiteralText.Companion.toText
import com.kakaouo.bot.mochi.texts.Text
import com.kakaouo.bot.mochi.texts.TextColor
import com.kakaouo.bot.mochi.texts.TranslateText
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Semaphore
import java.util.function.Consumer

object Logger {
    var level = Level.VERBOSE
    private val lock = Semaphore(1)
    private val listeners = ArrayList<AsyncLogListener>()
    private var bootstrapped = false
    private var thread: Thread? = null
    private val queue = ConcurrentLinkedQueue<Runnable>()

    private fun createPrefixFormat(): TranslateText {
        return TranslateText.of("%3\$s - %1\$s %2\$s")
    }

    fun addLogListener(listener: AsyncLogListener) {
        listeners.add(listener)
    }

    fun removeLogListener(listener: AsyncLogListener) {
        listeners.remove(listener)
    }

    fun runThreaded() {
        if (bootstrapped) return
        bootstrapped = true

        val t = Thread {
            runEventLoop()
        }.apply {
            name = "Logger Thread"
            isDaemon = true
        }
        thread = t
        t.start()
    }

    private fun runEventLoop() {
        while (bootstrapped) {
            while (queue.isEmpty()) {
                Thread.sleep(2)
                Thread.yield()
            }

            pollEvents()
            Thread.yield()
        }
    }

    private fun pollEvents() {
        if (!bootstrapped) {
            throw RuntimeException("Logger is not bootstrapped")
        }
        if (thread != Thread.currentThread()) {
            throw RuntimeException("pollEvents() called from wrong thread")
        }

        while (queue.isNotEmpty()) {
            val action = queue.poll()
            action.run()
            Thread.yield()
        }
    }

    private fun internalOnLogged(data: Entry) {
        fun handleException(handler: AsyncLogListener, ex: Throwable) {

        }

        for (handler in listeners) {

        }
    }

    fun logToConsoleAsync(entry: Entry): CompletableFuture<Void?> {
        val level = entry.level
        val text = entry.text
        val color = entry.color
        val name = entry.tag
        val thread = entry.thread
        if (Logger.level.ordinal > level.ordinal) return CompletableFuture.completedFuture(null)
        try {
            lock.acquire()
            println(getDefaultFormattedLine(text, color, name, thread).toAscii())
            lock.release()
        } catch (ex: InterruptedException) {
            // ignored
        }
        return CompletableFuture.completedFuture(null)
    }

    fun getDefaultFormattedLine(t: Text<*>, color: TextColor?, name: Text<*>, thread: Thread): Text<*> {
        val _name = name.copy()
        val f = createPrefixFormat()
        _name.color = color
        val threadText: Text<*> = TranslateText.of("[%s] ")
            .addWith(
                LiteralText.of(thread.name + "@" + thread.id)
                    .setColor(TextColor.DARK_GRAY)
            )
            .setColor(TextColor.DARK_GRAY)
        val tag: Text<*> = TranslateText.of("[%s]").addWith(_name).setColor(color)
        threadText.addExtra(tag)
        val now = SimpleDateFormat().format(Date())
        return f.addWith(threadText, t.copy(), LiteralText.of(now))
    }

    private fun log(level: Level, text: Text<*>, color: TextColor, name: Text<*>) {
        listeners.forEach(Consumer { listener: AsyncLogListener ->
            val entry = Entry(level, text.copy(), name.copy(), color, Thread.currentThread(), Instant.now())
            listener.invoke(entry)
        })
    }

    private val callSourceClass: Class<*>?
        get() {
            val stackTrace = Thread.currentThread().stackTrace
            return try {
                Class.forName(stackTrace[4].className)
            } catch (ex: ClassNotFoundException) {
                null
            }
        }

    private fun convertToText(obj: Any?): Text<*> {
        if (obj is Text<*>) return obj
        if (obj is Throwable) return obj.stackTraceToString().toText()
        if (obj == null) return LiteralText.of("<null>").setColor(TextColor.RED)!!
        return LiteralText.of(obj.toString())
    }

    fun verbose(text: Any?, tag: Text<*>? = null) {
        val t = tag ?: Text.representClass(
            callSourceClass, null
        )

        log(Level.VERBOSE, convertToText(text), TextColor.DARK_GRAY, t)
    }

    fun log(text: Any?, tag: Text<*>? = null) {
        val t = tag ?: Text.representClass(
            callSourceClass, null
        )

        log(Level.LOG, convertToText(text), TextColor.GRAY, t)
    }

    fun info(text: Any?, tag: Text<*>? = null) {
        val t = tag ?: Text.representClass(
            callSourceClass, null
        )

        log(Level.INFO, convertToText(text), TextColor.GREEN, t)
    }

    fun error(text: Any?, tag: Text<*>? = null) {
        val t = tag ?: Text.representClass(
            callSourceClass, null
        )
        log(Level.ERROR, convertToText(text), TextColor.RED, t)
    }

    fun warn(text: Any?, tag: Text<*>? = null) {
        val t = tag ?: Text.representClass(
            callSourceClass, null
        )
        log(Level.WARN, convertToText(text), TextColor.GOLD, t)
    }

    fun interface AsyncLogListener {
        operator fun invoke(entry: Entry): CompletableFuture<Void?>
    }

    data class Entry(val level: Level,
                     val text: Text<*>,
                     val tag: Text<*>,
                     val color: TextColor,
                     val thread: Thread,
                     val timestamp: Instant)

    enum class Level {
        VERBOSE,
        LOG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }
}
