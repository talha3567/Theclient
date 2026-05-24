package net.wurstclient.hacks;

import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags(value={"auto sprint"})
public final class AutoSprintHack
extends Hack
implements UpdateListener {
    private final CheckboxSetting allDirections = new CheckboxSetting("Omnidirectional Sprint", "Sprint in all directions, not just forward.", false);
    private final CheckboxSetting hungry = new CheckboxSetting("Hungry Sprint", "Sprint even on low hunger.", false);

    public AutoSprintHack() {
        super("AutoSprint");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.allDirections);
        this.addSetting(this.hungry);
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
        class_746 player = AutoSprintHack.MC.field_1724;
        if (player.field_5976 || player.method_5715()) {
            return;
        }
        if (player.method_5799() || player.method_5869()) {
            return;
        }
        if (!this.allDirections.isChecked() && player.field_6250 <= 0.0f) {
            return;
        }
        if (player.field_3913.method_3128().method_35584() <= 1.0E-5f) {
            return;
        }
        player.method_5728(true);
    }

    public boolean shouldOmniSprint() {
        return this.isEnabled() && this.allDirections.isChecked();
    }

    public boolean shouldSprintHungry() {
        return this.isEnabled() && this.hungry.isChecked();
    }
}
