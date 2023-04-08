package com.kakaouo.bot.mochi.texts;

public interface IAsciiColor {
    String toAsciiCode();

    static IAsciiColor fromTextColor(TextColor color) {
        // TextColor closest = color.toNearestPredefinedColor();
        // char code = closest.toString().charAt(1);
        // return AsciiColor.of(code);
        return new RgbAsciiColor(color.getColor());
    }
}
