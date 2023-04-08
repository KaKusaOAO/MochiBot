package com.kakaouo.bot.mochi.texts;

import java.util.HashMap;
import java.util.Map;

public enum AsciiColor implements IAsciiColor {
    BLACK('0', 30),
    DARK_BLUE('1', 34),
    DARK_GREEN('2', 32),
    DARK_AQUA('3', 36),
    DARK_RED('4', 31),
    DARK_PURPLE('5', 35),
    GOLD('6', 33),
    GRAY('7', 37),
    DARK_GRAY('8', 30, true),
    BLUE('9', 34, true),
    GREEN('a', 32, true),
    AQUA('b', 36, true),
    RED('c', 31, true),
    PURPLE('d', 35, true),
    YELLOW('e', 33, true),
    WHITE('f', 37, true);

    private char colorCode;
    private int color;
    private boolean bright;

    private static final Map<Character, AsciiColor> BY_CODE = new HashMap<>();

    AsciiColor(char code, int color) {
        this(code, color, false);
    }

    AsciiColor(char code, int color, boolean isBright) {
        this.colorCode = code;
        this.color = color;
        this.bright = isBright;
    }

    public String toAsciiCode() {
        return "\u001b[" + (this.bright ? "1;" : "0;") + this.color + "m";
    }

    public static AsciiColor of(char c) {
        if (BY_CODE.containsKey(c)) {
            return BY_CODE.get(c);
        } else {
            throw new IllegalArgumentException(String.format("Color of '%c' is not defined", c));
        }
    }

    static {
        for (AsciiColor color : AsciiColor.values()) {
            BY_CODE.put(color.colorCode, color);
        }
    }
}
