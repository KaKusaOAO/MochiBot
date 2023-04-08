package com.kakaouo.bot.mochi.i18n

interface ILanguageGenerator {
    fun addEntry(key: String, content: String): ILanguageGenerator
    fun addEntry(key: String, vararg pairs: Pair<String, String>): LanguageGenerator
}