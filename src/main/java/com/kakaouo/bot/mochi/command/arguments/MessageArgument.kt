package com.kakaouo.bot.mochi.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.dv8tion.jda.api.entities.Message
import java.util.NoSuchElementException

class MessageArgument : ArgumentType<Message>, ISnowflakeArgument {
    companion object {
        private val map = mutableMapOf<String, Message>()

        fun register(message: Message) {
            map[message.id] = message
        }
    }

    override fun parse(reader: StringReader): Message? {
        val snowflake = reader.readSnowflake()
        val result = map[snowflake] ?:
            throw NoSuchElementException("Message ID $snowflake is not registered")
        map.remove(snowflake)
        return result
    }
}