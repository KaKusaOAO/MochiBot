package com.kakaouo.bot.mochi.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.Semaphore

object MochiUtils {
    fun User?.asMember(guild: Guild?): Member? {
        if (this == null) return null
        return guild?.getMember(this)
    }

    fun <T: AbstractMessageBuilder<*, *>> T.setVoiceMessageFlag(): T {
        val field = AbstractMessageBuilder::class.java.getDeclaredField("messageFlags")
        field.isAccessible = true

        val oldFlag = field.get(this) as Int
        val flag = oldFlag or 8192
        field.set(this, flag)

        return this
    }

    fun <T> Semaphore.runAcquired(block: () -> T): T {
        var acquired = false
        try {
            acquire()
            acquired = true

            return block()
        } finally {
            if (acquired) release()
        }
    }

    fun Duration.format(): String {
        var result = ""
        var usesLeadingZero = false

        var seconds = get(ChronoUnit.SECONDS)
        var minutes = seconds / 60
        val hours = minutes / 60

        seconds %= 60
        minutes %= 60

        if (hours > 0) {
            result += "$hours:"
            usesLeadingZero = true
        }

        val hStr = minutes.toString()
        val hStr2 = if (usesLeadingZero) hStr.padStart(2, '0') else hStr
        result += "$hStr2:"

        val str = seconds.toString()
        val str2 = str.padStart(2, '0')
        result += str2

        return result
    }
}