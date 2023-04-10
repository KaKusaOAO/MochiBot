package com.kakaouo.bot.mochi.i18n

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.kakaouo.bot.mochi.command.Command
import com.kakaouo.bot.mochi.managers.player.PlayerManager
import com.kakaouo.mochi.utils.Utils
import java.io.File

object LanguageGenerator : ILanguageGenerator {
    private val localeJsonMap = mutableMapOf<String, ObjectNode>()
    private val localizables = mutableSetOf<ILocalizable>()

    fun <T: ILocalizable> register(obj: T) {
        localizables.add(obj)
    }

    init {
        register(Command)
        register(PlayerManager)
    }

    fun generate() {
        localeJsonMap.clear()
        for (obj in localizables) {
            obj.registerLocalizations(this)
        }
        write()
    }

    private fun write() {
        val langDir = File(Utils.getRootDirectory(), "lang/")
        if (!langDir.exists()) langDir.mkdir()

        for (entry in localeJsonMap) {
            val locale = entry.key
            val node = entry.value

            val file = File(langDir, "$locale.json")
            JsonMapper().writerWithDefaultPrettyPrinter().writeValue(file, node)
        }
    }

    override fun addEntry(key: String, content: String): LanguageGenerator {
        return addEntry(key, Pair("base", content))
    }

    override fun addEntry(key: String, vararg pairs: Pair<String, String>): LanguageGenerator {
        for (entry in pairs) {
            val locale = entry.first
            val content = entry.second

            if (!localeJsonMap.containsKey(locale)) {
                localeJsonMap[locale] = ObjectNode(JsonNodeFactory(true))
            }

            localeJsonMap[locale]!!.set<TextNode>(key, TextNode(content))
        }

        return this
    }
}

