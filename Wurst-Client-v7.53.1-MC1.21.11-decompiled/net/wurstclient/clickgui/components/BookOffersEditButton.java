package net.wurstclient.clickgui.components;

import java.util.Objects;
import net.minecraft.class_437;
import net.wurstclient.clickgui.components.AbstractListEditButton;
import net.wurstclient.clickgui.screens.EditBookOffersScreen;
import net.wurstclient.settings.BookOffersSetting;
import net.wurstclient.settings.Setting;

public final class BookOffersEditButton
extends AbstractListEditButton {
    private final BookOffersSetting setting;

    public BookOffersEditButton(BookOffersSetting setting) {
        this.setting = Objects.requireNonNull(setting);
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
    }

    @Override
    protected void openScreen() {
        MC.method_1507((class_437)new EditBookOffersScreen(BookOffersEditButton.MC.field_1755, this.setting));
    }

    @Override
    protected String getText() {
        return this.setting.getName() + ": " + this.setting.getOffers().size();
    }

    @Override
    protected Setting getSetting() {
        return this.setting;
    }
}
