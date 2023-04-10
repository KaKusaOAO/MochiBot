package com.kakaouo.bot.mochi.command.arguments

import com.kakaouo.bot.mochi.Mochi
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion

class ChannelArgument : ArgumentType<GuildChannelUnion>, ISnowflakeArgument {
    override fun parse(reader: StringReader): GuildChannelUnion {
        val snowflake = reader.readSnowflake()
        return Mochi.instance.client.getChannelById(GuildChannelUnion::class.java, snowflake) ?:
            throw IllegalArgumentException("Channel $snowflake not found")
    }
}

