package com.kakaouo.bot.mochi.managers.player.queue

import com.kakaouo.bot.mochi.MochiException
import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.kakaouo.bot.mochi.utils.MochiUtils.format
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.Units
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.future.await
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class AbstractQueueItem(val audioTrack: AudioTrack, val source: CommandSource) {
    // abstract val audioTrack: AudioTrack
    open val title = "Unknown"

    open fun getTitleForDisplay(): String {
        if (this !is ILinkAvailableQueue) return title
        return "[${title}](${link})"
    }

    open fun getTimeForDisplay(): String {
        if (this !is ILengthAvailableQueue) return ""
        if (length == Units.DURATION_MS_UNKNOWN) return ""

        val d = Duration.of(length, ChronoUnit.MILLIS)
        return "`[${d.format()}]`"
    }

    open suspend fun runPostPlayback() {

    }

    companion object {
        @JvmStatic
        protected suspend fun getTracksFromLavaPlayer(manager: AudioPlayerManager, query: String): List<AudioTrack> {
            val future = CompletableFuture<List<AudioTrack>>()
            manager.loadItemOrdered(this, query, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    future.complete(Collections.singletonList(track))
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    future.complete(playlist.tracks)
                }

                override fun noMatches() {
                    future.complete(listOf())
                }

                override fun loadFailed(exception: FriendlyException) {
                    future.completeExceptionally(MochiException(exception.message))
                }
            })
            return future.await()
        }
    }
}