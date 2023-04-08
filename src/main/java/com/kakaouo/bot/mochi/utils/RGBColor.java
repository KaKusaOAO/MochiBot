package com.kakaouo.bot.mochi.utils;

public class RGBColor {
    public int red;
    public int green;
    public int blue;

    public RGBColor(int red, int green, int blue) {
        this.red = red & 0xff;
        this.green = green & 0xff;
        this.blue = blue & 0xff;
    }

    public RGBColor(int hex) {
        this.blue = hex & 0xff;
        hex >>>= 8;
        this.green = hex & 0xff;
        hex >>>= 8;
        this.red = hex & 0xff;
    }

    public int getRGB() {
        return (((red << 8) | green) << 8) | blue;
    }

    @Override
    public String toString() {
        return "#" + Integer.toString(this.getRGB(), 16);
    }
}
