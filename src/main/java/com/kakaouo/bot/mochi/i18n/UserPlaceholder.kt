package com.kakaouo.bot.mochi.i18n

import net.dv8tion.jda.api.entities.User

class UserPlaceholder(val user: User, name: String = "user") : MentionablePlaceholder(user, name)