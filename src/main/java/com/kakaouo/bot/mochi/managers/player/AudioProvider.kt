package com.kakaouo.bot.mochi.managers.player

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class AudioProvider(val player: AudioPlayer) : AudioSendHandler {
    private val frame = MutableAudioFrame()
    private val buffer = ByteBuffer.allocate(1024)

    init {
        frame.setBuffer(buffer)
    }

    override fun canProvide(): Boolean {
        return player.provide(frame)
    }

    override fun provide20MsAudio(): ByteBuffer? {
        buffer.flip()
        return buffer
    }

    override fun isOpus(): Boolean {
        return true
    }
}