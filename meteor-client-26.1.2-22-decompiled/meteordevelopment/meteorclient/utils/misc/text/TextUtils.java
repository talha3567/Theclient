package meteordevelopment.meteorclient.utils.misc.text;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import meteordevelopment.meteorclient.utils.misc.text.ColoredText;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

public class TextUtils {
    private TextUtils() {
    }

    public static List<ColoredText> toColoredTextList(Component text) {
        ArrayDeque<ColoredText> stack = new ArrayDeque<ColoredText>();
        ArrayList<ColoredText> coloredTexts = new ArrayList<ColoredText>();
        TextUtils.preOrderTraverse(text, stack, coloredTexts);
        coloredTexts.removeIf(e -> e.text().isEmpty());
        return coloredTexts;
    }

    public static MutableComponent parseOrderedText(FormattedCharSequence orderedText) {
        MutableComponent parsedText = Component.empty();
        orderedText.accept((n, style, codePoint) -> {
            parsedText.append((Component)Component.literal((String)new String(Character.toChars(codePoint))).setStyle(style));
            return true;
        });
        return parsedText;
    }

    public static Color getMostPopularColor(Component text) {
        Object2IntMap.Entry biggestEntry = null;
        for (Object2IntMap.Entry entry : TextUtils.getColoredCharacterCount(TextUtils.toColoredTextList(text)).object2IntEntrySet()) {
            if (biggestEntry == null) {
                biggestEntry = entry;
                continue;
            }
            if (entry.getIntValue() <= biggestEntry.getIntValue()) continue;
            biggestEntry = entry;
        }
        return biggestEntry == null ? new Color(255, 255, 255) : (Color)biggestEntry.getKey();
    }

    public static Object2IntMap<Color> getColoredCharacterCount(List<ColoredText> coloredTexts) {
        Object2IntOpenHashMap colorCount = new Object2IntOpenHashMap();
        for (ColoredText coloredText : coloredTexts) {
            if (colorCount.containsKey((Object)coloredText.color())) {
                colorCount.put((Object)coloredText.color(), colorCount.getInt((Object)coloredText.color()) + coloredText.text().length());
                continue;
            }
            colorCount.put((Object)coloredText.color(), coloredText.text().length());
        }
        return colorCount;
    }

    private static void preOrderTraverse(Component text, Deque<ColoredText> stack, List<ColoredText> coloredTexts) {
        if (text == null) {
            return;
        }
        String textString = text.getString();
        TextColor mcTextColor = text.getStyle().getColor();
        Color textColor = mcTextColor == null ? (stack.isEmpty() ? new Color(255, 255, 255) : stack.peek().color()) : new Color(text.getStyle().getColor().getValue() | 0xFF000000);
        ColoredText coloredText = new ColoredText(textString, textColor);
        coloredTexts.add(coloredText);
        stack.push(coloredText);
        for (Component child : text.getSiblings()) {
            TextUtils.preOrderTraverse(child, stack, coloredTexts);
        }
        stack.pop();
    }
}
