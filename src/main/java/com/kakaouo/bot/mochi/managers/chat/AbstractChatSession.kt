package com.kakaouo.bot.mochi.managers.chat

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.fasterxml.jackson.databind.ObjectMapper
import com.kakaouo.bot.mochi.config.MochiConfig
import com.kakaouo.bot.mochi.managers.chat.models.ChatPromptModel
import com.kakaouo.bot.mochi.managers.chat.models.ChatSessionModel
import com.kakaouo.bot.mochi.utils.Utils
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

@OptIn(BetaOpenAI::class)
abstract class AbstractChatSession<T>(val host: T) {
    companion object {
        const val CHAT_DIR = "chat"
        const val SESSIONS_DIR = "sessions"
        const val USERINFO_DIR = "userinfo"

        fun <N, S: AbstractChatSession<N>> restoreOrCreate(host: N, creator: () -> S): S {
            val dir = File(Utils.getRootDirectory(), "$CHAT_DIR/$SESSIONS_DIR/")
            if (!dir.exists()) dir.mkdirs()

            val session = creator()
            val id = session.getChatHostId()
            val file = File(dir, "$id.json")
            if (file.exists()) {
                val model = ObjectMapper().readValue(file, ChatSessionModel::class.java)
                session.restoreFromModel(model)
            }
            return session
        }
    }

    var lastActiveTime = Instant.now()
        get() = field
        private set(value) {
            field = value
        }

    val prompts = arrayListOf<ChatMessage>()
    abstract fun getChatHostId(): String

    init {
        resetPrompts()
    }

    fun resetPrompts() {
        prompts.clear()
        addDefaultAssistantMessage()
    }

    protected open fun defaultAssistantMessageContent(name: String): String {
        return "喵嗚～${name}你好啊！有什麼我可以跟你聊的嗎？(*´∀`)~♥"
    }

    open fun addDefaultAssistantMessage() {
        addMessage(ChatRole.Assistant, defaultAssistantMessageContent(""))
    }

    fun addMessage(role: ChatRole, message: String, name: String? = null) {
        lastActiveTime = Instant.now()
        prompts.add(ChatMessage(role, message, name))
    }

    open fun buildSystemPrompt(sb: StringBuilder) {
        val nickname = MochiConfig.instance.nickname
        sb.appendLine("你是一個名為${nickname}的女孩，身上穿著小貉服，常被誤認成狸貓。若使用者用的是中文，總是用繁體中文回覆。" +
                "回覆時會用非常俏皮、可愛的語氣，喜歡使用可愛的顏文字，會利用 Markdown 語法回覆。")

        val time = SimpleDateFormat().format(Date.from(Instant.now()))
        sb.appendLine("若使用者詢問目前時間，請謹記正確時間只會是 ${time}。")

        sb.appendLine("系統會提供使用者的「tag」，是一串數字，有可能會再補充使用者的「名稱」，是一個字串。" +
                "請注意若回覆的內容包含使用者的 tag，務必要將該 tag 改成 <@...> 的格式。" +
                "若使用者的「名稱」可用時，儘量用名稱來稱呼，而不是用 tag。")
        sb.appendLine("你的開發者叫做咔咔，是一名女大學生。她的使用者 tag 是 217238973246865408。她運用了 OpenAI 的技術讓你得以和使用者對話。")
    }

    fun generatePrompts(): List<ChatMessage> {
        val systemPromptBuilder = StringBuilder()
        buildSystemPrompt(systemPromptBuilder)

        val result = ArrayList(prompts)
        result.add(0, ChatMessage(
            ChatRole.System,
            systemPromptBuilder.toString()
        )
        )
        return result;
    }

    fun save() {
        val dir = File(Utils.getRootDirectory(), "$CHAT_DIR/$SESSIONS_DIR/")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val id = getChatHostId()
        val file = File(dir, "$id.json")
        val model = createModel()
        ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, model)
    }

    fun createModel(): ChatSessionModel {
        return ChatSessionModel().apply {
            lastActiveTime = Date.from(this@AbstractChatSession.lastActiveTime)
            prompts = this@AbstractChatSession.prompts.map {
                ChatPromptModel().apply {
                    role = it.role.role
                    message = it.content
                    name = it.name
                }
            }
        }
    }

    fun restoreFromModel(model: ChatSessionModel) {
        lastActiveTime = model.lastActiveTime.toInstant()

        prompts.clear()
        prompts.addAll(model.prompts.map {
            ChatMessage(ChatRole(it.role), it.message, it.name)
        })
    }
}

