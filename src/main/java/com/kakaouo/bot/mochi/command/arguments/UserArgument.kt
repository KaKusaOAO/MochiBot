package com.kakaouo.bot.mochi.command.arguments

import com.kakaouo.bot.mochi.Mochi
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.dv8tion.jda.api.entities.User

class UserArgument : IArgumentType<User>, ISnowflakeArgument {
    companion object {
        fun serialize(user: User): String {
            return user.id
        }
    }

    override fun parse(reader: StringReader): User {
        val snowflake = reader.readSnowflake()
        return Mochi.instance.client.getUserById(snowflake) ?:
            throw IllegalArgumentException("User $snowflake not found")
    }
}

