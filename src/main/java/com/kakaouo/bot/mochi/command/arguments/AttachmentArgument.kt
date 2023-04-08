package com.kakaouo.bot.mochi.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.dv8tion.jda.api.entities.Message.Attachment
import java.util.NoSuchElementException

class AttachmentArgument : ArgumentType<Attachment> {
    companion object {
        private val map = mutableMapOf<String, Attachment>()

        fun register(attachment: Attachment) {
            map[attachment.id] = attachment
        }

        fun serialize(attachment: Attachment): String {
            return "#attachment:${attachment.id}#"
        }
    }

    override fun parse(reader: StringReader): Attachment {
        for (c in "#attachment:") {
            reader.expect(c)
        }

        val str = reader.readStringUntil('#')
        val attachment = map[str] ?:
            throw NoSuchElementException("Attachment ID $str is not registered")

        map.remove(str)
        return attachment
    }
}