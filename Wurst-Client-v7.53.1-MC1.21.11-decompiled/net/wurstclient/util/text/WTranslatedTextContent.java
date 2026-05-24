package net.wurstclient.util.text;

import java.util.Map;
import java.util.Objects;
import net.wurstclient.WurstClient;
import net.wurstclient.WurstTranslator;
import net.wurstclient.util.text.WTextContent;

public final class WTranslatedTextContent
implements WTextContent {
    private final String key;
    private final Object[] args;
    private String translation;
    private Map<String, String> lastLanguage;

    public WTranslatedTextContent(String key, Object ... args) {
        this.key = Objects.requireNonNull(key);
        this.args = args;
    }

    private void update() {
        WurstTranslator translator = WurstClient.INSTANCE.getTranslator();
        Map<String, String> language = translator.getWurstsCurrentLanguage();
        if (language == this.lastLanguage) {
            return;
        }
        this.translation = translator.translate(this.key, this.args);
        this.lastLanguage = language;
    }

    public String toString() {
        this.update();
        return this.translation;
    }
}
