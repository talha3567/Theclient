package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class KeybindSetting
extends Setting<Keybind> {
    private final Runnable action;
    public WKeybind widget;

    public KeybindSetting(String name, String description, Keybind defaultValue, Consumer<Keybind> onChanged, Consumer<Setting<Keybind>> onModuleActivated, IVisible visible, Runnable action) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.action = action;
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority=200)
    private void onKeyBinding(KeyInputEvent event) {
        if (this.widget == null) {
            return;
        }
        if (event.action == KeyAction.Press && event.key() == 256 && this.widget.onClear()) {
            event.cancel();
        } else if (event.action == KeyAction.Release && this.widget.onAction(true, event.key(), event.modifiers())) {
            event.cancel();
        }
    }

    @EventHandler(priority=200)
    private void onMouseClickBinding(MouseClickEvent event) {
        if (event.action == KeyAction.Press && this.widget != null && this.widget.onAction(false, event.button(), 0)) {
            event.cancel();
        }
    }

    @EventHandler(priority=100)
    private void onKey(KeyInputEvent event) {
        if (event.action == KeyAction.Release && ((Keybind)this.get()).matches(event.input) && (this.module == null || this.module.isActive()) && this.action != null) {
            this.action.run();
        }
    }

    @EventHandler(priority=100)
    private void onMouseClick(MouseClickEvent event) {
        if (event.action == KeyAction.Release && ((Keybind)this.get()).matches(event.input) && (this.module == null || this.module.isActive()) && this.action != null) {
            this.action.run();
        }
    }

    @Override
    public void resetImpl() {
        if (this.value == null) {
            this.value = ((Keybind)this.defaultValue).copy();
        } else {
            ((Keybind)this.value).set((Keybind)this.defaultValue);
        }
        if (this.widget != null) {
            this.widget.reset();
        }
    }

    @Override
    protected Keybind parseImpl(String str) {
        try {
            return Keybind.fromKey(Integer.parseInt(str.trim()));
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    @Override
    protected boolean isValueValid(Keybind value) {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("value", (Tag)((Keybind)this.get()).toTag());
        return tag;
    }

    @Override
    public Keybind load(CompoundTag tag) {
        ((Keybind)this.get()).fromTag(tag.getCompoundOrEmpty("value"));
        return (Keybind)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Keybind, KeybindSetting> {
        private Runnable action;

        public Builder() {
            super(Keybind.none());
        }

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        @Override
        public KeybindSetting build() {
            return new KeybindSetting(this.name, this.description, (Keybind)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.action);
        }
    }
}
