package com.kakaouo.bot.mochi.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.CommandSyntaxException

// A helper interface to enhance null checks in Kotlin
interface IArgumentType<T> : ArgumentType<T> {
    // This cannot be nullable!
    // If a value cannot be resolved, throw an exception instead.
    @Throws(CommandSyntaxException::class)
    override fun parse(reader: StringReader): T
}