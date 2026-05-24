package net.wurstclient.hacks.autocomplete;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import net.wurstclient.hacks.autocomplete.MessageCompleter;
import net.wurstclient.hacks.autocomplete.ModelSettings;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;

public final class OpenAiMessageCompleter
extends MessageCompleter {
    public OpenAiMessageCompleter(ModelSettings modelSettings) {
        super(modelSettings);
    }

    @Override
    protected JsonObject buildParams(String prompt, int maxSuggestions) {
        JsonObject params = new JsonObject();
        params.addProperty("stop", this.modelSettings.stopSequence.getSelected().getSequence());
        params.addProperty("max_tokens", this.modelSettings.maxTokens.getValueI());
        params.addProperty("temperature", this.modelSettings.temperature.getValue());
        params.addProperty("top_p", this.modelSettings.topP.getValue());
        params.addProperty("presence_penalty", this.modelSettings.presencePenalty.getValue());
        params.addProperty("frequency_penalty", this.modelSettings.frequencyPenalty.getValue());
        params.addProperty("n", maxSuggestions);
        boolean customModel = !this.modelSettings.customModel.getValue().isBlank();
        String modelName = customModel ? this.modelSettings.customModel.getValue() : String.valueOf((Object)this.modelSettings.openAiModel.getSelected());
        boolean chatModel = customModel ? this.modelSettings.customModelType.getSelected().isChat() : this.modelSettings.openAiModel.getSelected().isChatModel();
        params.addProperty("model", modelName);
        if (chatModel) {
            JsonArray messages = new JsonArray();
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "Complete the following text. Reply only with the completion. You are not an assistant.");
            messages.add(systemMessage);
            JsonObject promptMessage = new JsonObject();
            promptMessage.addProperty("role", "user");
            promptMessage.addProperty("content", prompt);
            messages.add(promptMessage);
            params.add("messages", messages);
        } else {
            params.addProperty("prompt", prompt);
        }
        return params;
    }

    @Override
    protected WsonObject requestCompletions(JsonObject parameters) throws IOException, JsonException {
        URL url = URI.create(this.modelSettings.openAiModel.getSelected().isChatModel() ? this.modelSettings.openaiChatEndpoint.getValue() : this.modelSettings.openaiLegacyEndpoint.getValue()).toURL();
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + System.getenv("WURST_OPENAI_KEY"));
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream();){
            os.write(JsonUtils.GSON.toJson(parameters).getBytes());
            os.flush();
        }
        return JsonUtils.parseConnectionToObject(conn);
    }

    @Override
    protected String[] extractCompletions(WsonObject response) throws JsonException {
        ArrayList<String> completions = new ArrayList<String>();
        ArrayList<WsonObject> choices = response.getArray("choices").getAllObjects();
        if (this.modelSettings.openAiModel.getSelected().isChatModel()) {
            for (WsonObject choice : choices) {
                WsonObject message = choice.getObject("message");
                String content = message.getString("content");
                completions.add(content);
            }
        } else {
            for (WsonObject choice : choices) {
                completions.add(choice.getString("text"));
            }
        }
        for (String completion : completions) {
            completion = completion.replace("\n", " ");
        }
        return completions.toArray(new String[completions.size()]);
    }
}
