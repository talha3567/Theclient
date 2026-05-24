package net.wurstclient;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_310;
import net.wurstclient.Category;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.mixinterface.IMinecraftClient;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.ChatUtils;

public abstract class Feature {
    protected static final WurstClient WURST = WurstClient.INSTANCE;
    protected static final EventManager EVENTS = WURST.getEventManager();
    protected static final class_310 MC = WurstClient.MC;
    protected static final IMinecraftClient IMC = WurstClient.IMC;
    private final LinkedHashMap<String, Setting> settings = new LinkedHashMap();
    private final LinkedHashSet<PossibleKeybind> possibleKeybinds = new LinkedHashSet();
    private final String searchTags = this.getClass().isAnnotationPresent(SearchTags.class) ? String.join((CharSequence)"\u00a7", this.getClass().getAnnotation(SearchTags.class).value()) : "";
    private final boolean safeToBlock = !this.getClass().isAnnotationPresent(DontBlock.class);

    public abstract String getName();

    public abstract String getDescription();

    public String getWrappedDescription(int width) {
        return ChatUtils.wrapText(this.getDescription(), width);
    }

    public Category getCategory() {
        return null;
    }

    public abstract String getPrimaryAction();

    public void doPrimaryAction() {
    }

    public boolean isEnabled() {
        return false;
    }

    public final Map<String, Setting> getSettings() {
        return Collections.unmodifiableMap(this.settings);
    }

    protected final void addSetting(Setting setting) {
        String key = setting.getName().toLowerCase();
        if (this.settings.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate setting: " + this.getName() + " " + key);
        }
        this.settings.put(key, setting);
        this.possibleKeybinds.addAll(setting.getPossibleKeybinds(this.getName()));
    }

    protected final void addPossibleKeybind(String command, String description) {
        this.possibleKeybinds.add(new PossibleKeybind(command, description));
    }

    public final Set<PossibleKeybind> getPossibleKeybinds() {
        return Collections.unmodifiableSet(this.possibleKeybinds);
    }

    public final String getSearchTags() {
        return this.searchTags;
    }

    public final boolean isSafeToBlock() {
        return this.safeToBlock;
    }
}
