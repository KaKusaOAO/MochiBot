package com.kakaouo.bot.mochi.managers.chat

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.kakaouo.bot.mochi.Mochi
import com.kakaouo.bot.mochi.command.Constants
import com.kakaouo.bot.mochi.command.sender.DiscordInteractionSender
import com.kakaouo.bot.mochi.command.sender.DiscordMessageSender
import com.kakaouo.bot.mochi.command.sender.IDiscordCommandSender
import com.kakaouo.bot.mochi.config.MochiConfig
import com.kakaouo.bot.mochi.i18n.MochiI18n
import com.kakaouo.bot.mochi.texts.Texts
import com.kakaouo.mochi.texts.LiteralText
import com.kakaouo.mochi.texts.TextColor
import com.kakaouo.mochi.texts.TextKt.toText
import com.kakaouo.mochi.texts.TranslateText
import com.kakaouo.mochi.utils.Logger
import com.kakaouo.mochi.utils.UtilsKt
import com.knuddels.jtokkit.Encodings
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import java.io.Closeable

@OptIn(BetaOpenAI::class)
class ChatBotManager : Closeable, EventListener {
    var api: OpenAI? = null
    private val sessions = mutableMapOf<String, AbstractChatSession<*>>()

    init {
        reloadApi()

        val client = Mochi.instance.client
        client.addEventListener(this)
    }

    private fun reloadApi() {
        val config = Mochi.instance.config.data.chatBot

        if (!config.enabled) {
            Logger.verbose("Chat bot is disabled.")
            api = null
            return
        }

        val secret = config.secret
        if (secret.isEmpty() || secret == "<insert secret here>") {
            Logger.warn("OpenAI secret is not set! Chat bot is disabled.")
            api = null
            return
        }

        Logger.verbose("Reloading API with the specified secret...")

        val apiConfig = OpenAIConfig(secret, logger = com.aallam.openai.api.logging.Logger.Empty)
        api = OpenAI(apiConfig)
    }

    fun resetSession(userId: String) {

    }

