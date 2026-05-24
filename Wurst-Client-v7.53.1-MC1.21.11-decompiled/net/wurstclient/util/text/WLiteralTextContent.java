package net.wurstclient.util.text;

import java.util.Objects;
import net.wurstclient.util.text.WTextContent;

public final class WLiteralTextContent
implements WTextContent {
    public static final WLiteralTextContent EMPTY = new WLiteralTextContent("");
    private final String text;

    public WLiteralTextContent(String text) {
        this.text = Objects.requireNonNull(text);
    }

    public String toString() {
        return this.text;
    }
}
