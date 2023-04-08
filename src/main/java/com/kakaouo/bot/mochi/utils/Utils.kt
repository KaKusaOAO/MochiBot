package com.kakaouo.bot.mochi.utils

import com.kakaouo.bot.mochi.utils.Utils.toCoroutine
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.CompletableFuture

object Utils {
    fun getRootDirectory(): File {
        return if (isRunningInIDE()) {
            val dir = File("bot/")
            if (!dir.exists()) dir.mkdir()
            dir
        } else File("")
    }

    fun isRunningInIDE(): Boolean {
        return try {
            Class.forName("com.intellij.rt.execution.application.AppMainV2")
            true
        } catch (ex: Throwable) {
            false
        }
    }

    fun <T> T?.asNullable(): T? = this

    @OptIn(DelicateCoroutinesApi::class)
    fun <T> async(block: suspend () -> T): Deferred<T> {
        return GlobalScope.async inner@ {
            return@inner block()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun asyncDiscard(block: suspend () -> Unit) {
        @Suppress("DeferredResultUnused")
        GlobalScope.async inner@ {
            try {
                return@inner block()
            } catch (ex: Throwable) {
                Logger.error("Unhandled exception is discarded!")
                Logger.error(ex.stackTraceToString())
            }
        }
    }

    @JvmName("promisifyVoid")
    fun <T> promisify(block: suspend () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        asyncDiscard {
            try {
                val result = block()
                future.complete(result)
            } catch(ex: Throwable) {
                future.completeExceptionally(ex)
            }
        }

        return future
    }

    fun promisify(block: suspend () -> Unit): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        asyncDiscard {
            try {
                block()
                future.complete(null)
            } catch(ex: Throwable) {
                future.completeExceptionally(ex)
            }
        }

        return future
    }

    @JvmName("toVoidCoroutine")
    suspend fun CompletableFuture<Void>.toCoroutune() {
        var completed = false
        var faulted = false
        var err: Throwable? = null

        this.whenComplete { _, e ->
            faulted = e != null
            err = e
            completed = true
        }

        while (!completed) {
            // delay(16)
            yield()
        }

        if (faulted) {
            throw err!!
        }
    }

    suspend fun <T> CompletableFuture<T>.toCoroutine(): T? {
        var completed = false
        var faulted = false
        var result: T? = null
        var err: Throwable? = null

        this.whenComplete { v, e ->
            faulted = e != null
            err = e
            result = v

            // Should be placed at the last :P...
            completed = true
        }

        while (!completed) {
            // delay(16)
            yield()
        }

        if (faulted) {
            throw err!!
        }

        if (result == null) {
            Logger.warn("Result is null!")
        }
        return result
    }
}