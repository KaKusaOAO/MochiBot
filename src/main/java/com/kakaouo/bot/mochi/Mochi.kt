package com.kakaouo.bot.mochi

import com.kakaouo.bot.mochi.command.Command
import com.kakaouo.bot.mochi.command.ExecuteCommand
import com.kakaouo.bot.mochi.command.exceptions.MochiBuiltinExceptionProvider
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ConsoleCommandSender
import com.kakaouo.bot.mochi.command.sender.DiscordInteractionSender
import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.bot.mochi.config.MochiConfig
import com.kakaouo.bot.mochi.i18n.MochiI18n
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.bot.mochi.i18n.ILocalizable
import com.kakaouo.bot.mochi.i18n.LanguageGenerator
import com.kakaouo.bot.mochi.managers.GuildManager
import com.kakaouo.bot.mochi.managers.chat.ChatBotManager
import com.kakaouo.bot.mochi.texts.TextBuilder
import com.kakaouo.bot.mochi.texts.Texts
import com.kakaouo.bot.mochi.utils.MochiEmbedBuilder
import com.kakaouo.mochi.texts.LiteralText
import com.kakaouo.mochi.texts.Text
import com.kakaouo.mochi.texts.TextColor
import com.kakaouo.mochi.texts.TranslateText
import com.kakaouo.mochi.utils.Logger
import com.kakaouo.mochi.utils.UtilsKt
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import kotlinx.coroutines.sync.Semaphore
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.http.HttpRequestEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.requests.GatewayIntent
import java.awt.Color
import java.lang.RuntimeException

class Mochi : EventListener, ILocalizable {
    companion object {
        private var _inst: Mochi? = null
        val instance get() = _inst!!

        lateinit var i18n: MochiI18n private set
    }

    object L {
        const val NICKNAME = "bot.nickname"
    }

    lateinit var dispatcher: CommandDispatcher<CommandSource> private set
    val config get() = MochiConfig.instance
    var client: JDA
    val user get() = client.selfUser
    val guildManagers = mutableListOf<GuildManager>()
    val chatBotManager: ChatBotManager

    private val localeCacheLock = Semaphore(1)
    val localeCache = mutableMapOf<DiscordLocale, MochiI18n>()

    init {
        if (_inst != null) {
            throw RuntimeException("Is the bot repeatedly created?")
        }
        _inst = this

        Logger.info("Generating language files...")
        LanguageGenerator.register(this)
        LanguageGenerator.generate()
        loadI18n()

        val token = config.data.token
        if (token.isEmpty()) {
            throw RuntimeException("Token is empty!")
        }

        client = JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(this)
            .build()
        CommandSyntaxException.BUILT_IN_EXCEPTIONS = MochiBuiltinExceptionProvider
        registerCommands()

        client.presence.setPresence(
            config.data.status,
            Activity.of(config.data.activity.type, config.data.activity.name))

        chatBotManager = ChatBotManager()
    }

    private fun registerCommands() {
        dispatcher = CommandDispatcher<CommandSource>()
        Command.registerToDispatcher()
    }

    private fun loadI18n() {
        val locale = config.data.locale
        val result = MochiI18n(locale)
        i18n = MochiI18n("$locale-${config.data.commandName}", result)

        Logger.info(Texts.translate("Loaded global locale: %s") {
            with(Texts.literal(locale) {
                color(TextColor.GOLD)
            })
        })
    }

    override fun onEvent(event: GenericEvent) {
        if (event is GuildReadyEvent) return onGuildReady(event)
        if (event is ReadyEvent) return onReady(event)
        if (event is SlashCommandInteractionEvent) return onSlashCommandInteraction(event)
        if (event is CommandAutoCompleteInteractionEvent) return onCommandAutoCompleteInteraction(event)
        if (event is UserContextInteractionEvent) return onUserContextInteraction(event)
        if (event is MessageContextInteractionEvent) return onMessageContextInteraction(event)
        if (event is MessageReceivedEvent) return onMessageReceived(event)
        if (event is HttpRequestEvent) return // Silence

        val message = Texts.translate("Received event: %s") {
            with(Text.representClass(event.javaClass, TextColor.AQUA))
            if (event is GenericGuildEvent) {
                extra {
                    literal(" in ")
                }
                extra(Texts.ofGuild(event.guild))
            }
        }

        Logger.info(message)
    }

    private fun onMessageReceived(event: MessageReceivedEvent) {
        val message = event.message
        val cmd = message.contentRaw
        if (cmd.isEmpty()) return

        val guild = if (message.isFromGuild) message.guild else null
        val manager = getGuildManager(guild)
        val i18n = manager?.i18n ?: i18n

        val prefix = config.data.commandPrefix
        if (!cmd.startsWith(prefix)) return

        UtilsKt.asyncDiscard {
            try {
                Command.executeByCommand(event.message)
            } catch (ex: Throwable) {
                Logger.error(ex)
            }
        }
    }

    private fun onMessageContextInteraction(event: MessageContextInteractionEvent) {
        UtilsKt.asyncDiscard {
            try {
                Command.executeByCommand(event.interaction)
            } catch (ex: Throwable) {
                Logger.error(ex)
            }
        }
    }

