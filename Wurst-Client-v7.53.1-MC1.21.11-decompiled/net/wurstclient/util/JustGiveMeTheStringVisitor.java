package net.wurstclient.util;

import net.minecraft.class_2583;
import net.minecraft.class_5224;

public class JustGiveMeTheStringVisitor
implements class_5224 {
    private final StringBuilder sb = new StringBuilder();

    public boolean accept(int index, class_2583 style, int codePoint) {
        this.sb.appendCodePoint(codePoint);
        return true;
    }

    public String toString() {
        return this.sb.toString();
    }
}
