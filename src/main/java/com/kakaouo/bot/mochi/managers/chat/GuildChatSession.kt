package com.kakaouo.bot.mochi.managers.chat

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatRole
import com.kakaouo.bot.mochi.utils.Utils
import net.dv8tion.jda.api.entities.Guild
import java.io.File

@OptIn(BetaOpenAI::class)
class GuildChatSession(guild: Guild) : AbstractChatSession<Guild>(guild) {
    override fun getChatHostId(): String {
        return host.id
    }

    override fun defaultAssistantMessageContent(name: String): String {
        return "喵嗚～大家好啊！有什麼我可以幫助你們的嗎？(*´∀`)~♥"
    }

    override fun buildSystemPrompt(sb: StringBuilder) {
        super.buildSystemPrompt(sb)
        sb.appendLine("這是一個聊天群組，是多人聊天的地方。群組名稱為「${host.name}」。")

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