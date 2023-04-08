package com.kakaouo.bot.mochi.texts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Text<T extends Text<T>> {
    public List<Text<?>> extra = new ArrayList<>();
    public Text<?> parent = null;
    public TextColor color = null;
    public boolean bold = false;
    public boolean italic = false;
    public boolean obfuscated = false;
    public boolean underlined = false;
    public boolean strikethrough = false;
    public boolean reset = false;

    public TextColor getParentColor() {
        if (parent == null) return TextColor.WHITE;
        return Optional.ofNullable(parent.color).orElse(parent.getParentColor());
    }

    public String toAscii() {
        StringBuilder extra = new StringBuilder();
        for (Text<?> e : this.extra) {
            extra.append(e.toAscii());
            extra.append(Optional.ofNullable(this.color).orElse(this.getParentColor()).toAsciiCode());
        }
        return extra.append(this.getParentColor().toAsciiCode()).toString();
    }

    public String toPlainText() {
        StringBuilder extra = new StringBuilder();
        for (Text<?> e : this.extra) {
            extra.append(e.toPlainText());
        }
        return extra.toString();
    }

    public static <T> Text<?> representClass(Class<T> clz, TextColor color) {
        String name = clz == null ? "?" : clz.getTypeName().substring(clz.getPackageName().length() + 1);
        String pack = clz == null ? "?" : clz.getPackageName();
        return TranslateText.of("%s." + name)
                .setColor(Optional.ofNullable(color).orElse(TextColor.GOLD))
                .addWith(LiteralText.of(pack)
                        .setColor(TextColor.DARK_GRAY));
    }

    public static <T> Text<?> representClass(Class<T> clz) {
        return Text.representClass(clz, null);
    }

    protected abstract T resolveThis();

    public T setColor(TextColor color) {
        this.color = color;
        return this.resolveThis();
    }

    public T addExtra(Text<?> text) {
        this.extra.add(text);
        return this.resolveThis();
    }

    protected abstract T createCopy();

    public T copy() {
        T clone = this.createCopy();
        clone.color = this.color;
        clone.extra = new ArrayList<>(this.extra);
        return clone;
    }
}

