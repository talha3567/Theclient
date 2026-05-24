package net.wurstclient.hacks;

import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.minecraft.class_327;
import net.minecraft.class_338;
import net.minecraft.class_341;
import net.minecraft.class_3532;
import net.minecraft.class_5250;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.minecraft.class_7417;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.MathUtils;

@SearchTags(value={"NoSpam", "ChatFilter", "anti spam", "no spam", "chat filter"})
public final class AntiSpamHack
extends Hack
implements ChatInputListener {
    public AntiSpamHack() {
        super("AntiSpam");
        this.setCategory(Category.CHAT);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(ChatInputListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(ChatInputListener.class, this);
    }

    @Override
    public void onReceivedMessage(ChatInputListener.ChatInputEvent event) {
        List<class_303.class_7590> chatLines = event.getChatLines();
        if (chatLines.isEmpty()) {
            return;
        }
        int maxTextLength = class_3532.method_15357((double)((double)class_338.method_1806((double)((Double)AntiSpamHack.MC.field_1690.method_42556().method_41753())) / (Double)AntiSpamHack.MC.field_1690.method_42554().method_41753()));
        List newLines = class_341.method_1850((class_5348)event.getComponent(), (int)maxTextLength, (class_327)AntiSpamHack.MC.field_1772);
        int spamCounter = 1;
        int matchingLines = 0;
        for (int i = chatLines.size() - 1; i >= 0; --i) {
            String oldLine = ChatUtils.getAsString(chatLines.get(i));
            if (matchingLines <= newLines.size() - 1) {
                String oldSpamCounter;
                String nextOldLine;
                String twoLines;
                String addedText;
                String newLine = ChatUtils.getAsString((class_5481)newLines.get(matchingLines));
                if (matchingLines < newLines.size() - 1) {
                    if (oldLine.equals(newLine)) {
                        ++matchingLines;
                        continue;
                    }
                    matchingLines = 0;
                    continue;
                }
                if (!oldLine.startsWith(newLine)) {
                    matchingLines = 0;
                    continue;
                }
                if (i > 0 && matchingLines == newLines.size() - 1 && (addedText = (twoLines = oldLine + (nextOldLine = ChatUtils.getAsString(chatLines.get(i - 1)))).substring(newLine.length())).startsWith(" [x") && addedText.endsWith("]") && MathUtils.isInteger(oldSpamCounter = addedText.substring(3, addedText.length() - 1))) {
                    spamCounter += Integer.parseInt(oldSpamCounter);
                    ++matchingLines;
                    continue;
                }
                if (oldLine.length() == newLine.length()) {
                    ++spamCounter;
                } else {
                    String addedText2 = oldLine.substring(newLine.length());
                    if (!addedText2.startsWith(" [x") || !addedText2.endsWith("]")) {
                        matchingLines = 0;
                        continue;
                    }
                    String oldSpamCounter2 = addedText2.substring(3, addedText2.length() - 1);
                    if (!MathUtils.isInteger(oldSpamCounter2)) {
                        matchingLines = 0;
                        continue;
                    }
                    spamCounter += Integer.parseInt(oldSpamCounter2);
                }
            }
            for (int i2 = i + matchingLines; i2 >= i; --i2) {
                chatLines.remove(i2);
            }
            matchingLines = 0;
        }
        if (spamCounter > 1) {
            class_5250 oldText = (class_5250)event.getComponent();
            class_5250 newText = class_5250.method_43477((class_7417)oldText.method_10851());
            newText.method_10862(oldText.method_10866());
            oldText.method_10855().forEach(arg_0 -> ((class_5250)newText).method_10852(arg_0));
            event.setComponent((class_2561)newText.method_27693(" [x" + spamCounter + "]"));
        }
    }
}
