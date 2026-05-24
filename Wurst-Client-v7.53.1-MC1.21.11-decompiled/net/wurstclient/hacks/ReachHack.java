package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"range"})
public final class ReachHack
extends Hack {
    private final SliderSetting range = new SliderSetting("Range", 6.0, 1.0, 10.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);

    public ReachHack() {
        super("Reach");
        this.setCategory(Category.OTHER);
        this.addSetting(this.range);
    }

    public double getReachDistance() {
        return this.range.getValue();
    }
}
