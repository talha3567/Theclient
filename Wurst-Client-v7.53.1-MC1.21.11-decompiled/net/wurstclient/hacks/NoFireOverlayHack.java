package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"no fire overlay"})
public final class NoFireOverlayHack
extends Hack {
    private final SliderSetting offset = new SliderSetting("Offset", "The amount to lower the fire overlay by.", 0.6, 0.01, 0.6, 0.01, SliderSetting.ValueDisplay.DECIMAL);

    public NoFireOverlayHack() {
        super("NoFireOverlay");
        this.setCategory(Category.RENDER);
        this.addSetting(this.offset);
    }

    public float getOverlayOffset() {
        return this.isEnabled() ? this.offset.getValueF() : 0.0f;
    }
}
