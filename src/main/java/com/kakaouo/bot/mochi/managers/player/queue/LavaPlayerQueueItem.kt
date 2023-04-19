package com.kakaouo.bot.mochi.managers.player.queue

import com.kakaouo.bot.mochi.command.sender.CommandSource
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack


class LavaPlayerQueueItem private constructor(track: AudioTrack, source: CommandSource, private val customTitle: String? = null) :
    AbstractQueueItem(track, source), ILengthAvailableQueue, ILinkAvailableQueue {

    override val title: String get() = customTitle ?: audioTrack.info.title
    override val link: String get() = audioTrack.info.uri
    override val length: Long get() = audioTrack.info.length

    companion object {
        suspend fun createQueuesFromQuery(manager: AudioPlayerManager, query: String, source: CommandSource, title: String? = null): List<LavaPlayerQueueItem> {
            val list = getTracksFromLavaPlayer(manager, query)
            return list.map { LavaPlayerQueueItem(it, source, title) }
        }
    }
}