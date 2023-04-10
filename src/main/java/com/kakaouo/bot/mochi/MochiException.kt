package com.kakaouo.bot.mochi

import com.kakaouo.bot.mochi.i18n.MochiI18n

open class MochiException(
    message: String? = null,
    val isFatal: Boolean = false,
    cause: Throwable? = null) : RuntimeException(message, cause) {

    private var localizer: ((MochiI18n) -> String)? = null
    val canSurvive get() = !isFatal

    fun getLocalizedMessage(i18n: MochiI18n) = localizer?.invoke(i18n) ?: message

    fun <T: MochiException> T.localized(block: (MochiI18n) -> String): T {
        localizer = block
        return this
    }
}
