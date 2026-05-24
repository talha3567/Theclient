package meteordevelopment.meteorclient.utils.player;

import com.mojang.brigadier.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChatUtils {
    private static final List<Tuple<String, Supplier<Component>>> customPrefixes = new ArrayList<Tuple<String, Supplier<Component>>>();
    private static String forcedPrefixClassName;
    private static Component PREFIX;

    private ChatUtils() {
    }

    @PostInit
    public static void init() {
        PREFIX = Component.empty().setStyle(Style.EMPTY.applyFormats(new ChatFormatting[]{ChatFormatting.GRAY})).append("[").append((Component)Component.literal((String)"Meteor").setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)MeteorClient.ADDON.color.getPacked())))).append("] ");
    }

    public static Component getMeteorPrefix() {
        return PREFIX;
    }

    public static void registerCustomPrefix(String packageName, Supplier<Component> supplier) {
        for (Tuple<String, Supplier<Component>> pair : customPrefixes) {
            if (!((String)pair.getA()).equals(packageName)) continue;
            pair.setB(supplier);
            return;
        }
        customPrefixes.add((Tuple<String, Supplier<Component>>)new Tuple((Object)packageName, supplier));
    }

    public static void unregisterCustomPrefix(String packageName) {
        customPrefixes.removeIf(pair -> ((String)pair.getA()).equals(packageName));
    }

    public static void forceNextPrefixClass(Class<?> klass) {
        forcedPrefixClassName = klass.getName();
    }

    public static void sendPlayerMsg(String message) {
        ChatUtils.sendPlayerMsg(message, true);
    }

    public static void sendPlayerMsg(String message, boolean addToHistory) {
        if (addToHistory) {
            MeteorClient.mc.gui.getChat().addRecentChat(message);
        }
        if (message.startsWith("/")) {
            MeteorClient.mc.player.connection.sendCommand(message.substring(1));
        } else {
            MeteorClient.mc.player.connection.sendChat(message);
        }
    }

    public static void info(String message, Object ... args) {
        ChatUtils.sendMsg(ChatFormatting.GRAY, message, args);
    }

    public static void infoPrefix(String prefix, String message, Object ... args) {
        ChatUtils.sendMsg(0, prefix, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GRAY, message, args);
    }

    public static void warning(String message, Object ... args) {
        ChatUtils.sendMsg(ChatFormatting.YELLOW, message, args);
    }

    public static void warningPrefix(String prefix, String message, Object ... args) {
        ChatUtils.sendMsg(0, prefix, ChatFormatting.LIGHT_PURPLE, ChatFormatting.YELLOW, message, args);
    }

    public static void error(String message, Object ... args) {
        ChatUtils.sendMsg(ChatFormatting.RED, message, args);
    }

    public static void errorPrefix(String prefix, String message, Object ... args) {
        ChatUtils.sendMsg(0, prefix, ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED, message, args);
    }

    public static void sendMsg(Component message) {
        ChatUtils.sendMsg(null, message);
    }

    public static void sendMsg(String prefix, Component message) {
        ChatUtils.sendMsg(0, prefix, ChatFormatting.LIGHT_PURPLE, message);
    }

    public static void sendMsg(ChatFormatting color, String message, Object ... args) {
        ChatUtils.sendMsg(0, null, null, color, message, args);
    }

    public static void sendMsg(int id, ChatFormatting color, String message, Object ... args) {
        ChatUtils.sendMsg(id, null, null, color, message, args);
    }

    public static void sendMsg(int id, @Nullable String prefixTitle, @Nullable ChatFormatting prefixColor, ChatFormatting messageColor, String messageContent, Object ... args) {
        MutableComponent message = ChatUtils.formatMsg(String.format(messageContent, args), messageColor);
        ChatUtils.sendMsg(id, prefixTitle, prefixColor, (Component)message);
    }

    public static void sendMsg(int id, @Nullable String prefixTitle, @Nullable ChatFormatting prefixColor, String messageContent, ChatFormatting messageColor) {
        MutableComponent message = ChatUtils.formatMsg(messageContent, messageColor);
        ChatUtils.sendMsg(id, prefixTitle, prefixColor, (Component)message);
    }

    public static void sendMsg(int id, @Nullable String prefixTitle, @Nullable ChatFormatting prefixColor, Component msg) {
        if (MeteorClient.mc.level == null) {
            return;
        }
        MutableComponent message = Component.empty();
        message.append(ChatUtils.getPrefix());
        if (prefixTitle != null) {
            message.append((Component)ChatUtils.getCustomPrefix(prefixTitle, prefixColor));
        }
        message.append(msg);
        if (!Config.get().deleteChatFeedback.get().booleanValue()) {
            id = 0;
        }
        int finalId = id;
        MeteorClient.mc.execute(() -> ((IChatHud)MeteorClient.mc.gui.getChat()).meteor$add((Component)message, finalId));
    }

    private static MutableComponent getCustomPrefix(String prefixTitle, ChatFormatting prefixColor) {
        MutableComponent prefix = Component.empty();
        prefix.setStyle(prefix.getStyle().applyFormats(new ChatFormatting[]{ChatFormatting.GRAY}));
        prefix.append("[");
        MutableComponent moduleTitle = Component.literal((String)prefixTitle);
        moduleTitle.setStyle(moduleTitle.getStyle().applyFormats(new ChatFormatting[]{prefixColor}));
        prefix.append((Component)moduleTitle);
        prefix.append("] ");
        return prefix;
    }

    private static Component getPrefix() {
        if (customPrefixes.isEmpty()) {
            forcedPrefixClassName = null;
            return PREFIX;
        }
        boolean foundChatUtils = false;
        String className = null;
        if (forcedPrefixClassName != null) {
            className = forcedPrefixClassName;
            forcedPrefixClassName = null;
        } else {
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                if (foundChatUtils) {
                    if (element.getClassName().equals(ChatUtils.class.getName())) continue;
                    className = element.getClassName();
                    break;
                }
                if (!element.getClassName().equals(ChatUtils.class.getName())) continue;
                foundChatUtils = true;
            }
        }
        if (className == null) {
            return PREFIX;
        }
        for (Tuple tuple : customPrefixes) {
            if (!className.startsWith((String)tuple.getA())) continue;
            Component prefix = (Component)((Supplier)tuple.getB()).get();
            return prefix != null ? prefix : PREFIX;
        }
        return PREFIX;
    }

    private static MutableComponent formatMsg(String message, ChatFormatting defaultColor) {
        StringReader reader = new StringReader(message);
        MutableComponent text = Component.empty();
        Style style = Style.EMPTY.applyFormats(new ChatFormatting[]{defaultColor});
        StringBuilder result = new StringBuilder();
        boolean formatting = false;
        while (reader.canRead()) {
            char c = reader.read();
            if (c == '(') {
                text.append((Component)Component.literal((String)result.toString()).setStyle(style));
                result.setLength(0);
                result.append(c);
                formatting = true;
                continue;
            }
            result.append(c);
            if (!formatting || c != ')') continue;
            switch (result.toString()) {
                case "(default)": {
                    style = style.applyFormats(new ChatFormatting[]{defaultColor});
                    result.setLength(0);
                    break;
                }
                case "(highlight)": {
                    style = style.applyFormats(new ChatFormatting[]{ChatFormatting.WHITE});
                    result.setLength(0);
                    break;
                }
                case "(underline)": {
                    style = style.applyFormats(new ChatFormatting[]{ChatFormatting.UNDERLINE});
                    result.setLength(0);
                    break;
                }
                case "(bold)": {
                    style = style.applyFormats(new ChatFormatting[]{ChatFormatting.BOLD});
                    result.setLength(0);
                }
            }
            formatting = false;
        }
        if (!result.isEmpty()) {
            text.append((Component)Component.literal((String)result.toString()).setStyle(style));
        }
        return text;
    }

    public static MutableComponent formatCoords(Vec3 pos) {
        String coordsString = String.format("(highlight)(underline)%.0f, %.0f, %.0f(default)", pos.x, pos.y, pos.z);
        MutableComponent coordsText = ChatUtils.formatMsg(coordsString, ChatFormatting.GRAY);
        if (BaritoneUtils.IS_AVAILABLE) {
            Style style = coordsText.getStyle().applyFormats(new ChatFormatting[]{ChatFormatting.BOLD}).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)"Set as Baritone goal"))).withClickEvent((ClickEvent)new MeteorClickEvent(String.format("%sgoto %d %d %d", BaritoneUtils.getPrefix(), (int)pos.x, (int)pos.y, (int)pos.z)));
            coordsText.setStyle(style);
        }
        return coordsText;
    }
}
