package com.kakaouo.bot.mochi.managers.player

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.Constants
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.i18n.ILanguageGenerator
import com.kakaouo.bot.mochi.i18n.ILocalizable
import com.kakaouo.bot.mochi.managers.GuildManager
import com.kakaouo.bot.mochi.managers.player.queue.AbstractQueueItem
import com.kakaouo.bot.mochi.managers.player.queue.LavaPlayerQueueItem
import com.kakaouo.bot.mochi.utils.MochiUtils.runAcquired
import com.kakaouo.mochi.texts.LiteralText
import com.kakaouo.mochi.texts.TextColor
import com.kakaouo.mochi.texts.TranslateText
import com.kakaouo.mochi.utils.Logger
import com.kakaouo.mochi.utils.UtilsKt
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.nio.ByteBuffer
import java.util.concurrent.Semaphore

class PlayerManager(val guildManager: GuildManager) : EventListener, AudioEventListener {
    private val audioManager = guildManager.guild.audioManager
    private val manager = DefaultAudioPlayerManager()
    private val player = manager.createPlayer()
    private val audioProvider = AudioProvider(player)
    private val queue = mutableListOf<AbstractQueueItem>()
    private val queueLock = Semaphore(1)
    private var playerIndex = -1
    var announceChannel: MessageChannel? = null
    var lastPlayingMessage: Message? = null

    val channel get() = audioManager.connectedChannel

    init {
        player.addListener(this)

        val bot = Mochi.instance
        val chn = bot.client.getChannelById(MessageChannel::class.java, guildManager.config.data.player.lastTextChannelId)
        announceChannel = chn

        chn?.retrieveMessageById(guildManager.config.data.player.lastPlayingMessageId)?.submit()?.thenAccept {
            lastPlayingMessage = it
        }

        bot.client.addEventListener(this)
        AudioSourceManagers.registerRemoteSources(manager)
        guildManager.guild.audioManager.sendingHandler = audioProvider
    }

    private object L {
        private const val PREFIX = "player"
        const val CURRENT_PLAYING_TITLE = "$PREFIX.title.currentPlaying"
    }

    companion object : ILocalizable {
        override fun registerLocalizations(generator: ILanguageGenerator) {
            generator
                .addEntry(L.CURRENT_PLAYING_TITLE, "現正播放")
        }
    }

    suspend fun addLavaPlayerSourceToQueueAsync(query: String, source: CommandSource): List<LavaPlayerQueueItem> {
        val list = LavaPlayerQueueItem.createQueuesFromQuery(manager, query, source)
        return queueList(list)
    }

    private fun <T: AbstractQueueItem> queueList(list: List<T>): List<T> {
        list.forEach {
            internalQueue(it)
        }
        return list
    }

    private suspend fun <T: AbstractQueueItem> queueFromSupplier(factory: suspend () -> T) {
        val queue = factory()
        internalQueue(queue)
    }

    private fun <T: AbstractQueueItem> internalQueue(q: T) {
        queueLock.runAcquired {
            queue.add(q)
        }

        if (playerIndex == -1) {
            playLast(true)
        } else {
            playNext(false)
        }
    }

    private fun playLast(interrupt: Boolean) {
        playAt(queue.size - 1, interrupt)
    }

    fun getQueue(index: Int): AbstractQueueItem? {
        if (index < 0) return null
        return queueLock.runAcquired {
            if (queue.size <= index)
                null
            else
                queue[index]
        }
    }

    fun getNextIndex(): Int {
        return playerIndex + 1
    }

    fun getCurrentPlaying(): AbstractQueueItem? {
        return getQueue(playerIndex)
    }

    fun peekNext(): AbstractQueueItem? {
        return getQueue(getNextIndex())
    }

