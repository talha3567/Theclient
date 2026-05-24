package net.wurstclient.clickgui.components;

import java.util.Objects;
import net.minecraft.class_437;
import net.wurstclient.clickgui.components.AbstractListEditButton;
import net.wurstclient.clickgui.screens.EditItemListScreen;
import net.wurstclient.settings.ItemListSetting;
import net.wurstclient.settings.Setting;

public final class ItemListEditButton
extends AbstractListEditButton {
    private final ItemListSetting setting;

    public ItemListEditButton(ItemListSetting setting) {
        this.setting = Objects.requireNonNull(setting);
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
    }

    @Override
    protected void openScreen() {
        MC.method_1507((class_437)new EditItemListScreen(ItemListEditButton.MC.field_1755, this.setting));
    }

    @Override
    protected String getText() {
        return this.setting.getName() + ": " + this.setting.getItemNames().size();
    }

    @Override
    protected Setting getSetting() {
        return this.setting;
    }
}
