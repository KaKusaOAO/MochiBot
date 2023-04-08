package com.kakaouo.bot.mochi.logging

import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.MarkerFactory
import org.slf4j.helpers.BasicMDCAdapter
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

class LoggingServiceProvider : SLF4JServiceProvider {
    override fun getLoggerFactory(): ILoggerFactory {
        return LoggerFactory()
    }

    override fun getMarkerFactory(): IMarkerFactory {
        return MarkerFactory.getIMarkerFactory()
    }

    override fun getMDCAdapter(): MDCAdapter {
        return BasicMDCAdapter()
    }

    override fun getRequestedApiVersion(): String {
        return "2.0.1"
    }

    override fun initialize() {

    }
}