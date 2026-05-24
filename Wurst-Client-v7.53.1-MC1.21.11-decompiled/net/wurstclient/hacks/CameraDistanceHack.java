package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"camera distance", "CamDistance", "cam distance"})
public final class CameraDistanceHack
extends Hack {
    private final SliderSetting distance = new SliderSetting("Distance", 12.0, -0.5, 150.0, 0.5, SliderSetting.ValueDisplay.DECIMAL);

    public CameraDistanceHack() {
        super("CameraDistance");
        this.setCategory(Category.RENDER);
        this.addSetting(this.distance);
    }

    public float getDistance() {
        return this.distance.getValueF();
    }
}
