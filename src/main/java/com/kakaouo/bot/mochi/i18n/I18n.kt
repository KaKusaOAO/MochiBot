package com.kakaouo.bot.mochi.i18n

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import com.kakaouo.bot.mochi.config.MochiConfig
import com.kakaouo.bot.mochi.texts.LiteralText.Companion.toText
import com.kakaouo.bot.mochi.texts.TextColor
import com.kakaouo.bot.mochi.texts.TranslateText
import com.kakaouo.bot.mochi.utils.Logger
import com.kakaouo.bot.mochi.utils.Utils
import com.nfeld.jsonpathkt.extension.read
import org.stringtemplate.v4.ST
import java.io.File
import java.util.Locale

open class I18n {
    companion object {
        private const val BASE_NAME = "base"
        private const val LOCALE_DIR = "lang"
        private val baseLocale: JsonNode?

        init {
            baseLocale = getLocaleJson(BASE_NAME)
        }

        fun getLocaleJson(name: String): JsonNode? {
            val localeDir = File(Utils.getRootDirectory(), "$LOCALE_DIR/")
            if (!localeDir.exists()) {
                localeDir.mkdir()
            }

            val baseFile = File(localeDir, "$name.json")
            return if (baseFile.exists()) {
                JsonMapper().readTree(baseFile)
            } else {
                null
            }
        }
    }

    val parent: I18n?
    private var currentLocale: String? = null
    private var locale: JsonNode? = null

    constructor(locale: Locale?, parent: I18n? = null) : this(locale?.toString() ?: "", parent)

    // Sometimes we need custom locale as well
    constructor(locale: String? = "", parent: I18n? = null) {
        var p = parent
        if (p == null && !(locale.isNullOrEmpty() || locale == BASE_NAME)) {
            p = I18n()
        }
        this.parent = p

        if (!locale.isNullOrEmpty()) {
            setLocale(locale)
        }
    }

    fun setLocale(locale: String?) {
        currentLocale = locale
        if (locale.isNullOrEmpty()) return
        this.locale = getLocaleJson(locale)
    }

    fun getOrNull(key: String): String? {
        return locale?.get(key)?.textValue() ?:
        locale?.read<String>("$.$key") ?:
        parent?.get(key) ?:
        baseLocale?.get(key)?.textValue() ?:
        baseLocale?.read<String>("$.$key")
    }

    fun get(key: String): String {
        val result = getOrNull(key)
        if (result != null) return result

        Logger.warn(
            TranslateText.of("The message is not set for key %s!")
                .addWith(key.toText().setColor(TextColor.AQUA))
        )
        return key
    }

    fun of(key: String, placeholders: Map<String, Any> = mapOf()): String {
        val map = HashMap(placeholders)
        writeDefaultPlaceholders(map)

        val formatter = ST(get(key))
        for (entry in map) {
            formatter.add(entry.key, entry.value)
        }
        return formatter.render()
    }

    open fun writeDefaultPlaceholders(placeholders: MutableMap<String, Any>) {
        placeholders["nickname"] = getOrNull("bot.nickname") ?: MochiConfig.instance.nickname
    }

    fun of(key: String, vararg placeholders: IPlaceholder): String {
        val dict = mutableMapOf<String, Any>()
        for (p in placeholders) {
            p.writePlaceholders(dict)
        }

        return of(key, dict)
    }
}