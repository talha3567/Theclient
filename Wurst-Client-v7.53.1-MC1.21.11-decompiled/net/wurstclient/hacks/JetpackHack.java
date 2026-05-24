package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"jet pack", "AirJump", "air jump"})
public final class JetpackHack
extends Hack
implements UpdateListener {
    public JetpackHack() {
        super("Jetpack");
        this.setCategory(Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        JetpackHack.WURST.getHax().creativeFlightHack.setEnabled(false);
        JetpackHack.WURST.getHax().flightHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (JetpackHack.MC.field_1690.field_1903.method_1434()) {
            JetpackHack.MC.field_1724.method_6043();
        }
    }
}
