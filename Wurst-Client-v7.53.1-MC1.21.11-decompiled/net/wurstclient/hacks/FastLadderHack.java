package net.wurstclient.hacks;

import net.minecraft.class_243;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"FastClimb", "fast ladder", "fast climb"})
public final class FastLadderHack
extends Hack
implements UpdateListener {
    public FastLadderHack() {
        super("FastLadder");
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
        class_746 player = FastLadderHack.MC.field_1724;
        if (!player.method_6101() || !player.field_5976) {
            return;
        }
        if (player.field_3913.method_3128().method_35584() <= 1.0E-5f) {
            return;
        }
        class_243 velocity = player.method_18798();
        player.method_18800(velocity.field_1352, 0.2872, velocity.field_1350);
    }
}
