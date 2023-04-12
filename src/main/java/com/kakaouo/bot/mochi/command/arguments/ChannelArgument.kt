package com.kakaouo.bot.mochi.command.arguments

import com.kakaouo.bot.mochi.Mochi
import com.mojang.brigadier.StringReader
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion

class ChannelArgument : IArgumentType<GuildChannelUnion>, ISnowflakeArgument {
    companion object {
        fun serialize(channel: Channel): String {
            return channel.id
        }
    }

    override fun parse(reader: StringReader): GuildChannelUnion {
        val snowflake = reader.readSnowflake()
        return Mochi.instance.client.getChannelById(GuildChannelUnion::class.java, snowflake) ?:
            throw IllegalArgumentException("Channel $snowflake not found")
    }
}

