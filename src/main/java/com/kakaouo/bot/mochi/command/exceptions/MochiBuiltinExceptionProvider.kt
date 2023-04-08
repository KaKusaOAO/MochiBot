package com.kakaouo.bot.mochi.command.exceptions

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.*

object MochiBuiltinExceptionProvider : BuiltInExceptionProvider {
    private val builtin = BuiltInExceptions()

    override fun doubleTooLow(): Dynamic2CommandExceptionType {
        return Dynamic2CommandExceptionType { found, min ->
            LiteralMessage("輸入的小數 $found 低於下限 $min")
        }
    }

    override fun doubleTooHigh(): Dynamic2CommandExceptionType {
        return Dynamic2CommandExceptionType { found, max ->
            LiteralMessage("輸入的小數 $found 高於上限 $max")
        }
    }

    override fun floatTooLow(): Dynamic2CommandExceptionType {
        return Dynamic2CommandExceptionType { found, min ->
            LiteralMessage("輸入的小數 $found 低於下限 $min")
        }
    }

    override fun floatTooHigh(): Dynamic2CommandExceptionType {
        return Dynamic2CommandExceptionType { found, max ->
            LiteralMessage("輸入的小數 $found 高於上限 $max")
        }
    }

    override fun integerTooLow(): Dynamic2CommandExceptionType {
        return Dynamic2CommandExceptionType { found, min ->
            LiteralMessage("輸入的整數 $found 低於下限 $min")
        }
    }

    override fun integerTooHigh(): Dynamic2CommandExceptionType {
        return Dynamic2CommandExceptionType { found, max ->
            LiteralMessage("輸入的整數 $found 高於上限 $max")
        }
    }

    override fun longTooLow(): Dynamic2CommandExceptionType {
        return Dynamic2CommandExceptionType { found, min ->
            LiteralMessage("輸入的整數 $found 低於下限 $min")
        }
    }

    override fun longTooHigh(): Dynamic2CommandExceptionType {
        return Dynamic2CommandExceptionType { found, max ->
            LiteralMessage("輸入的整數 $found 高於上限 $max")
        }
    }

    override fun literalIncorrect(): DynamicCommandExceptionType {
        return builtin.literalIncorrect()
    }

    override fun readerExpectedStartOfQuote(): SimpleCommandExceptionType {
        return builtin.readerExpectedStartOfQuote()
    }

    override fun readerExpectedEndOfQuote(): SimpleCommandExceptionType {
        return builtin.readerExpectedEndOfQuote()
    }

    override fun readerInvalidEscape(): DynamicCommandExceptionType {
        return builtin.readerInvalidEscape()
    }

    override fun readerInvalidBool(): DynamicCommandExceptionType {
        return DynamicCommandExceptionType {
            LiteralMessage("'$it' 不是 true 或 false")
        }
    }

    override fun readerInvalidInt(): DynamicCommandExceptionType {
        return DynamicCommandExceptionType {
            LiteralMessage("'$it' 不是有效的整數")
        }
    }

    override fun readerExpectedInt(): SimpleCommandExceptionType {
        return SimpleCommandExceptionType(LiteralMessage("需要接一個整數"))
    }

    override fun readerInvalidLong(): DynamicCommandExceptionType {
        return builtin.readerInvalidLong()
    }

    override fun readerExpectedLong(): SimpleCommandExceptionType {
        return builtin.readerExpectedLong()
    }

    override fun readerInvalidDouble(): DynamicCommandExceptionType {
        return builtin.readerInvalidDouble()
    }

    override fun readerExpectedDouble(): SimpleCommandExceptionType {
        return builtin.readerExpectedDouble()
    }

    override fun readerInvalidFloat(): DynamicCommandExceptionType {
        return builtin.readerInvalidFloat()
    }

    override fun readerExpectedFloat(): SimpleCommandExceptionType {
        return builtin.readerExpectedFloat()
    }

    override fun readerExpectedBool(): SimpleCommandExceptionType {
        return builtin.readerExpectedBool()
    }

    override fun readerExpectedSymbol(): DynamicCommandExceptionType {
        return builtin.readerExpectedSymbol()
    }

    override fun dispatcherUnknownCommand(): SimpleCommandExceptionType {
        return builtin.dispatcherUnknownCommand()
    }

    override fun dispatcherUnknownArgument(): SimpleCommandExceptionType {
        return builtin.dispatcherUnknownArgument()
    }

    override fun dispatcherExpectedArgumentSeparator(): SimpleCommandExceptionType {
        return SimpleCommandExceptionType(LiteralMessage("你後面多打了什麼東西嗎？"))
    }

    override fun dispatcherParseException(): DynamicCommandExceptionType {
        return DynamicCommandExceptionType { DispatcherParseFailureMessage(it) }
    }
}

