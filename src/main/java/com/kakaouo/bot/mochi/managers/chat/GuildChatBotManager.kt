package com.kakaouo.bot.mochi.managers.chat

import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.sender.DiscordMessageSender
import com.kakaouo.bot.mochi.config.MochiConfig
import com.kakaouo.bot.mochi.managers.GuildManager
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import java.io.Closeable

class GuildChatBotManager(val guildManager: GuildManager) : Closeable, EventListener {
    init {
        val client = Mochi.instance.client
        client.addEventListener(this)
    }

    override fun close() {
        val client = Mochi.instance.client
        client.removeEventListener(this)
    }

    override fun onEvent(event: GenericEvent) {
        if (event is MessageReceivedEvent) handleMessage(event)
    }

    private fun handleMessage(event: MessageReceivedEvent) {
        val message = event.message
        val config = guildManager.config.data.chatBot
        if (!config.enabled) return
        if (message.channel.id != config.chatChannelId) return

        val chatBot = Mochi.instance.chatBotManager
        if (!chatBot.commonValidateMessage(message)) return

        val sender = DiscordMessageSender(message)
        chatBot.handleCheckedMessage(sender, message, sender.i18n)
    }
}