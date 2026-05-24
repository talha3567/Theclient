package net.wurstclient.hacks;

import net.minecraft.class_243;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"AutoSwim", "auto swim"})
public final class FishHack
extends Hack
implements UpdateListener {
    public FishHack() {
        super("Fish");
        this.setCategory(Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        FishHack.WURST.getHax().dolphinHack.setEnabled(false);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        class_746 player = FishHack.MC.field_1724;
        if (!player.method_5799() || player.method_5715()) {
            return;
        }
        class_243 velocity = player.method_18798();
        player.method_18800(velocity.field_1352, velocity.field_1351 + 0.005, velocity.field_1350);
    }
}
