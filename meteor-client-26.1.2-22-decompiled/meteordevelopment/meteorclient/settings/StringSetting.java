package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.CompoundTag;

public class StringSetting
extends Setting<String> {
    public final String placeholder;
    public final Class<? extends WTextBox.Renderer> renderer;
    public final CharFilter filter;
    public final boolean wide;

    public StringSetting(String name, String description, String defaultValue, Consumer<String> onChanged, Consumer<Setting<String>> onModuleActivated, IVisible visible, String placeholder, Class<? extends WTextBox.Renderer> renderer, CharFilter filter, boolean wide) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.placeholder = placeholder;
        this.renderer = renderer;
        this.filter = filter;
        this.wide = wide;
    }

    @Override
    protected String parseImpl(String str) {
        return str;
    }

    @Override
    protected boolean isValueValid(String value) {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("value", (String)this.get());
        return tag;
    }

    @Override
    public String load(CompoundTag tag) {
        this.set(tag.getStringOr("value", ""));
        return (String)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, String, StringSetting> {
        private String placeholder;
        private Class<? extends WTextBox.Renderer> renderer;
        private CharFilter filter;
        private boolean wide;

        public Builder() {
            super("");
        }

        public Builder placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder renderer(Class<? extends WTextBox.Renderer> renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder filter(CharFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder wide() {
            this.wide = true;
            return this;
        }

        @Override
        public StringSetting build() {
            return new StringSetting(this.name, this.description, (String)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.placeholder, this.renderer, this.filter, this.wide);
        }
    }
}