    private fun onUserContextInteraction(event: UserContextInteractionEvent) {
        UtilsKt.asyncDiscard {
            try {
                Command.executeByCommand(event.interaction)
            } catch (ex: Throwable) {
                Logger.error(ex)
            }
        }
    }

    private fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val options = event.options
        if (options.isEmpty()) return

        // You're right!
        // We still don't support other commands!
        if (event.name != ExecuteCommand.COMMAND_NAME) return

        val arg = event.options.first().asString
        val line = "${ExecuteCommand.COMMAND_NAME} $arg"

        val interaction = event.interaction
        val sender = DiscordInteractionSender(interaction)
        val source = CommandSource(sender)
        var results = dispatcher.parse(line, source)
        var suggestions = dispatcher.getCompletionSuggestions(results).get()

        val result = suggestions.list.map {
            val length = "${ExecuteCommand.COMMAND_NAME} ".length
            val n = it.apply(line).substring(length)
            Choice(n, n)
        }.toMutableList()

        if (result.isEmpty()) {
            val exceptions = results.exceptions
            if (exceptions.any()) {
                val ex = exceptions.values.first()
                result.add(Choice(ex.message ?: "Error", "_"))
                interaction.replyChoices(result).submit().get()
                return
            }
        }

        results = dispatcher.parse("$line ", source)
        suggestions = dispatcher.getCompletionSuggestions(results).get()
        result.addAll(suggestions.list.map {
            val length = "${ExecuteCommand.COMMAND_NAME} ".length
            val n = it.apply("$line ").substring(length)
            Choice(n, n)
        })

        interaction.replyChoices(result).submit().get()
    }

    private fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        UtilsKt.asyncDiscard {
            try {
                Command.executeByCommand(event.interaction)
            } catch (ex: Throwable) {
                Logger.error(ex)
            }
        }
    }

    private fun onGuildReady(event: GuildReadyEvent) {
        Logger.info(TranslateText.of("Guild discovered: %s")
            .addWith(Texts.ofGuild(event.guild))
        )
    }

    private fun onReady(event: ReadyEvent) {
        Logger.info(Texts.translate("Discord bot logged in as %s %s") {
            with(Texts.ofUser(user))
            with(Texts.literal("(shard #${client.shardInfo.shardId})") {
                color(TextColor.DARK_GRAY)
            })
        })

        val guilds = client.guilds
        guildManagers.removeIf { manager ->
            !guilds.any { it.id == manager.guild.id }
        }
        guildManagers.addAll(guilds
            .filter { guild ->
                guildManagers.all { it.guild.id != guild.id }
            }
            .map { GuildManager(it) }
        )

        UtilsKt.asyncDiscard {
            Command.registerCommandsForDiscordGlobal()
        }

        guildManagers.forEach {
            UtilsKt.asyncDiscard {
                Command.registerCommandsForDiscordGuild(it.guild)
            }
        }
    }

    fun getI18nFor(locale: DiscordLocale): MochiI18n {
        UtilsKt.promisify {
            localeCacheLock.acquire()
        }.get()

        try {
            return localeCache.computeIfAbsent(locale) {
                MochiI18n(locale.name, i18n).wrapBotVariant()
            }
        } finally {
            localeCacheLock.release()
        }
    }

    override fun registerLocalizations(generator: ILanguageGenerator) {
        generator.addEntry(L.NICKNAME, config.nickname)
    }

    fun createStyledEmbed(sender: IDiscordCommandSender, block: EmbedBuilder.() -> Unit): EmbedBuilder {
        val embed = getBaseEmbed(sender)
        embed.block()
        return embed
    }

    fun createStyledEmbed(guild: Guild? = null, block: EmbedBuilder.() -> Unit): EmbedBuilder {
        val i18n = getGuildManager(guild)?.i18n ?: Mochi.i18n
        val embed = getBaseEmbed(i18n, guild)
        embed.block()
        return embed
    }

    fun createStyledEmbed(description: String, sender: IDiscordCommandSender): EmbedBuilder {
        return createStyledEmbed(sender) {
            setDescription(description)
        }
    }

    fun createStyledEmbed(description: String, guild: Guild? = null): EmbedBuilder {
        return createStyledEmbed(guild) {
            setDescription(description)
        }
    }

    fun getGuildManager(guild: Guild?): GuildManager? {
        if (guild == null) return null
        return guildManagers.find { it.guild.id == guild.id }
    }

    fun getMainColor(guild: Guild? = null): Color {
        val theme = Color(0xd8993b)
        return getGuildManager(guild)?.mainColor ?: theme
    }

    fun getBaseEmbed(i18n: MochiI18n, guild: Guild?): EmbedBuilder {
        return MochiEmbedBuilder()
            .setColor(getMainColor(guild))
            .setAuthor(i18n.of(L.NICKNAME), null, user.avatarUrl)
    }

    fun getBaseEmbed(sender: IDiscordCommandSender): EmbedBuilder {
        return getBaseEmbed(sender.i18n, sender.guild)
    }

    fun handleCommandLine(line: String?) {
        if (line == null) return

        val source = CommandSource(ConsoleCommandSender())
        try {
            dispatcher.execute(line, source)
        } catch (ex: CommandSyntaxException) {
            Logger.error("Syntax error!\n" + ex.message)
        } catch (ex: Throwable) {
            Logger.error("Unexpected error occurred!")
            Logger.error(ex)
        }
    }
}
