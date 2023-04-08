package com.kakaouo.bot.mochi.logging

import com.kakaouo.bot.mochi.texts.LiteralText
import com.kakaouo.bot.mochi.texts.LiteralText.Companion.toText
import com.kakaouo.bot.mochi.texts.Text
import com.kakaouo.bot.mochi.texts.TextColor
import com.kakaouo.bot.mochi.utils.Logger
import org.slf4j.Marker
import org.slf4j.Logger as ILogger

class LoggerWrapper(val tagName: String?) : ILogger {
    companion object {
        var level = Logger.Level.INFO
    }

    private fun String?.toClassOrNormalTag(): Text<*> {
        if (this == null) return LiteralText.of("<null>").setColor(TextColor.RED)!!

        return try {
            val clz = Class.forName(this)
            Text.representClass(clz)
        } catch (ex: ClassNotFoundException) {
            LiteralText.of(this)
        }
    }

    val tag get() = tagName.toClassOrNormalTag()

    override fun getName(): String? {
        return tagName
    }

    override fun isTraceEnabled(): Boolean = false

    override fun isTraceEnabled(marker: Marker?) = false

    private fun processFormat(format: String?, vararg arguments: Any?): String? {
        if (format == null) return null
        return format.replace("{}", "%s").format(*arguments)
    }

    override fun trace(msg: String?) {
        if (level.ordinal > Logger.Level.VERBOSE.ordinal) return
        Logger.verbose(msg.toText(), tag)
    }

    override fun trace(format: String?, arg: Any?) {
        if (level.ordinal > Logger.Level.VERBOSE.ordinal) return
        Logger.verbose(processFormat(format, arg).toText(), tag)
    }

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {
        if (level.ordinal > Logger.Level.VERBOSE.ordinal) return
        Logger.verbose(processFormat(format, arg1, arg2).toText(), tag)
    }

    override fun trace(format: String?, vararg arguments: Any?) {
        if (level.ordinal > Logger.Level.VERBOSE.ordinal) return
        Logger.verbose(processFormat(format, *arguments).toText(), tag)
    }

    override fun trace(msg: String?, t: Throwable?) {
        if (level.ordinal > Logger.Level.VERBOSE.ordinal) return
        Logger.verbose(msg.toText(), tag)
    }

    override fun trace(marker: Marker?, msg: String?) {
        if (level.ordinal > Logger.Level.VERBOSE.ordinal) return
        Logger.verbose(msg.toText(), tag)
    }

