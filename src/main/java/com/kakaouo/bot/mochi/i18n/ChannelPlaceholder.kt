package com.kakaouo.bot.mochi.i18n

import net.dv8tion.jda.api.entities.channel.Channel

class ChannelPlaceholder(val channel: Channel, name: String = "channel") : MentionablePlaceholder(channel, name)

