package com.kakaouo.bot.mochi.config

import com.knuddels.jtokkit.api.ModelType
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity.ActivityType

class MochiConfig : BaseConfig<MochiConfig.Data>(FILE_NAME, Data::class.java) {
    companion object {
        const val FILE_NAME = "config.json"
        private var _inst: MochiConfig? = null
        val instance: MochiConfig get() {
            val result = _inst ?: MochiConfig()
            if (_inst == null) {
                _inst = result
            }
            return result
        }
    }

    init {
        load()
        save()
    }

    class EmailConfig {
        var smtpServer = ""
        var account = ""
        var password = ""
        var source = ""
        var destination = ""
    }

    class ActivityConfig {
        var name = "大家睡覺...zzZ"
        var type = ActivityType.WATCHING
    }

    class RepoConfig {
        var provider = "GitHub"
        var url = "https://github.com/KaKusaOAO/Sayu"
    }

    class ChatBotConfig {
        var model = "gpt-3.5-turbo"
        var enabled = false
        var secret = ""
        var allowDMChat = true
    }

    class Data: IBaseConfigData {
        override var version = 1
        var token = ""
        var email = EmailConfig()
        var nickname = "ㄚˇ糰"
        var commandName = "mochi"
        var locale = "base"
        var commandPrefix = "&"
        var potatoResponses = listOf<String>()
        var potatoRate = 0.25
        var status = OnlineStatus.ONLINE
        var activity = ActivityConfig()
        var repository = RepoConfig()
        var chatBot = ChatBotConfig()
    }

    val nickname get() = data.nickname

    override fun resolveDefault() = Data()
}