    override fun trace(marker: Marker?, format: String?, arg: Any?) {
        if (level.ordinal > Logger.Level.VERBOSE.ordinal) return
        Logger.verbose(processFormat(format, arg).toText(), tag)
    }

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (level.ordinal > Logger.Level.VERBOSE.ordinal) return
        Logger.verbose(processFormat(format, arg1, arg2).toText(), tag)
    }

    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) {
        if (level.ordinal > Logger.Level.VERBOSE.ordinal) return
        Logger.verbose(processFormat(format, *argArray).toText(), tag)
    }

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        if (level.ordinal > Logger.Level.VERBOSE.ordinal) return
        Logger.verbose(msg.toText(), tag)
    }

    override fun isDebugEnabled() = true

    override fun isDebugEnabled(marker: Marker?) = true

    override fun debug(msg: String?) {
        if (level.ordinal > Logger.Level.LOG.ordinal) return
        Logger.log(msg.toText(), tag)
    }

    override fun debug(format: String?, arg: Any?) {
        if (level.ordinal > Logger.Level.LOG.ordinal) return
        Logger.log(processFormat(format, arg).toText(), tag)
    }

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {
        if (level.ordinal > Logger.Level.LOG.ordinal) return
        Logger.log(processFormat(format, arg1, arg2).toText(), tag)
    }

    override fun debug(format: String?, vararg arguments: Any?) {
        if (level.ordinal > Logger.Level.LOG.ordinal) return
        Logger.log(processFormat(format, *arguments).toText(), tag)
    }

    override fun debug(msg: String?, t: Throwable?) {
        if (level.ordinal > Logger.Level.LOG.ordinal) return
        Logger.log(msg.toText(), tag)
    }

    override fun debug(marker: Marker?, msg: String?) {
        if (level.ordinal > Logger.Level.LOG.ordinal) return
        Logger.log(msg.toText(), tag)
    }

    override fun debug(marker: Marker?, format: String?, arg: Any?) {
        if (level.ordinal > Logger.Level.LOG.ordinal) return
        Logger.log(processFormat(format, arg).toText(), tag)
    }

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (level.ordinal > Logger.Level.LOG.ordinal) return
        Logger.log(processFormat(format, arg1, arg2).toText(), tag)
    }

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (level.ordinal > Logger.Level.LOG.ordinal) return
        Logger.log(processFormat(format, *arguments).toText(), tag)
    }

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        if (level.ordinal > Logger.Level.LOG.ordinal) return
        Logger.log(msg.toText(), tag)
    }

    override fun isInfoEnabled(): Boolean = true

    override fun isInfoEnabled(marker: Marker?) = true

    override fun info(msg: String?) {
        Logger.info(msg.toText(), tag)
    }

    override fun info(format: String?, arg: Any?) {
        Logger.info(format.toText(), tag)
    }

    override fun info(format: String?, arg1: Any?, arg2: Any?) {
        Logger.info(format.toText(), tag)
    }

    override fun info(format: String?, vararg arguments: Any?) {
        Logger.info(format.toText(), tag)
    }

    override fun info(msg: String?, t: Throwable?) {
        Logger.info(msg.toText(), tag)
    }

    override fun info(marker: Marker?, msg: String?) {
        Logger.info(msg.toText(), tag)
    }

    override fun info(marker: Marker?, format: String?, arg: Any?) {
        Logger.info(format.toText(), tag)
    }

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        Logger.info(format.toText(), tag)
    }

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        Logger.info(format.toText(), tag)
    }

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {
        Logger.info(msg.toText(), tag)
    }

    override fun isWarnEnabled() = true

    override fun isWarnEnabled(marker: Marker?) = true

    override fun warn(msg: String?) {
        Logger.warn(msg.toText(), tag)
    }

    override fun warn(format: String?, arg: Any?) {
        Logger.warn(format.toText(), tag)
    }

    override fun warn(format: String?, vararg arguments: Any?) {
        Logger.warn(format.toText(), tag)
    }

    override fun warn(format: String?, arg1: Any?, arg2: Any?) {
        Logger.warn(format.toText(), tag)
    }

    override fun warn(msg: String?, t: Throwable?) {
        Logger.warn(msg.toText(), tag)
    }

    override fun warn(marker: Marker?, msg: String?) {
        Logger.warn(msg.toText(), tag)
    }

    override fun warn(marker: Marker?, format: String?, arg: Any?) {
        Logger.warn(format.toText(), tag)
    }

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        Logger.warn(format.toText(), tag)
    }

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
        Logger.warn(format.toText(), tag)
    }

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {
        Logger.warn(msg.toText(), tag)
    }

    override fun isErrorEnabled() = true

    override fun isErrorEnabled(marker: Marker?) = true

    override fun error(msg: String?) {
        Logger.error(msg.toText(), tag)
    }

    override fun error(format: String?, arg: Any?) {
        Logger.error(format.toText(), tag)
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        Logger.error(format.toText(), tag)
    }

    override fun error(format: String?, vararg arguments: Any?) {
        Logger.error(format.toText(), tag)
    }

    override fun error(msg: String?, t: Throwable?) {
        Logger.error(msg, tag)
        Logger.error(t, tag)
    }

    override fun error(marker: Marker?, msg: String?) {
        Logger.error(msg, tag)
    }

    override fun error(marker: Marker?, format: String?, arg: Any?) {
        Logger.error(processFormat(format, arg), tag)
    }

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        Logger.error(processFormat(format, arg1, arg2), tag)
    }

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        Logger.error(processFormat(format, *arguments), tag)
    }

    override fun error(marker: Marker?, msg: String?, t: Throwable?) {
        Logger.error(msg, tag)
        Logger.error(t, tag)
    }

}