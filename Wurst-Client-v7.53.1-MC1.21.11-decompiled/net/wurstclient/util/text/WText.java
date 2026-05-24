package net.wurstclient.util.text;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.wurstclient.util.text.WLiteralTextContent;
import net.wurstclient.util.text.WTextContent;
import net.wurstclient.util.text.WTranslatedTextContent;

public final class WText {
    private final ArrayList<WTextContent> contents;

    private WText(WTextContent ... contents) {
        this.contents = Lists.newArrayList(contents);
    }

    public static WText literal(String text) {
        return new WText(new WLiteralTextContent(text));
    }

    public static WText translated(String key, Object ... args) {
        return new WText(new WTranslatedTextContent(key, args));
    }

    public static WText empty() {
        return new WText(WLiteralTextContent.EMPTY);
    }

    public WText append(WText text) {
        this.contents.addAll(text.contents);
        return this;
    }

    public WText append(String text) {
        return this.append(WText.literal(text));
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (WTextContent content : this.contents) {
            builder.append(content);
        }
        return builder.toString();
    }
}
