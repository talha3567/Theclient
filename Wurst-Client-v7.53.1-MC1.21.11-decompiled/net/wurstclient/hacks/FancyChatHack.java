package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ChatOutputListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"fancy chat"})
public final class FancyChatHack
extends Hack
implements ChatOutputListener {
    private final String blacklist = "(){}[]|";

    public FancyChatHack() {
        super("FancyChat");
        this.setCategory(Category.CHAT);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(ChatOutputListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(ChatOutputListener.class, this);
    }

    @Override
    public void onSentMessage(ChatOutputListener.ChatOutputEvent event) {
        String message = event.getOriginalMessage();
        if (message.startsWith("/") || message.startsWith(".")) {
            return;
        }
        String newMessage = this.convertString(message);
        event.setMessage(newMessage);
    }

    private String convertString(String input) {
        Object output = "";
        for (char c : input.toCharArray()) {
            output = (String)output + this.convertChar(c);
        }
        return output;
    }

    private String convertChar(char c) {
        if (c < '!' || c > '\u0080') {
            return "" + c;
        }
        if ("(){}[]|".contains(Character.toString(c))) {
            return "" + c;
        }
        return new String(Character.toChars(c + 65248));
    }
}
