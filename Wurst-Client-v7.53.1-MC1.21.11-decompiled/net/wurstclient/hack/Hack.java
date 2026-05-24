package net.wurstclient.hack;

import java.util.Objects;
import net.wurstclient.Category;
import net.wurstclient.Feature;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hacks.ClickGuiHack;
import net.wurstclient.hacks.NavigatorHack;
import net.wurstclient.hacks.TooManyHaxHack;

public abstract class Hack
extends Feature {
    private final String name;
    private final String description;
    private Category category;
    private boolean enabled;
    private final boolean stateSaved = !this.getClass().isAnnotationPresent(DontSaveState.class);

    public Hack(String name) {
        this.name = Objects.requireNonNull(name);
        this.description = "description.wurst.hack." + name.toLowerCase();
        this.addPossibleKeybind(name, "Toggle " + name);
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Feature name must not contain spaces: " + name);
        }
    }

    @Override
    public final String getName() {
        return this.name;
    }

    public String getRenderName() {
        return this.name;
    }

    @Override
    public final String getDescription() {
        return WURST.translate(this.description, new Object[0]);
    }

    public final String getDescriptionKey() {
        return this.description;
    }

    @Override
    public final Category getCategory() {
        return this.category;
    }

    protected final void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public final boolean isEnabled() {
        return this.enabled;
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        TooManyHaxHack tooManyHax = Hack.WURST.getHax().tooManyHaxHack;
        if (enabled && tooManyHax.isEnabled() && tooManyHax.isBlocked(this)) {
            return;
        }
        this.enabled = enabled;
        if (!(this instanceof NavigatorHack) && !(this instanceof ClickGuiHack)) {
            WURST.getHud().getHackList().updateState(this);
        }
        if (enabled) {
            this.onEnable();
        } else {
            this.onDisable();
        }
        if (this.stateSaved) {
            WURST.getHax().saveEnabledHax();
        }
    }

    @Override
    public final String getPrimaryAction() {
        return this.enabled ? "Disable" : "Enable";
    }

    @Override
    public final void doPrimaryAction() {
        this.setEnabled(!this.enabled);
    }

    public final boolean isStateSaved() {
        return this.stateSaved;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}
