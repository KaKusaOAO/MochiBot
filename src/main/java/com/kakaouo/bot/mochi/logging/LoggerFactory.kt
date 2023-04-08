package com.kakaouo.bot.mochi.logging

import org.slf4j.ILoggerFactory
import org.slf4j.Logger

class LoggerFactory : ILoggerFactory {
    override fun getLogger(name: String?): Logger {
        return LoggerWrapper(name)
    }
}