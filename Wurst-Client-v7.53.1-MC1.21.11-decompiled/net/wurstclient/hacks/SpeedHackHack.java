package net.wurstclient.hacks;

import net.minecraft.class_243;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"speed hack"})
public final class SpeedHackHack
extends Hack
implements UpdateListener {
    public SpeedHackHack() {
        super("SpeedHack");
        this.setCategory(Category.MOVEMENT);
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
        if (SpeedHackHack.MC.field_1724.method_5715() || SpeedHackHack.MC.field_1724.field_6250 == 0.0f && SpeedHackHack.MC.field_1724.field_6212 == 0.0f) {
            return;
        }
        if (SpeedHackHack.MC.field_1724.field_6250 > 0.0f && !SpeedHackHack.MC.field_1724.field_5976) {
            SpeedHackHack.MC.field_1724.method_5728(true);
        }
        if (!SpeedHackHack.MC.field_1724.method_24828()) {
            return;
        }
        class_243 v = SpeedHackHack.MC.field_1724.method_18798();
        SpeedHackHack.MC.field_1724.method_18800(v.field_1352 * 1.8, v.field_1351 + 0.1, v.field_1350 * 1.8);
        v = SpeedHackHack.MC.field_1724.method_18798();
        double currentSpeed = Math.sqrt(Math.pow(v.field_1352, 2.0) + Math.pow(v.field_1350, 2.0));
        double maxSpeed = 0.66f;
        if (currentSpeed > maxSpeed) {
            SpeedHackHack.MC.field_1724.method_18800(v.field_1352 / currentSpeed * maxSpeed, v.field_1351, v.field_1350 / currentSpeed * maxSpeed);
        }
    }
}
