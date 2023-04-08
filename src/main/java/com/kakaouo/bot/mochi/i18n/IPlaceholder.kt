package com.kakaouo.bot.mochi.i18n

@FunctionalInterface
interface IPlaceholder {
    fun writePlaceholders(placeholders: MutableMap<String, Any>)
    operator fun invoke(placeholders: MutableMap<String, Any>) = writePlaceholders(placeholders)
}
