package com.kakaouo.bot.mochi.managers.chat

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatRole
import com.kakaouo.bot.mochi.utils.Utils
import net.dv8tion.jda.api.entities.User
import java.io.File

@OptIn(BetaOpenAI::class)
class UserChatSession(user: User) : AbstractChatSession<User>(user) {
    override fun getChatHostId(): String {
        return host.id
    }

    override fun addDefaultAssistantMessage() {
        val name = host.name
        addMessage(ChatRole.Assistant, defaultAssistantMessageContent(name))
    }

    override fun buildSystemPrompt(sb: StringBuilder) {
        super.buildSystemPrompt(sb)
        sb.appendLine("這名使用者名稱是「${host.name}」。")

        val fileDir = File(Utils.getRootDirectory(), "$CHAT_DIR/$USERINFO_DIR/")
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }

        val userInfoFile = File(fileDir, "${host.id}.txt")
        if (userInfoFile.exists()) {
            sb.appendLine(userInfoFile.readText())
        }
    }
}

