package com.kakaouo.bot.mochi.command

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.command.sender.ICommandSender
import com.kakaouo.bot.mochi.i18n.ChannelPlaceholder
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.bot.mochi.i18n.ILocalizable
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel

private typealias ExecuteDelegate = (AudioChannel) -> Unit

interface IPlayerBaseCommand {
    private object L {
        private const val CATEGORY = "player"
        private const val PREFIX = "command.$CATEGORY"

        const val ERR_USER_NOT_IN_CHANNEL = "$PREFIX.error.notInChannel.user"
        const val ERR_BOT_NOT_IN_CHANNEL = "$PREFIX.error.notInChannel.bot"
        const val ERR_NOT_IN_SAME_CHANNEL = "$PREFIX.error.notInChannel.mutual"
        const val ERR_NO_CONNECT_PERMISSION = "$PREFIX.error.noPermission.connect"
        const val ERR_NO_SPEAK_PERMISSION = "$PREFIX.error.noPermission.speak"
    }

    companion object : ILocalizable {
        override fun registerLocalizations(generator: ILanguageGenerator) {
            generator
                .addEntry(L.ERR_USER_NOT_IN_CHANNEL, "<nickname>發現你沒在語音頻道內，覺得不想做事（躺")
                .addEntry(L.ERR_BOT_NOT_IN_CHANNEL, "<nickname>不在語音頻道內，覺得不想做事（躺")
                .addEntry(L.ERR_NOT_IN_SAME_CHANNEL, "你需要和<nickname>在同個頻道才可以用這個指令！")
                .addEntry(L.ERR_NO_CONNECT_PERMISSION, "<nickname>進不去 <channel> 啦！")
                .addEntry(L.ERR_NO_SPEAK_PERMISSION, "<nickname>進去 <channel> 就不能說話啦！")
        }
    }

    suspend fun ensureCanExecute(
        source: CommandSource, requiresBotInChannel: Boolean, requiresSameChannel: Boolean,
        suppressRespond: Boolean, callback: ExecuteDelegate? = null) {
        val guildManager = source.guildManager
        if (guildManager == null) {
            source.respondError("You have to be in a guild!", ICommandSender.RespondOption(
                preferEmbed = true
            ))
            return
        }

        val channel = source.voiceChannel
        val guild = guildManager.guild

        if (channel == null) {
            if (!suppressRespond) {
                source.respondError(source.i18n.of(L.ERR_USER_NOT_IN_CHANNEL), ICommandSender.RespondOption(
                    preferEmbed = true
                ))
            }
            return
        }

        val voiceChannel = guildManager.playerManager.channel
        if (requiresBotInChannel && voiceChannel == null) {
            if (!suppressRespond) {
                source.respondError(source.i18n.of(L.ERR_BOT_NOT_IN_CHANNEL), ICommandSender.RespondOption(
                    preferEmbed = true
                ))
            }
            return
        }

        if (requiresSameChannel && voiceChannel != null && voiceChannel.id != channel.id) {
            if (!suppressRespond) {
                source.respondError(source.i18n.of(L.ERR_NOT_IN_SAME_CHANNEL, ChannelPlaceholder(voiceChannel)),
                    ICommandSender.RespondOption(preferEmbed = true))
            }
            return
        }

        val user = guild.selfMember
        val permissions = user.getPermissions(channel)

        if (!permissions.contains(Permission.VOICE_CONNECT)) {
            if (!suppressRespond) {
                source.respondError(source.i18n.of(L.ERR_NO_CONNECT_PERMISSION, ChannelPlaceholder(channel)),
                    ICommandSender.RespondOption(preferEmbed = true))
            }
            return
        }

        if (!permissions.contains(Permission.VOICE_SPEAK)) {
            if (!suppressRespond) {
                source.respondError(source.i18n.of(L.ERR_NO_SPEAK_PERMISSION, ChannelPlaceholder(channel)),
                    ICommandSender.RespondOption(preferEmbed = true))
            }
            return
        }

        callback?.invoke(channel)
    }

    suspend fun ensureCanExecute(source: CommandSource, requiresBotInChannel: Boolean, callback: ExecuteDelegate? = null) {
        ensureCanExecute(source, requiresBotInChannel, true,  false, callback)
    }

    suspend fun ensureSourceInVoiceChannel(source: CommandSource, callback: ExecuteDelegate? = null) {
        ensureCanExecute(source, false, callback)
    }

    suspend fun ensureInMutualVoiceChannel(source: CommandSource, callback: ExecuteDelegate? = null) {
        ensureCanExecute(source, true, callback)
    }

    suspend fun canExecute(source: CommandSource,
                           requiresBotInChannel: Boolean = true,
                           requiresSameChannel: Boolean = true,
                           suppressRespond: Boolean = false): Boolean {
        var result = false
        ensureCanExecute(source, requiresBotInChannel, requiresSameChannel, suppressRespond) {
            result = true
        }

        return result
    }
}
