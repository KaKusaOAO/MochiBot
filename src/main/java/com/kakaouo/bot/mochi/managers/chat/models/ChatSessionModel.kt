package com.kakaouo.bot.mochi.managers.chat.models

import java.time.Instant
import java.util.*

class ChatSessionModel {
    var lastActiveTime = Date.from(Instant.now())
    var prompts = listOf<ChatPromptModel>()
}