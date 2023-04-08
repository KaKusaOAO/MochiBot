package com.kakaouo.bot.mochi.config

import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.kakaouo.bot.mochi.texts.LiteralText.Companion.toText
import com.kakaouo.bot.mochi.utils.Logger
import com.kakaouo.bot.mochi.utils.Utils
import java.io.File

abstract class BaseConfig<T: IBaseConfigData>(path: String, val clz: Class<T>) {
    var data = clz.getConstructor().newInstance()
    val file = File(Utils.getRootDirectory(), path)
    open val skipLines = 0

    protected open fun prepareConfigPath() {
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    open fun load() {
        prepareConfigPath()
        data = try {
            val reader = file.reader()

            var lines = 0
            while (true) {
                if (lines >= skipLines) break

                val r = reader.read()
                if (r.toChar() == '\n') {
                    lines++
                }
            }

            ObjectMapper().readValue(reader, clz)
        } catch (ex: MismatchedInputException) {
            resolveDefault()
        } catch (ex: Throwable) {
            Logger.error("An error occurred while reading config!")
            Logger.error(ex)
            resolveDefault()
        }

        save()
    }

    protected abstract fun resolveDefault(): T

    open fun save() {
        ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, data)
    }
}

