package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.Rotation;

public final class TiredHack
extends Hack
implements UpdateListener {
    public TiredHack() {
        super("Tired");
        this.setCategory(Category.FUN);
    }

    @Override
    protected void onEnable() {
        TiredHack.WURST.getHax().derpHack.setEnabled(false);
        TiredHack.WURST.getHax().headRollHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        new Rotation(TiredHack.MC.field_1724.method_36454(), TiredHack.MC.field_1724.field_6012 % 100).sendPlayerLookPacket();
    }
}
