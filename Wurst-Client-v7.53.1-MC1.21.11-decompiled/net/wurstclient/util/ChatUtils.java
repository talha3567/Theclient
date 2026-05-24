package net.wurstclient.util;

import java.util.List;
import java.util.StringJoiner;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_303;
import net.minecraft.class_310;
import net.minecraft.class_338;
import net.minecraft.class_5224;
import net.minecraft.class_5250;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.wurstclient.WurstClient;
import net.wurstclient.util.JustGiveMeTheStringVisitor;

public final class ChatUtils
extends Enum<ChatUtils> {
    private static final class_310 MC;
    public static final String WURST_PREFIX = "\u00a7c[\u00a76Wurst\u00a7c]\u00a7r ";
    private static final String WARNING_PREFIX = "\u00a7c[\u00a76\u00a7lWARNING\u00a7c]\u00a7r ";
    private static final String ERROR_PREFIX = "\u00a7c[\u00a74\u00a7lERROR\u00a7c]\u00a7r ";
    private static final String SYNTAX_ERROR_PREFIX = "\u00a74Syntax error:\u00a7r ";
    private static boolean enabled;
    private static final /* synthetic */ ChatUtils[] $VALUES;

    public static ChatUtils[] values() {
        return (ChatUtils[])$VALUES.clone();
    }

    public static ChatUtils valueOf(String name) {
        return Enum.valueOf(ChatUtils.class, name);
    }

    public static void setEnabled(boolean enabled) {
        ChatUtils.enabled = enabled;
    }

    public static void component(class_2561 component) {
        if (!enabled) {
            return;
        }
        class_338 chatHud = ChatUtils.MC.field_1705.method_1743();
        class_5250 prefix = class_2561.method_43470((String)WURST_PREFIX);
        chatHud.method_1812((class_2561)prefix.method_10852(component));
    }

    public static void message(String message) {
        ChatUtils.component((class_2561)class_2561.method_43470((String)message));
    }

    public static void warning(String message) {
        ChatUtils.message(WARNING_PREFIX + message);
    }

    public static void error(String message) {
        ChatUtils.message(ERROR_PREFIX + message);
    }

    public static void syntaxError(String message) {
        ChatUtils.message(SYNTAX_ERROR_PREFIX + message);
    }

    public static String getAsString(class_303.class_7590 visible) {
        return ChatUtils.getAsString(visible.comp_896());
    }

    public static String getAsString(class_5481 text) {
        JustGiveMeTheStringVisitor visitor = new JustGiveMeTheStringVisitor();
        text.accept((class_5224)visitor);
        return visitor.toString();
    }

    public static final String wrapText(String text, int width) {
        return ChatUtils.wrapText(text, width, class_2583.field_24360);
    }

    public static final String wrapText(String text, int width, class_2583 style) {
        List lines = ChatUtils.MC.field_1772.method_27527().method_27498(text, width, class_2583.field_24360);
        StringJoiner joiner = new StringJoiner("\n");
        lines.stream().map(class_5348::getString).forEach(s -> joiner.add((CharSequence)s));
        return joiner.toString();
    }

    private static /* synthetic */ ChatUtils[] $values() {
        return new ChatUtils[0];
    }

    static {
        $VALUES = ChatUtils.$values();
        MC = WurstClient.MC;
        enabled = true;
    }
}
