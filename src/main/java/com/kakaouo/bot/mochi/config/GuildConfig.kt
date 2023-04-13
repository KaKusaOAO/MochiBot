package com.kakaouo.bot.mochi.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.kakaouo.bot.mochi.managers.GuildManager
import com.kakaouo.mochi.config.BaseConfig
import com.kakaouo.mochi.utils.Utils
import java.io.File

class GuildConfig (val guildManager: GuildManager) :
    BaseConfig<GuildConfig.Data>(guildManager.rootPath + "config.json", Data::class.java) {

    // The JSON parser we are using doesn't support comments in payload.
    // We need to skip these comment lines!
    override fun getSkipLineCount() = 3

    class Data: IBaseConfigData {
        override var version = 1
        var isNsfwEnabled = false
        var chatBot = ChatBotConfig()
        var player = PlayerConfig()
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

        // Always write the guild basic info as comments
        writer.write("// Guild: ${guildManager.guild.name}\n")
        writer.write("// ID: #${guildManager.guild.id}\n\n")

        // Write the JSON payload
        ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(writer, data)
        writer.close()
    }
}