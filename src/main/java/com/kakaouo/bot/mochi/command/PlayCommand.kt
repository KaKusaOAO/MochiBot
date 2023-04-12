package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.arguments.AttachmentArgument
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ICommandSender
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.bot.mochi.i18n.UserPlaceholder
import com.kakaouo.bot.mochi.utils.MochiUtils.format
import com.kakaouo.mochi.utils.Logger
import com.kakaouo.mochi.utils.UtilsKt
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.sedmelluq.discord.lavaplayer.tools.Units
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.time.Duration
import java.time.temporal.ChronoUnit

object PlayCommand : Command(), IDiscordCommand, IPlayerBaseCommand {
    private const val COMMAND_NAME = "play"
    private const val FILE_LITERAL_NAME = "file"
    private const val QUERY_LITERAL_NAME = "query"
    private const val FILE_ARG_NAME = "attachment"
    private const val QUERY_ARG_NAME = "query"

    private object L {
        private const val PREFIX = "command.$COMMAND_NAME"
        const val CMD_DESC = "$PREFIX.description"
        const val SUBCMD_QUERY_DESC = "$PREFIX.$QUERY_LITERAL_NAME.description"
        const val CMD_ARG_QUERY_DESC = "$PREFIX.$QUERY_LITERAL_NAME.$QUERY_ARG_NAME.description"
        const val SUBCMD_FILE_DESC = "$PREFIX.$FILE_LITERAL_NAME.description"
        const val CMD_ARG_FILE_DESC = "$PREFIX.$FILE_LITERAL_NAME.$FILE_ARG_NAME.description"
        const val SUCCESS_QUEUED_PLAYLIST = "$PREFIX.playlist.success"
        const val SUCCESS_QUEUED_SINGLE = "$PREFIX.single.success"
    }

    override fun registerLocalizations(generator: ILanguageGenerator) {
        generator
            .addEntry(L.CMD_DESC, "<nickname>要來當 DJ！")
            .addEntry(L.SUBCMD_QUERY_DESC, "<nickname>要找歌來放！")
            .addEntry(L.CMD_ARG_QUERY_DESC, "<nickname>要放什麼歌呢！？")
            .addEntry(L.SUBCMD_FILE_DESC, "<nickname>要把自己的歌放上來！")
            .addEntry(L.CMD_ARG_FILE_DESC, "要放哪一首呢... (緩慢挑選")
            .addEntry(L.SUCCESS_QUEUED_PLAYLIST, "已加入 <count> 首歌曲！ [<user>]")
            .addEntry(L.SUCCESS_QUEUED_SINGLE, "已加入 <songTitle> [<user>]")
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal(COMMAND_NAME)
            .then(literal(QUERY_LITERAL_NAME)
                .then(argument(QUERY_ARG_NAME, StringArgumentType.greedyString())
                    .executes(PlayCommand::executeQuery)
                )
            )
            .then(literal(FILE_LITERAL_NAME)
                .then(argument(FILE_ARG_NAME, AttachmentArgument())
                    .executes {
                        Logger.error("Not implemented for /play file ...")
                        return@executes 1
                    }
                )
            )
        )
    }

    override fun registerDiscord(): List<CommandData> {
        return listOf(
            Commands.slash(COMMAND_NAME, Mochi.i18n.of(L.CMD_DESC))
                .setDescriptionLocalizations(makeLocaleMap(L.CMD_DESC))
                .addSubcommands(
                    SubcommandData(QUERY_LITERAL_NAME, Mochi.i18n.of(L.SUBCMD_QUERY_DESC))
                        .setDescriptionLocalizations(makeLocaleMap(L.SUBCMD_QUERY_DESC))
                        .addOptions(OptionData(OptionType.STRING, QUERY_ARG_NAME, Mochi.i18n.of(L.CMD_ARG_QUERY_DESC))
                            .setDescriptionLocalizations(makeLocaleMap(L.CMD_ARG_QUERY_DESC))
                            .setRequired(true)
                        ),
                    SubcommandData(FILE_LITERAL_NAME, Mochi.i18n.of(L.SUBCMD_FILE_DESC))
                        .setDescriptionLocalizations(makeLocaleMap(L.SUBCMD_FILE_DESC))
                        .addOptions(OptionData(OptionType.ATTACHMENT, FILE_ARG_NAME, Mochi.i18n.of(L.CMD_ARG_FILE_DESC))
                            .setDescriptionLocalizations(makeLocaleMap(L.CMD_ARG_FILE_DESC))
                            .setRequired(true)
                        )
                )
        )
    }

    private fun executeQuery(ctx: CommandContext<CommandSource>): Int {
        UtilsKt.asyncDiscard i@ {
            val source = ctx.source
            if (!canExecute(source, requiresBotInChannel = false)) return@i

            val member = source.member ?: return@i
            val manager = source.guildManager!!
            val i18n = source.i18n
            val query = ctx.getArgument<String>("query")
            val channel = source.voiceChannel!!
            val textChannel = source.channel
            val playerManager = manager.playerManager

            if (textChannel != null) playerManager.announceChannel = textChannel
            source.defer()

            if (!playerManager.hasJoinedChannel) {
                playerManager.joinChannel(channel)
            }

            try {
                val list = playerManager.addLavaPlayerSourceToQueueAsync(query, source)
                var desc: String

                if (list.isEmpty()) {
                    source.respondError("找不到歌曲！", ICommandSender.RespondOption(
                        preferEmbed = true
                    ))
                }

                if (list.size > 1) {
                    desc = i18n.of(L.SUCCESS_QUEUED_PLAYLIST, mutableMapOf<String, Any>(
                        Pair("count", list.size),
                        Pair("user", UserPlaceholder(member.user))
                    ))

                    if (list.all { it.length != Units.DURATION_MS_UNKNOWN }) {
                        val totalDuration = list
                            .filter { it.length != Units.DURATION_MS_UNKNOWN }
                            .map { it.length }
                            .reduce { a, b -> a + b }
                        val d = Duration.of(totalDuration, ChronoUnit.MILLIS)
                        val time = "`[${d.format()}]`"
                        desc = desc.plus(" $time").trim()
                    }
                } else {
                    val queue = list.first()
                    desc = i18n.of(L.SUCCESS_QUEUED_SINGLE, mutableMapOf<String, Any>(
                        Pair("songTitle", "[${queue.title}](${queue.link})"),
                        Pair("user", UserPlaceholder(member.user))
                    ))

                    val duration = queue.length
                    if (duration != Units.DURATION_MS_UNKNOWN) {
                        val d = Duration.of(duration, ChronoUnit.MILLIS)
                        val time = "`[${d.format()}]`"
                        desc = desc.plus(" $time").trim()
                    }
                }

                source.respond(desc, ICommandSender.RespondOption(preferEmbed = true, preferNoAuthor = true))
            } catch (ex: Throwable) {
                Logger.error("Failed to queue a query!")
                Logger.error(ex)
                source.respondError("Failed!",
                    ICommandSender.RespondOption(preferEmbed = true, preferEphemeral = true))
            }
        }

        return 1
    }
}