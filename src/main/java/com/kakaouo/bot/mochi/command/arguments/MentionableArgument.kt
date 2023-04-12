package com.kakaouo.bot.mochi.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.dv8tion.jda.api.entities.IMentionable
import java.util.NoSuchElementException

class MentionableArgument : IArgumentType<IMentionable>, ISnowflakeArgument {
    companion object {
        private val map = mutableMapOf<String, IMentionable>()

        fun register(mentionable: IMentionable) {
            map[mentionable.id] = mentionable
        }

        fun serialize(mentionable: IMentionable): String {
            return mentionable.id
        }
    }

    override fun parse(reader: StringReader): IMentionable {
        val snowflake = reader.readSnowflake()
        val result = map[snowflake] ?:
            throw NoSuchElementException("Mentionable ID $snowflake is not registered")
        map.remove(snowflake)
        return result
    }
}