package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.class_304;
import net.minecraft.class_342;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_481;
import net.minecraft.class_490;
import net.minecraft.class_7706;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.clickgui.screens.ClickGuiScreen;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags(value={"inv walk", "inventory walk", "InvMove", "inv move", "inventory move", "MenuWalk", "menu walk"})
public final class InvWalkHack
extends Hack
implements UpdateListener {
    private final CheckboxSetting allowClickGUI = new CheckboxSetting("Allow ClickGUI", "description.wurst.setting.invwalk.allow_clickgui", true);
    private final CheckboxSetting allowOther = new CheckboxSetting("Allow other screens", "description.wurst.setting.invwalk.allow_other", true);
    private final CheckboxSetting allowSneak = new CheckboxSetting("Allow sneak key", true);
    private final CheckboxSetting allowSprint = new CheckboxSetting("Allow sprint key", true);
    private final CheckboxSetting allowJump = new CheckboxSetting("Allow jump key", true);

    public InvWalkHack() {
        super("InvWalk");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.allowClickGUI);
        this.addSetting(this.allowOther);
        this.addSetting(this.allowSneak);
        this.addSetting(this.allowSprint);
        this.addSetting(this.allowJump);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        class_437 screen = InvWalkHack.MC.field_1755;
        if (screen == null) {
            return;
        }
        if (!this.isAllowedScreen(screen)) {
            return;
        }
        ArrayList<class_304> keys = new ArrayList<class_304>(Arrays.asList(InvWalkHack.MC.field_1690.field_1894, InvWalkHack.MC.field_1690.field_1881, InvWalkHack.MC.field_1690.field_1913, InvWalkHack.MC.field_1690.field_1849));
        if (this.allowSneak.isChecked()) {
            keys.add(InvWalkHack.MC.field_1690.field_1832);
        }
        if (this.allowSprint.isChecked()) {
            keys.add(InvWalkHack.MC.field_1690.field_1867);
        }
        if (this.allowJump.isChecked()) {
            keys.add(InvWalkHack.MC.field_1690.field_1903);
        }
        for (class_304 key : keys) {
            IKeyMapping.get(key).resetPressedState();
        }
    }

    private boolean isAllowedScreen(class_437 screen) {
        if ((screen instanceof class_490 || screen instanceof class_481) && !this.isCreativeSearchBarOpen(screen)) {
            return true;
        }
        if (this.allowClickGUI.isChecked() && screen instanceof ClickGuiScreen) {
            return true;
        }
        return this.allowOther.isChecked() && screen instanceof class_465 && !this.hasTextBox(screen);
    }

    private boolean isCreativeSearchBarOpen(class_437 screen) {
        if (!(screen instanceof class_481)) {
            return false;
        }
        return class_481.field_2896 == class_7706.method_47344();
    }

    private boolean hasTextBox(class_437 screen) {
        return screen.method_25396().stream().anyMatch(class_342.class::isInstance);
    }
}
