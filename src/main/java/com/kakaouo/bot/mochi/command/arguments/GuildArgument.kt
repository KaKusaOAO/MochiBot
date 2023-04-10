package com.kakaouo.bot.mochi.command.arguments

import com.kakaouo.bot.mochi.Mochi
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.dv8tion.jda.api.entities.Guild

class GuildArgument : ArgumentType<Guild>, ISnowflakeArgument {
    override fun parse(reader: StringReader): Guild {
        val snowflake = reader.readSnowflake()
        return Mochi.instance.client.getGuildById(snowflake) ?:
            throw IllegalArgumentException("Guild $snowflake not found")
    }
}