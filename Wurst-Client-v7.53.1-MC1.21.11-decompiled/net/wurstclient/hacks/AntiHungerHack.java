package net.wurstclient.hacks;

import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.PacketUtils;

@DontSaveState
@SearchTags(value={"anti hunger"})
public final class AntiHungerHack
extends Hack
implements PacketOutputListener {
    public AntiHungerHack() {
        super("AntiHunger");
        this.setCategory(Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        AntiHungerHack.WURST.getHax().noFallHack.setEnabled(false);
        EVENTS.add(PacketOutputListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(PacketOutputListener.class, this);
    }

    @Override
    public void onSentPacket(PacketOutputListener.PacketOutputEvent event) {
        class_2596<?> class_25962 = event.getPacket();
        if (!(class_25962 instanceof class_2828)) {
            return;
        }
        class_2828 packet = (class_2828)class_25962;
        if (!AntiHungerHack.MC.field_1724.method_24828() || AntiHungerHack.MC.field_1724.field_6017 > 0.5) {
            return;
        }
        if (AntiHungerHack.MC.field_1761.method_2923()) {
            return;
        }
        event.setPacket((class_2596<?>)PacketUtils.modifyOnGround(packet, false));
    }
}
