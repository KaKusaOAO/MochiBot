package com.kakaouo.bot.mochi.texts

import com.kakaouo.mochi.texts.LiteralText
import com.kakaouo.mochi.texts.Text
import com.kakaouo.mochi.texts.TextColor
import com.kakaouo.mochi.texts.TextKt.toText
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.User

object Texts {
    private fun <T: ISnowflake> ofSnowflakeEntity(entity: T?, color: TextColor, nameResolver: (T) -> String): Text<*> {
        val name = if (entity == null) null else nameResolver(entity)
        val t = name.toText().setColor(color)

        if (entity != null) {
            t.addExtra(LiteralText.of(" (${entity.id})").setColor(TextColor.DARK_GRAY))
        }

        return t
    }

    fun ofGuild(guild: Guild?): Text<*> {
        return ofSnowflakeEntity(guild, TextColor.GREEN) { it.name }
    }

    fun ofUser(user: User?): Text<*> {
        return ofSnowflakeEntity(user, TextColor.YELLOW) { it.name }
    }
}