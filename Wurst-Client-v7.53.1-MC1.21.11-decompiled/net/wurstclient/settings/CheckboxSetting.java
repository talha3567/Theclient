package net.wurstclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.LinkedHashSet;
import java.util.Set;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.CheckboxComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.CheckboxLock;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public class CheckboxSetting
extends Setting
implements CheckboxLock {
    private boolean checked;
    private final boolean checkedByDefault;
    private CheckboxLock lock;

    public CheckboxSetting(String name, WText description, boolean checked) {
        super(name, description);
        this.checked = checked;
        this.checkedByDefault = checked;
    }

    public CheckboxSetting(String name, String descriptionKey, boolean checked) {
        this(name, WText.translated(descriptionKey, new Object[0]), checked);
    }

    public CheckboxSetting(String name, boolean checked) {
        this(name, WText.empty(), checked);
    }

    @Override
    public final boolean isChecked() {
        return this.isLocked() ? this.lock.isChecked() : this.checked;
    }

    public final boolean isCheckedByDefault() {
        return this.checkedByDefault;
    }

    public final void setChecked(boolean checked) {
        if (this.isLocked()) {
            return;
        }
        this.setCheckedIgnoreLock(checked);
    }

    private void setCheckedIgnoreLock(boolean checked) {
        this.checked = checked;
        this.update();
        WurstClient.INSTANCE.saveSettings();
    }

    public final boolean isLocked() {
        return this.lock != null;
    }

    public final void lock(CheckboxLock lock) {
        this.lock = lock;
        this.update();
    }

    public final void unlock() {
        this.lock = null;
        this.update();
    }

    @Override
    public final Component getComponent() {
        return new CheckboxComponent(this);
    }

    @Override
    public final void fromJson(JsonElement json) {
        if (!JsonUtils.isBoolean(json)) {
            return;
        }
        this.setCheckedIgnoreLock(json.getAsBoolean());
    }

    @Override
    public final JsonElement toJson() {
        return new JsonPrimitive(this.checked);
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "Checkbox");
        json.addProperty("checkedByDefault", this.checkedByDefault);
        return json;
    }

    @Override
    public final Set<PossibleKeybind> getPossibleKeybinds(String featureName) {
        String fullName = featureName + " " + this.getName();
        String command = ".setcheckbox " + featureName.toLowerCase() + " ";
        command = command + this.getName().toLowerCase().replace(" ", "_") + " ";
        LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<PossibleKeybind>();
        pkb.add(new PossibleKeybind(command + "toggle", "Toggle " + fullName));
        pkb.add(new PossibleKeybind(command + "on", "Check " + fullName));
        pkb.add(new PossibleKeybind(command + "off", "Uncheck " + fullName));
        return pkb;
    }
}
