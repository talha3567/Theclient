package net.wurstclient.hacks;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.function.BiConsumer;
import net.minecraft.class_408;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ChatOutputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.autocomplete.MessageCompleter;
import net.wurstclient.hacks.autocomplete.ModelSettings;
import net.wurstclient.hacks.autocomplete.OpenAiMessageCompleter;
import net.wurstclient.hacks.autocomplete.SuggestionHandler;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.ChatUtils;

@SearchTags(value={"auto complete", "Copilot", "ChatGPT", "chat GPT", "GPT-3", "GPT3", "GPT 3", "OpenAI", "open ai", "ChatAI", "chat AI", "ChatBot", "chat bot"})
public final class AutoCompleteHack
extends Hack
implements ChatOutputListener,
UpdateListener {
    private final ModelSettings modelSettings = new ModelSettings();
    private final SuggestionHandler suggestionHandler = new SuggestionHandler();
    private MessageCompleter completer;
    private String draftMessage;
    private BiConsumer<SuggestionsBuilder, String> suggestionsUpdater;
    private Thread apiCallThread;
    private long lastApiCallTime;
    private long lastRefreshTime;

    public AutoCompleteHack() {
        super("AutoComplete");
        this.setCategory(Category.CHAT);
        this.modelSettings.forEach(x$0 -> this.addSetting((Setting)x$0));
        this.suggestionHandler.getSettings().forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        this.completer = new OpenAiMessageCompleter(this.modelSettings);
        if (this.completer instanceof OpenAiMessageCompleter && System.getenv("WURST_OPENAI_KEY") == null) {
            ChatUtils.error("API key not found. Please set the WURST_OPENAI_KEY environment variable and reboot.");
            this.setEnabled(false);
            return;
        }
        EVENTS.add(ChatOutputListener.class, this);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(ChatOutputListener.class, this);
        EVENTS.remove(UpdateListener.class, this);
        this.suggestionHandler.clearSuggestions();
    }

    @Override
    public void onSentMessage(ChatOutputListener.ChatOutputEvent event) {
        this.suggestionHandler.clearSuggestions();
    }

    @Override
    public void onUpdate() {
        long timeSinceLastRefresh = System.currentTimeMillis() - this.lastRefreshTime;
        if (timeSinceLastRefresh < 300L) {
            return;
        }
        long timeSinceLastApiCall = System.currentTimeMillis() - this.lastApiCallTime;
        if (timeSinceLastApiCall < 3000L) {
            return;
        }
        if (!(AutoCompleteHack.MC.field_1755 instanceof class_408)) {
            return;
        }
        if (this.draftMessage == null || this.suggestionsUpdater == null) {
            return;
        }
        if (this.apiCallThread != null && this.apiCallThread.isAlive()) {
            return;
        }
        int maxSuggestions = this.suggestionHandler.getMaxSuggestionsFor(this.draftMessage);
        if (maxSuggestions < 1) {
            return;
        }
        String draftMessage2 = this.draftMessage;
        BiConsumer<SuggestionsBuilder, String> suggestionsUpdater2 = this.suggestionsUpdater;
        this.apiCallThread = new Thread(() -> {
            String[] suggestions = this.completer.completeChatMessage(draftMessage2, maxSuggestions);
            if (suggestions.length < 1) {
                return;
            }
            for (String suggestion : suggestions) {
                if (suggestion.isEmpty()) continue;
                this.suggestionHandler.addSuggestion(suggestion, draftMessage2, suggestionsUpdater2);
            }
        });
        this.apiCallThread.setName("AutoComplete API Call");
        this.apiCallThread.setPriority(1);
        this.apiCallThread.setDaemon(true);
        this.lastApiCallTime = System.currentTimeMillis();
        this.apiCallThread.start();
    }

    public void onRefresh(String draftMessage, BiConsumer<SuggestionsBuilder, String> suggestionsUpdater) {
        this.suggestionHandler.showSuggestions(draftMessage, suggestionsUpdater);
        this.draftMessage = draftMessage;
        this.suggestionsUpdater = suggestionsUpdater;
        this.lastRefreshTime = System.currentTimeMillis();
    }
}
