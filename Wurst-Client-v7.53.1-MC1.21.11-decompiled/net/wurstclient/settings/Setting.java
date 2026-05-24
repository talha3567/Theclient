package net.wurstclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Objects;
import java.util.Set;
import net.wurstclient.clickgui.Component;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.text.WText;

public abstract class Setting {
    private final String name;
    private final WText description;

    public Setting(String name, WText description) {
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
    }

    public final String getName() {
        return this.name;
    }

    public final String getDescription() {
        return this.description.toString();
    }

    public final String getWrappedDescription(int width) {
        return ChatUtils.wrapText(this.getDescription(), width);
    }

    public abstract Component getComponent();

    public abstract void fromJson(JsonElement var1);

    public abstract JsonElement toJson();

    public abstract JsonObject exportWikiData();

    public void update() {
    }

    public abstract Set<PossibleKeybind> getPossibleKeybinds(String var1);
}
