package com.kakaouo.bot.mochi.utils.dsl

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

class CommandSlashOptionDSL(
    private val type: OptionType,
    private val name: String,
    private val desc: String) {

    var required = false
    var autoComplete = false

    fun commit(data: SlashCommandData) {
        data.addOption(type, name, desc, required, autoComplete)
    }
}

class CommandSlashDSL(private val data: SlashCommandData) {
    fun option(type: OptionType, name: String, desc: String, block: CommandSlashOptionDSL.() -> Unit = {}) {
        val dsl = CommandSlashOptionDSL(type, name, desc)
        dsl.block()
        dsl.commit(data)
    }
}

class CommandDSL(private val actions: CommandListUpdateAction) {
    fun slash(name: String, desc: String, block: CommandSlashDSL.() -> Unit) {
        val command = Commands.slash(name, desc)
        val dsl = CommandSlashDSL(command)
        dsl.block()
        actions.addCommands(command)
    }
}

class CommandListDSL(private val actions: CommandListUpdateAction) {
    fun commands(block: CommandDSL.() -> Unit) {
        val dsl = CommandDSL(actions)
        dsl.block()
    }
}

fun CommandListUpdateAction.toDsl(): CommandListDSL {
    return CommandListDSL(this)
}