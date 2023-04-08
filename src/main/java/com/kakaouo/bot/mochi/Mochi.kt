package com.kakaouo.bot.mochi

import com.kakaouo.bot.mochi.command.Command
import com.kakaouo.bot.mochi.command.exceptions.MochiBuiltinExceptionProvider
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.bot.mochi.config.MochiConfig
import com.kakaouo.bot.mochi.i18n.I18n
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.bot.mochi.i18n.ILocalizable
import com.kakaouo.bot.mochi.i18n.LanguageGenerator
import com.kakaouo.bot.mochi.managers.GuildManager
import com.kakaouo.bot.mochi.managers.chat.ChatBotManager
import com.kakaouo.bot.mochi.texts.LiteralText
import com.kakaouo.bot.mochi.texts.TextColor
import com.kakaouo.bot.mochi.texts.Texts
import com.kakaouo.bot.mochi.texts.TranslateText
import com.kakaouo.bot.mochi.utils.Logger
import com.kakaouo.bot.mochi.utils.Utils
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
import net.dv8tion.jda.api.requests.GatewayIntent
import java.awt.Color
import java.lang.RuntimeException

class Mochi : EventListener, ILocalizable {
    companion object {
        private var _inst: Mochi? = null
        val instance get() = _inst!!

        lateinit var i18n: I18n private set
    }

    object L {
        const val NICKNAME = "bot.nickname"
    }

    lateinit var dispatcher: CommandDispatcher<CommandSource> private set
    val config get() = MochiConfig.instance
    lateinit var client: JDA
    val user get() = client.selfUser
    val guildManagers = mutableListOf<GuildManager>()
    val chatBotManager: ChatBotManager

    private val localeCacheLock = Semaphore(1)
    val localeCache = mutableMapOf<DiscordLocale, I18n>()

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
        val result = I18n(locale)
        i18n = I18n("$locale-${config.data.commandName}", result)

        Logger.info(TranslateText.of("Loaded global locale: %s")
            .addWith(LiteralText.of(locale).setColor(TextColor.GOLD)))
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

        val name = event.javaClass.name
        val message = TranslateText.of("Received event: %s")
            .addWith(LiteralText.of(name).setColor(TextColor.AQUA))

        if (event is GenericGuildEvent) {
            message.addExtra(TranslateText.of(" at guild %s")
                .addWith(Texts.ofGuild(event.guild)))
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

        Utils.asyncDiscard {
            try {
                Command.executeByCommand(event.message)
            } catch (ex: Throwable) {
                Logger.error(ex)
            }
        }
    }

    private fun onMessageContextInteraction(event: MessageContextInteractionEvent) {
        Utils.asyncDiscard {
            try {
                Command.executeByCommand(event.interaction)
            } catch (ex: Throwable) {
                Logger.error(ex)
            }
        }
    }

    private fun onUserContextInteraction(event: UserContextInteractionEvent) {
        Utils.asyncDiscard {
            try {
                Command.executeByCommand(event.interaction)
            } catch (ex: Throwable) {
                Logger.error(ex)
            }
        }
    }

    private fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {

    }

    private fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        Utils.asyncDiscard {
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
        Logger.info(TranslateText.of("Discord bot logged in as %s %s")
            .addWith(Texts.ofUser(user))
            .addWith(LiteralText.of("(shard #${client.shardInfo.shardId})")
                .setColor(TextColor.DARK_GRAY))
        )

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

        Utils.asyncDiscard {
            Command.registerCommandsForDiscordGlobal()
        }

        guildManagers.forEach {
            Utils.asyncDiscard {
                Command.registerCommandsForDiscordGuild(it.guild)
            }
        }
    }

    fun getI18nFor(locale: DiscordLocale): I18n {
        Utils.promisify {
            localeCacheLock.acquire()
        }.get()

        try {
            return localeCache.computeIfAbsent(locale) {
                val result = I18n(locale.name, i18n)
                I18n(locale.name + "-" + config.data.commandName, result)
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

    fun getBaseEmbed(i18n: I18n, guild: Guild?): EmbedBuilder {
        return EmbedBuilder()
            .setColor(getMainColor(guild))
            .setAuthor(i18n.of(L.NICKNAME), null, user.avatarUrl)
    }

    fun getBaseEmbed(sender: IDiscordCommandSender): EmbedBuilder {
        return getBaseEmbed(sender.i18n, sender.guild)
    }
}
