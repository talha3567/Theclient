package net.wurstclient.hacks;

import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"auto swim"})
public final class AutoSwimHack
extends Hack
implements UpdateListener {
    public AutoSwimHack() {
        super("AutoSwim");
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
        class_746 player = AutoSwimHack.MC.field_1724;
        if (player.field_5976 || player.method_5715()) {
            return;
        }
        if (!player.method_5799()) {
            return;
        }
        if (player.field_6250 > 0.0f) {
            player.method_5728(true);
        }
    }
}
