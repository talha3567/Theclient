package net.wurstclient.hacks;

import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.AirStrafingSpeedListener;
import net.wurstclient.events.IsNormalCubeListener;
import net.wurstclient.events.PlayerMoveListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.events.VisGraphListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"no clip"})
public final class NoClipHack
extends Hack
implements UpdateListener,
PlayerMoveListener,
IsNormalCubeListener,
VisGraphListener,
AirStrafingSpeedListener {
    public NoClipHack() {
        super("NoClip");
        this.setCategory(Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PlayerMoveListener.class, this);
        EVENTS.add(IsNormalCubeListener.class, this);
        EVENTS.add(VisGraphListener.class, this);
        EVENTS.add(AirStrafingSpeedListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PlayerMoveListener.class, this);
        EVENTS.remove(IsNormalCubeListener.class, this);
        EVENTS.remove(VisGraphListener.class, this);
        EVENTS.remove(AirStrafingSpeedListener.class, this);
        NoClipHack.MC.field_1724.field_5960 = false;
    }

    @Override
    public void onUpdate() {
        class_746 player = NoClipHack.MC.field_1724;
        player.field_5960 = true;
        player.field_6017 = 0.0;
        player.method_24830(false);
        player.method_31549().field_7479 = false;
        player.method_18800(0.0, 0.0, 0.0);
        float speed = 0.2f;
        if (NoClipHack.MC.field_1690.field_1903.method_1434()) {
            player.method_5762(0.0, (double)speed, 0.0);
        }
        if (NoClipHack.MC.field_1690.field_1832.method_1434()) {
            player.method_5762(0.0, (double)(-speed), 0.0);
        }
    }

    @Override
    public void onGetAirStrafingSpeed(AirStrafingSpeedListener.AirStrafingSpeedEvent event) {
        event.setSpeed(0.2f);
    }

    @Override
    public void onPlayerMove() {
        NoClipHack.MC.field_1724.field_5960 = true;
    }

    @Override
    public void onIsNormalCube(IsNormalCubeListener.IsNormalCubeEvent event) {
        event.cancel();
    }

    @Override
    public void onVisGraph(VisGraphListener.VisGraphEvent event) {
        event.cancel();
    }
}
