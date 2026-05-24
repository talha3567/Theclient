package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"boat fly", "BoatFlight", "boat flight", "EntitySpeed", "entity speed"})
public final class BoatFlyHack
extends Hack
implements UpdateListener {
    private final CheckboxSetting changeForwardSpeed = new CheckboxSetting("Change Forward Speed", "Allows \u00a7eForward Speed\u00a7r to be changed, disables smooth acceleration.", false);
    private final SliderSetting forwardSpeed = new SliderSetting("Forward Speed", 1.0, 0.05, 5.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final SliderSetting upwardSpeed = new SliderSetting("Upward Speed", 0.3, 0.0, 5.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);

    public BoatFlyHack() {
        super("BoatFly");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.changeForwardSpeed);
        this.addSetting(this.forwardSpeed);
        this.addSetting(this.upwardSpeed);
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
        if (!BoatFlyHack.MC.field_1724.method_5765()) {
            return;
        }
        class_1297 vehicle = BoatFlyHack.MC.field_1724.method_5854();
        class_243 velocity = vehicle.method_18798();
        double motionX = velocity.field_1352;
        double motionY = 0.0;
        double motionZ = velocity.field_1350;
        if (BoatFlyHack.MC.field_1690.field_1903.method_1434()) {
            motionY = this.upwardSpeed.getValue();
        } else if (BoatFlyHack.MC.field_1690.field_1867.method_1434()) {
            motionY = velocity.field_1351;
        }
        if (BoatFlyHack.MC.field_1690.field_1894.method_1434() && this.changeForwardSpeed.isChecked()) {
            double speed = this.forwardSpeed.getValue();
            float yawRad = vehicle.method_36454() * ((float)Math.PI / 180);
            motionX = (double)class_3532.method_15374((double)(-yawRad)) * speed;
            motionZ = (double)class_3532.method_15362((double)yawRad) * speed;
        }
        vehicle.method_18800(motionX, motionY, motionZ);
    }
}
