package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.settings.IGeneric;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class GenericSetting<T extends IGeneric<T>>
extends Setting<T> {
    public GenericSetting(String name, String description, T defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    public WidgetScreen createScreen(GuiTheme theme) {
        return ((IGeneric)this.get()).createScreen(theme, this);
    }

    @Override
    public void resetImpl() {
        if (this.value == null) {
            this.value = ((IGeneric)this.defaultValue).copy();
        }
        ((IGeneric)this.value).set((IGeneric)this.defaultValue);
    }

    @Override
    protected T parseImpl(String str) {
        return (T)((IGeneric)((IGeneric)this.defaultValue).copy());
    }

    @Override
    protected boolean isValueValid(T value) {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("value", (Tag)((IGeneric)this.get()).toTag());
        return tag;
    }

    @Override
    public T load(CompoundTag tag) {
        ((IGeneric)this.get()).fromTag(tag.getCompoundOrEmpty("value"));
        return (T)((IGeneric)this.get());
    }

    public static class Builder<T extends IGeneric<T>>
    extends Setting.SettingBuilder<Builder<T>, T, GenericSetting<T>> {
        public Builder() {
            super(null);
        }

        @Override
        public GenericSetting<T> build() {
            return new GenericSetting<IGeneric>(this.name, this.description, (IGeneric)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
