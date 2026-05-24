package net.wurstclient.hacks;

import net.minecraft.class_243;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

public final class NoWebHack
extends Hack
implements UpdateListener {
    public NoWebHack() {
        super("NoWeb");
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
        NoWebHack.MC.field_1724.field_17046 = class_243.field_1353;
    }
}
