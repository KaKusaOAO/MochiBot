package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.arguments.ChannelArgument
import com.kakaouo.bot.mochi.command.arguments.GuildArgument
import com.kakaouo.bot.mochi.command.arguments.ISnowflakeArgument
import com.kakaouo.bot.mochi.command.arguments.UserArgument
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ConsoleCommandSender
import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData

object ExecuteCommand : Command(), IDiscordCommand {
    const val COMMAND_NAME = "execute"
    const val ARG_SUBCOMMAND = "subcommands"

    private object L {
        private const val PREFIX = "command.$COMMAND_NAME"
        const val CMD_DESC = "$PREFIX.description"
        const val CMD_ARG_SUBCOMMAND_DESC = "$PREFIX.$ARG_SUBCOMMAND.description"
    }

    override fun registerLocalizations(generator: ILanguageGenerator) {
        generator
            .addEntry(L.CMD_DESC, "<nickname>想玩指令！！")
            .addEntry(L.CMD_ARG_SUBCOMMAND_DESC, "喔天啊... <nickname>要打什麼東西啊... 好難打喔...")
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        val root = dispatcher.register(literal(COMMAND_NAME)
            .requires {
                if (it.sender is ConsoleCommandSender) return@requires true

                val sender = it.sender
                sender is IDiscordCommandSender && sender.author.id == "217238973246865408"
            })

        dispatcher.register(literal(COMMAND_NAME)
            .then(literal("run")
                .redirect(dispatcher.root) {
                    val source = it.source
                    source.guild ?: return@redirect source
                    validateSourceGuild(source)
                    validateGuildUser(source)
                    return@redirect source
                }
            )
            .then(addUserNode(root, literal("as")) { s, user ->
                s.user = user
            })
            .then(literal("in")
                .then(literal("channel")
                    .then(literal("text")
                        .then(argument("channelId", ChannelArgument())
                            .fork(root) {
                                val source = it.source
                                val channel = it.getArgument<GuildChannelUnion>("channelId")
                                val guild = channel.guild
                                listOf(source.clone().apply {
                                    this.guild = guild
                                    this.channel = channel as? MessageChannel
                                })
                            }
                        )
                    )
                    .then(literal("voice")
                        .then(argument("channelId", ChannelArgument())
                            .fork(root) {
                                val source = it.source
                                val channel = it.getArgument<GuildChannelUnion>("channelId")
                                val guild = channel.guild
                                listOf(source.clone().apply {
                                    this.guild = guild
                                    this.voiceChannel = channel as? AudioChannel
                                })
                            }
                        )
                    )
                )
                .then(literal("guild")
                    .then(argument("guildId", GuildArgument())
                        .fork(root) {
                            val source = it.source
                            val guild = it.getArgument<Guild>("guildId")
                            listOf(source.clone().apply {
                                this.guild = guild
                            })
                        }
                    )
                )
            )
            .then(addUserNode(root, literal("at")) { s, user ->
                validateGuildUser(s)
                s.voiceChannel = s.member?.voiceState?.channel
            })
        )
    }

    private fun validateGuildUser(source: CommandSource) {
        val user = source.user ?: return
        val guild = source.guild ?: return
        guild.getMember(user) ?: throw UnsupportedOperationException("User not in this guild")
    }

    private fun validateSourceGuild(source: CommandSource) {
        val voiceChannel = source.voiceChannel
        if (voiceChannel != null && voiceChannel.guild != source.guild) {
            throw UnsupportedOperationException("Voice channel guild mismatch")
        }

        val chn = source.channel as? GuildChannel
        if (chn != null && chn.guild != source.guild) {
            throw UnsupportedOperationException("Channel guild mismatch")
        }
    }

    private fun <T : ArgumentBuilder<CommandSource, T>> addUserNode(root: CommandNode<CommandSource>, node: T, block: (CommandSource, User?) -> Unit): T {
        return node
            .then(literal("selfuser")
                .fork(root) {
                    val source = it.source
                    val user = Mochi.instance.user
                    listOf(source.clone().apply {
                        block(this, user)
                    })
                }
            )
            .then(literal("user")
                .then(argument("memberId", UserArgument())
                    .fork(root) {
                        val source = it.source
                        val user = it.getArgument<User>("memberId")
                        listOf(source.clone().apply {
                            block(this, user)
                        })
                    }
                )
            )
            .then(literal("@me")
                .fork(root) {
                    val source = it.source
                    val user = source.user
                    listOf(source.clone().apply {
                        block(this, user)
                    })
                }
            )
    }

    override fun registerDiscord(): List<CommandData> {
        return listOf(
            Commands.slash(COMMAND_NAME, Mochi.i18n.of(L.CMD_DESC))
                .setDescriptionLocalizations(makeLocaleMap(L.CMD_DESC))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(
                    Permission.ADMINISTRATOR
                ))
                .addOptions(OptionData(OptionType.STRING, ARG_SUBCOMMAND, Mochi.i18n.of(L.CMD_ARG_SUBCOMMAND_DESC))
                    .setDescriptionLocalizations(makeLocaleMap(L.CMD_ARG_SUBCOMMAND_DESC))
                    .setRequired(true)
                    .setAutoComplete(true)
                )
        )
    }
}

