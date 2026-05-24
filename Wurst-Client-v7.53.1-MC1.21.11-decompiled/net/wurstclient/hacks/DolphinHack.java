package net.wurstclient.hacks;

import net.minecraft.class_243;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"AutoSwim", "auto swim"})
public final class DolphinHack
extends Hack
implements UpdateListener {
    public DolphinHack() {
        super("Dolphin");
        this.setCategory(Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        DolphinHack.WURST.getHax().fishHack.setEnabled(false);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        class_746 player = DolphinHack.MC.field_1724;
        if (!player.method_5799() || player.method_5715()) {
            return;
        }
        class_243 velocity = player.method_18798();
        player.method_18800(velocity.field_1352, velocity.field_1351 + 0.04, velocity.field_1350);
    }
}
