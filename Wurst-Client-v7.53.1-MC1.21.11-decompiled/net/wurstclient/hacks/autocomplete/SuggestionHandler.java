package net.wurstclient.hacks.autocomplete;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.class_3532;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;

public final class SuggestionHandler {
    private final ArrayList<String> suggestions = new ArrayList();
    private final SliderSetting maxSuggestionsPerDraft = new SliderSetting("Max suggestions per draft", "How many suggestions the AI is allowed to generate for the same draft message.", 3.0, 1.0, 10.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final SliderSetting maxSuggestionsKept = new SliderSetting("Max suggestions kept", "Maximum number of suggestions kept in memory.", 100.0, 10.0, 1000.0, 10.0, SliderSetting.ValueDisplay.INTEGER);
    private final SliderSetting maxSuggestionsShown = new SliderSetting("Max suggestions shown", "How many suggestions can be shown above the chat box.\n\nIf this is set too high, the suggestions will obscure some of the existing chat messages. How high you can set this depends on your screen resolution and GUI scale.", 5.0, 1.0, 10.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final List<Setting> settings = Arrays.asList(this.maxSuggestionsPerDraft, this.maxSuggestionsKept, this.maxSuggestionsShown);

    public List<Setting> getSettings() {
        return this.settings;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getMaxSuggestionsFor(String draftMessage) {
        ArrayList<String> arrayList = this.suggestions;
        synchronized (arrayList) {
            int existing = (int)this.suggestions.stream().map(String::toLowerCase).filter(s -> s.startsWith(draftMessage.toLowerCase())).count();
            int maxPerDraft = this.maxSuggestionsPerDraft.getValueI();
            return class_3532.method_15340((int)(maxPerDraft - existing), (int)0, (int)maxPerDraft);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addSuggestion(String suggestion, String draftMessage, BiConsumer<SuggestionsBuilder, String> suggestionsUpdater) {
        ArrayList<String> arrayList = this.suggestions;
        synchronized (arrayList) {
            String completedMessage = draftMessage + suggestion;
            if (!this.suggestions.contains(completedMessage)) {
                this.suggestions.add(completedMessage);
                if ((double)this.suggestions.size() > this.maxSuggestionsKept.getValue()) {
                    this.suggestions.remove(0);
                }
            }
            this.showSuggestionsImpl(draftMessage, suggestionsUpdater);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void showSuggestions(String draftMessage, BiConsumer<SuggestionsBuilder, String> suggestionsUpdater) {
        ArrayList<String> arrayList = this.suggestions;
        synchronized (arrayList) {
            this.showSuggestionsImpl(draftMessage, suggestionsUpdater);
        }
    }

    private void showSuggestionsImpl(String draftMessage, BiConsumer<SuggestionsBuilder, String> suggestionsUpdater) {
        SuggestionsBuilder builder = new SuggestionsBuilder(draftMessage, 0);
        String inlineSuggestion = null;
        int shownSuggestions = 0;
        for (int i = this.suggestions.size() - 1; i >= 0; --i) {
            String s = this.suggestions.get(i);
            if (!s.toLowerCase().startsWith(draftMessage.toLowerCase())) continue;
            if ((double)shownSuggestions >= this.maxSuggestionsShown.getValue()) break;
            builder.suggest(s);
            inlineSuggestion = s;
            ++shownSuggestions;
        }
        suggestionsUpdater.accept(builder, inlineSuggestion);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void clearSuggestions() {
        ArrayList<String> arrayList = this.suggestions;
        synchronized (arrayList) {
            this.suggestions.clear();
        }
    }
}
