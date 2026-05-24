package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"high jump"})
public final class HighJumpHack
extends Hack {
    private final SliderSetting height = new SliderSetting("Height", "Jump height in blocks.\nThis gets very inaccurate at higher values.", 6.0, 1.0, 100.0, 1.0, SliderSetting.ValueDisplay.INTEGER);

    public HighJumpHack() {
        super("HighJump");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.height);
    }

    public float getAdditionalJumpMotion() {
        return this.isEnabled() ? this.height.getValueF() * 0.1f : 0.0f;
    }
}
