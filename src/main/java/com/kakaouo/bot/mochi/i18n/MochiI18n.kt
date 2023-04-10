package com.kakaouo.bot.mochi.i18n

import com.kakaouo.bot.mochi.config.MochiConfig
import com.kakaouo.mochi.i18n.I18n
import java.util.Locale

open class MochiI18n : I18n {
    constructor(locale: Locale?, parent: MochiI18n? = null) : this(locale?.toString() ?: "", parent)

    // Sometimes we need custom locale as well
    constructor(locale: String? = "", parent: MochiI18n? = null) : super(locale, parent) {

    }

    override fun writeDefaultPlaceholders(placeholders: MutableMap<String, Any>) {
        placeholders["nickname"] = getOrNull("bot.nickname") ?: MochiConfig.instance.nickname
    }
}