package net.wurstclient.hacks;

import net.minecraft.class_3532;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.Rotation;

@SearchTags(value={"head roll", "nodding", "yes"})
public final class HeadRollHack
extends Hack
implements UpdateListener {
    public HeadRollHack() {
        super("HeadRoll");
        this.setCategory(Category.FUN);
    }

    @Override
    protected void onEnable() {
        HeadRollHack.WURST.getHax().derpHack.setEnabled(false);
        HeadRollHack.WURST.getHax().tiredHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        float timer = (float)(HeadRollHack.MC.field_1724.field_6012 % 20) / 10.0f;
        float pitch = class_3532.method_15374((double)(timer * (float)Math.PI)) * 90.0f;
        new Rotation(HeadRollHack.MC.field_1724.method_36454(), pitch).sendPlayerLookPacket();
    }
}
