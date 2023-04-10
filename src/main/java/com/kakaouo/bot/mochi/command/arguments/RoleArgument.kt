package com.kakaouo.bot.mochi.command.arguments

import com.kakaouo.bot.mochi.Mochi
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.dv8tion.jda.api.entities.Role

class RoleArgument : ArgumentType<Role>, ISnowflakeArgument {
    override fun parse(reader: StringReader): Role {
        val snowflake = reader.readSnowflake()
        return Mochi.instance.client.getRoleById(snowflake) ?:
            throw IllegalArgumentException("Role $snowflake not found")
    }
}