    fun handleCheckedMessage(sender: IDiscordCommandSender, message: Message, i18n: MochiI18n) {
        val api = api ?: return

        val user = message.author
        val isSelfMessage = sender.author.id == user.id

        /* {
            UtilsKt.asyncDiscard {
                sender.respond(embed = sender.createStyledEmbed {
                    setColor(Constants.ERROR_COLOR)
                    setDescription("只能回覆您自己的訊息！")
                }.build(), ephemeral = true)
            }
            return
        }
         */

        val channel = message.channel
        val guild = if (message.isFromGuild) message.guild else null
        val content = message.contentRaw

        val isGuildHost = guild != null
        val id = if (isGuildHost) guild!!.id else user.id
        val session = sessions[id] ?: run {
            val s = if (isGuildHost)
                AbstractChatSession.restoreOrCreate(guild!!) {
                    GuildChatSession(guild)
                }
            else
                AbstractChatSession.restoreOrCreate(user) {
                    UserChatSession(user)
                }

            sessions[id] = s
            s
        }

        fun sendErrorEmbed(error: String) {
            UtilsKt.asyncDiscard {
                sender.respond(embed = sender.createStyledEmbed {
                    setDescription(error)
                    setColor(Constants.ERROR_COLOR)
                }.build(), allowedMentions = listOf())
            }
        }

        val registry = Encodings.newDefaultEncodingRegistry()
        val modelName = MochiConfig.instance.data.chatBot.model
        val enc = registry.getEncodingForModel(modelName).get()
        val tokens = enc.encode(content)

        if (tokens.size > 1000) {
            Logger.warn(
                TranslateText.of("Message sent by %s is too long! (>1000 tokens)")
                    .addWith(Texts.ofUser(user))
            )

            UtilsKt.asyncDiscard {
                sendErrorEmbed("您的訊息太長了。")
            }
            return
        }

        Logger.verbose(
            TranslateText.of("[Chat] %s: %s")
            .addWith(Texts.ofUser(user))
            .addWith(LiteralText.of(content)))

        val prompts = session.generatePrompts().toMutableList()
        prompts.add(ChatMessage(ChatRole.User, content, user.id))

        val request = ChatCompletionRequestBuilder().apply {
            model = ModelId(modelName)
            messages = prompts
            temperature = 1.0
        }.build()

        var typing = true
        UtilsKt.asyncDiscard {
            if (sender is DiscordInteractionSender) {
                sender.defer(!isSelfMessage)
            } else {
                while (typing) {
                    channel.sendTyping().queue()
                    delay(5000)
                }
            }
        }

        UtilsKt.asyncDiscard {
            val response: ChatCompletion
            try {
                response = api.chatCompletion(request)
            } catch (ex: Throwable) {
                Logger.error("An error occurred while handling chat request!")
                Logger.error(ex)
                return@asyncDiscard
            } finally {
                typing = false
            }

            val text = response.choices.first().message!!.content
            if (isSelfMessage) {
                session.addMessage(ChatRole.User, content, user.id)
                session.addMessage(ChatRole.Assistant, text)
            }

            Logger.verbose(TranslateText.of("[Chat] Response to %s: %s")
                .addWith(Texts.ofUser(user))
                .addWith(LiteralText.of(text)))

            sender.respond(text, allowedMentions = listOf(), ephemeral = !isSelfMessage)
            if (!isSelfMessage) {
                sender.respond(embed = sender.createStyledEmbed {
                    setDescription("因為這不是您自己的訊息，本次對話不會儲存。")
                }.build(), allowedMentions = listOf(), ephemeral = true)
            } else {
                val compressThreshold = 1024 * 3
                val usedTokens = response.usage?.totalTokens ?: 0
                Logger.verbose(TranslateText.of("Token usage of %s is %s/${compressThreshold}")
                    .addWith(Texts.ofUser(user))
                    .addWith(LiteralText.of(usedTokens.toString()).setColor(TextColor.AQUA)))

                if (usedTokens >= compressThreshold) {
                    compressSession(session, i18n)
                }

                session.save()
            }
        }
    }

    suspend fun compressSession(session: AbstractChatSession<*>, i18n: MochiI18n) {
        val api = api ?: return

        val modelName = MochiConfig.instance.data.chatBot.model
        Logger.info(TranslateText.of("Compressing conversation for host %s...")
            .addWith(session.getChatHostId().toText().setColor(TextColor.AQUA)))

        val temp = session.generatePrompts().toMutableList()
        temp.add(ChatMessage(ChatRole.User,
            "因為本對話即將被壓縮，請為以上對話描述重點，盡可能地保留對話細節。"))
        val request = ChatCompletionRequestBuilder().apply {
            model = ModelId(modelName)
            messages = temp
            temperature = 1.0
        }
        val response = api.chatCompletion(request.build())
        val compressed = response.choices.first().message!!.content

        session.prompts.clear()
        session.addDefaultAssistantMessage()
        session.addMessage(ChatRole.System, compressed)

        Logger.verbose(TranslateText.of("[Chat] Compressed for %s: %s")
            .addWith(session.getChatHostId().toText().setColor(TextColor.AQUA))
            .addWith(LiteralText.of(compressed)))
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
        if (!MochiConfig.instance.data.chatBot.allowDMChat) return
        if (event.isFromGuild) return
        if (!commonValidateMessage(message)) return

        val sender = DiscordMessageSender(message)
        handleCheckedMessage(sender, message, sender.i18n)
    }

    fun commonValidateMessage(message: Message): Boolean {
        if (message.contentRaw.isEmpty()) return false
        if (message.author.isBot) return false
        if (message.referencedMessage != null) return false
        if (message.contentRaw.startsWith(MochiConfig.instance.data.commandPrefix)) return false

        return true
    }
}
