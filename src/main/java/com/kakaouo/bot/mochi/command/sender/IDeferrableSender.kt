package com.kakaouo.bot.mochi.command.sender

interface IDeferrableSender {
    suspend fun defer()
}