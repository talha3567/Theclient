package net.wurstclient.hacks;

import java.util.Random;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.Rotation;

@SearchTags(value={"Retarded"})
public final class DerpHack
extends Hack
implements UpdateListener {
    private final Random random = new Random();

    public DerpHack() {
        super("Derp");
        this.setCategory(Category.FUN);
    }

    @Override
    protected void onEnable() {
        DerpHack.WURST.getHax().headRollHack.setEnabled(false);
        DerpHack.WURST.getHax().tiredHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        float yaw = DerpHack.MC.field_1724.method_36454() + this.random.nextFloat() * 360.0f - 180.0f;
        float pitch = this.random.nextFloat() * 180.0f - 90.0f;
        new Rotation(yaw, pitch).sendPlayerLookPacket();
    }
}
