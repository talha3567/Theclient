package net.wurstclient.hacks;

import net.minecraft.class_243;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

public final class SpiderHack
extends Hack
implements UpdateListener {
    public SpiderHack() {
        super("Spider");
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
        class_746 player = SpiderHack.MC.field_1724;
        if (!player.field_5976) {
            return;
        }
        class_243 velocity = player.method_18798();
        if (velocity.field_1351 >= 0.2) {
            return;
        }
        player.method_18800(velocity.field_1352, 0.2, velocity.field_1350);
    }
}
