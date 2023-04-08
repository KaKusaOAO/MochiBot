package com.kakaouo.bot.mochi.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.kakaouo.bot.mochi.managers.GuildManager
import com.kakaouo.bot.mochi.utils.Utils
import java.io.File

class GuildConfig(val guildManager: GuildManager) :
    BaseConfig<GuildConfig.Data>(guildManager.rootPath + "config.json", Data::class.java) {

    override val skipLines = 3

    class Data: IBaseConfigData {
        override var version = 1
        var isNsfwEnabled = false
        var chatBot = ChatBotConfig()
    }

    class PlayerConfig {
        var lastTextChannelId = "0"
        var lastPlayingMessageId = "0"
        var isSilentMode = false
        var respondIfLastMemberLeft = false
    }

    class ChatBotConfig {
        var enabled = true
        var chatChannelId = "0"
    }

    override fun prepareConfigPath() {
        val guildPath = "guilds"
        val dir = File(Utils.getRootDirectory(), "$guildPath/${guildManager.guild.id}/")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = file
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    override fun resolveDefault() = Data()

    override fun save() {
        val writer = file.writer()
        writer.write("// Guild: ${guildManager.guild.name}\n")
        writer.write("// ID: #${guildManager.guild.id}\n\n")
        ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(writer, data)
        writer.close()
    }
}