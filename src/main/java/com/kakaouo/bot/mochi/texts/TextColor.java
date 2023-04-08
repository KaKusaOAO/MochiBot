package com.kakaouo.bot.mochi.texts;

import com.kakaouo.bot.mochi.utils.RGBColor;

import java.util.HashMap;
import java.util.Map;

public final class TextColor {
    private static final char COLOR_CHAR = '\u00a7';

    private static int count = 0;
    private static final Map<Character, TextColor> BY_CHAR = new HashMap<>();
    private static final Map<String, TextColor> BY_NAME = new HashMap<>();

    private String name;
    private final int ordinal;
    private final String toString;
    private final RGBColor color;

    public static final TextColor BLACK       = new TextColor('0', "black",       new RGBColor(0));
    public static final TextColor DARK_BLUE   = new TextColor('1', "dark_blue",   new RGBColor(0xaa));
    public static final TextColor DARK_GREEN  = new TextColor('2', "dark_green",  new RGBColor(0xaa00));
    public static final TextColor DARK_AQUA   = new TextColor('3', "dark_aqua",   new RGBColor(0xaaaa));
    public static final TextColor DARK_RED    = new TextColor('4', "dark_red",    new RGBColor(0xaa0000));
    public static final TextColor DARK_PURPLE = new TextColor('5', "dark_purple", new RGBColor(0xaa00aa));
    public static final TextColor GOLD        = new TextColor('6', "gold",        new RGBColor(0xffaa00));
    public static final TextColor GRAY        = new TextColor('7', "gray",        new RGBColor(0xaaaaaa));
    public static final TextColor DARK_GRAY   = new TextColor('8', "dark_gray",   new RGBColor(0x555555));
    public static final TextColor BLUE        = new TextColor('9', "blue",        new RGBColor(0x5555ff));
    public static final TextColor GREEN       = new TextColor('a', "green",       new RGBColor(0x55ff55));
    public static final TextColor AQUA        = new TextColor('b', "aqua",        new RGBColor(0x55ffff));
    public static final TextColor RED         = new TextColor('c', "red",         new RGBColor(0xff5555));
    public static final TextColor PURPLE      = new TextColor('d', "purple",      new RGBColor(0xff55ff));
    public static final TextColor YELLOW      = new TextColor('e', "yellow",      new RGBColor(0xffff55));
    public static final TextColor WHITE       = new TextColor('f', "white",       new RGBColor(0xffffff));

    private TextColor(char code, String name, RGBColor color) {
        this.name = name;
        this.toString = COLOR_CHAR + "" + code;
        this.ordinal = count++;
        this.color = color;

        BY_CHAR.put(code, this);
        BY_NAME.put(name, this);
    }

    private TextColor(String name, String toString, int hex) {
        this.name = name;
        this.toString = toString;
        this.ordinal = -1;
        this.color = new RGBColor(hex);
    }

    public static TextColor of(RGBColor color) {
        return TextColor.of(color.toString());
    }

    public static TextColor of(String name) {
        if (name == null) throw new IllegalArgumentException("name cannot be null");

        if (name.startsWith("#") && name.length() == 7) {
            int rgb;
            try {
                rgb = Integer.parseInt(name.substring(1), 16);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Illegal hex string " + name);
            }

            StringBuilder magic = new StringBuilder(COLOR_CHAR + "x");
            for (char c : magic.substring(1).toCharArray()) {
                magic.append(COLOR_CHAR + "").append(c);
            }
            return new TextColor(name, magic.toString(), rgb);
        }

        if (BY_NAME.containsKey(name)) {
            return BY_NAME.get(name);
        }

        throw new IllegalArgumentException("Could not parse TextColor " + name);
    }

    public static TextColor of(char code) {
        return BY_CHAR.getOrDefault(code, null);
    }

    @Override
    public String toString() {
        return this.toString;
    }

    public TextColor toNearestPredefinedColor() {
        char c = this.toString.charAt(1);
        if (c != 'x') return this;

        TextColor closest = null;
        RGBColor cl = this.color;

        TextColor[] defined = new TextColor[] {
            BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE,
                GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, PURPLE, YELLOW, WHITE
        };

        int smallestDiff = 0;
        for (TextColor tc : defined) {
            int rAverage = (tc.color.red + cl.red) / 2;
            int rDiff = tc.color.red - cl.red;
            int gDiff = tc.color.green - cl.green;
            int bDiff = tc.color.blue - cl.blue;

            int diff = ((2 + (rAverage >> 8)) * rDiff * rDiff)
                    + (4 * gDiff * gDiff)
                    + ((2 + ((255 - rAverage) >> 8)) * bDiff * bDiff);

            if (closest == null || diff < smallestDiff) {
                closest = tc;
                smallestDiff = diff;
            }
        }

        return closest;
    }

    public RGBColor getColor() {
        return color;
    }

    public String toAsciiCode() {
        return IAsciiColor.fromTextColor(this).toAsciiCode();
    }
}
