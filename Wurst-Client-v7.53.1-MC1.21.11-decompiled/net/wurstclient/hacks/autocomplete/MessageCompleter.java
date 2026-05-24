package net.wurstclient.hacks.autocomplete;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
import net.minecraft.class_303;
import net.minecraft.class_310;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.autocomplete.ModelSettings;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.WsonObject;

public abstract class MessageCompleter {
    protected static final class_310 MC = WurstClient.MC;
    protected final ModelSettings modelSettings;

    public MessageCompleter(ModelSettings modelSettings) {
        this.modelSettings = modelSettings;
    }

    public final String[] completeChatMessage(String draftMessage, int maxSuggestions) {
        String prompt = this.buildPrompt(draftMessage);
        JsonObject params = this.buildParams(prompt, maxSuggestions);
        System.out.println(params);
        try {
            WsonObject response = this.requestCompletions(params);
            System.out.println(response);
            return this.extractCompletions(response);
        }
        catch (IOException | JsonException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    protected String buildPrompt(String draftMessage) {
        Object prompt = "=== Minecraft chat log ===\n";
        List chatHistory = MessageCompleter.MC.field_1705.method_1743().field_2064;
        int messages = 0;
        for (int i = chatHistory.size() - 1; i >= 0; --i) {
            Object message = ChatUtils.getAsString((class_303.class_7590)chatHistory.get(i));
            if (((String)message).startsWith("\u00a7c[\u00a76Wurst\u00a7c]\u00a7r ")) continue;
            if (!((String)message).startsWith("<")) {
                if (this.modelSettings.filterServerMessages.isChecked()) continue;
                message = "<System> " + (String)message;
            }
            if (messages >= this.modelSettings.contextLength.getValueI()) break;
            prompt = (String)prompt + (String)message + "\n";
            ++messages;
        }
        if (chatHistory.isEmpty()) {
            prompt = (String)prompt + "<System> " + MC.method_1548().method_1676() + " joined the game.\n";
        }
        prompt = (String)prompt + "<" + MC.method_1548().method_1676() + "> " + draftMessage;
        return prompt;
    }

    protected abstract JsonObject buildParams(String var1, int var2);

    protected abstract WsonObject requestCompletions(JsonObject var1) throws IOException, JsonException;

    protected abstract String[] extractCompletions(WsonObject var1) throws JsonException;
}