    private fun play(n: AbstractQueueItem, interrupt: Boolean): Boolean {
        val started = player.startTrack(n.audioTrack, !interrupt)
        if (!started) return false

        val i18n = n.source.i18n
        val isSilent = guildManager.config.data.player.isSilentMode

        if (!isSilent) {
            sendToAnnounceChannel(createStyledEmbed(false) {
                val title = n.getTitleForDisplay()
                val time = n.getTimeForDisplay()

                val user = n.source.user
                val tag = if (user == null) n.source.sender.getMentionString() else "<@${user.id}>"
                setDescription("$title [$tag] $time".trim())
                setAuthor(i18n.of(L.CURRENT_PLAYING_TITLE))
            }.build())
        }

        return true
    }

    fun sendToAnnounceChannel(embed: MessageEmbed, saveAsLastMessage: Boolean = true) {
        val chn = announceChannel ?: return
        chn.sendMessage(MessageCreateBuilder()
            .setEmbeds(embed)
            .build()
        ).submit().thenAccept {
            if (!saveAsLastMessage) return@thenAccept
            lastPlayingMessage = it
            guildManager.config.data.player.lastPlayingMessageId = it.id
        }.get()
    }

    fun createStyledEmbed(removeAuthor: Boolean = true, block: EmbedBuilder.() -> Unit): EmbedBuilder {
        return guildManager.createStyledEmbed {
            if (removeAuthor) {
                setAuthor(null)
            }
            block()
        }
    }

    fun removeLastPlayingMessage() {
        lastPlayingMessage?.delete()?.queue()
        guildManager.config.data.player.lastPlayingMessageId = "0"
    }

    val hasJoinedChannel: Boolean get() {
        return audioManager.isConnected ||
                audioManager.connectionStatus.name.startsWith("connecting", true)
    }

    fun joinChannel(audioChannel: AudioChannel) {
        audioManager.openAudioConnection(audioChannel)
    }

    fun playAt(index: Int, interrupt: Boolean) {
        val n = getQueue(index)
        if (n != null) {
            if (play(n, interrupt)) {
                playerIndex = index
            }
        }
    }

    fun playNext(interrupt: Boolean) {
        val index = getNextIndex()
        val n = getQueue(index)

        if (n != null) {
            if (play(n, interrupt)) {
                playerIndex = index
            }
        } else {
            playerIndex = -1
            stop()
        }
    }

    fun pause(): Boolean {
        if (player.isPaused) return false
        player.isPaused = true
        return true
    }

    fun resume(): Boolean {
        if (!player.isPaused) return false
        player.isPaused = false
        return true
    }

    fun skip() {
        playNext(true)
    }

    fun playNextNoInterrupt() {
        playNext(false)
    }

    fun jumpTo(index: Int) {
        playerIndex = index
        playAt(index, true)
    }

    fun close() {
        val client = Mochi.instance.client
        client.removeEventListener(this)
    }

    override fun onEvent(event: GenericEvent) {
        if (event is GuildVoiceUpdateEvent) onGuildVoiceUpdate(event)
    }

    private fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val member = guildManager.guild.selfMember
        if (event.member.id == member.id) {
            if (event.channelJoined == null) {
                stop()
            }
        }
    }

    fun stop() {
        player.stopTrack()
    }

    override fun onEvent(event: AudioEvent?) {
        if (event is TrackExceptionEvent) onTrackException(event)
        if (event is TrackEndEvent) onTrackEnd(event)
    }

    private fun onTrackException(event: TrackExceptionEvent) {
        val track = event.track
        Logger.error(
            TranslateText.of("Failed to play track: %s")
                .addWith(LiteralText.of(track.toString()).setColor(TextColor.AQUA))
        )
        Logger.error(event.exception)

        sendToAnnounceChannel(createStyledEmbed {
            setColor(Constants.ERROR_COLOR)
            setDescription("播放該歌曲時發生錯誤！")
        }.build(), false)
    }

    private fun onTrackEnd(event: TrackEndEvent) {
        UtilsKt.asyncDiscard {
            val queue = getCurrentPlaying()
            queue?.runPostPlayback()

            val isSilent = guildManager.config.data.player.isSilentMode
            if (!isSilent) {
                removeLastPlayingMessage()
            }

            if (event.endReason.mayStartNext) {
                playNext(false)
            }
        }
    }
}
