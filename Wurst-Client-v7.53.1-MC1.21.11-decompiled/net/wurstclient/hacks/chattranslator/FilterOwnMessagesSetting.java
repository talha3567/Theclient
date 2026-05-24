package net.wurstclient.hacks.chattranslator;

import java.util.regex.Pattern;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.CheckboxSetting;

public class FilterOwnMessagesSetting
extends CheckboxSetting {
    private Pattern ownMessagePattern;
    private String lastUsername;

    public FilterOwnMessagesSetting() {
        super("Filter own messages", "description.wurst.setting.chattranslator.filter_own_messages", true);
    }

    public boolean isOwnMessage(String message) {
        this.updateOwnMessagePattern();
        return this.ownMessagePattern.matcher(message).find();
    }

    private void updateOwnMessagePattern() {
        String username = WurstClient.MC.method_1548().method_1676();
        if (username.equals(this.lastUsername)) {
            return;
        }
        String rankPattern = "(?:\\[[^\\]]+\\] ?){0,2}";
        String namePattern = Pattern.quote(username);
        String regex = "^" + rankPattern + "[<\\[]?" + namePattern + "[>\\]:]";
        this.ownMessagePattern = Pattern.compile(regex);
        this.lastUsername = username;
    }
}
