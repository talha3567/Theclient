package net.wurstclient.hacks;

import java.util.ArrayDeque;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.FakePlayerEntity;

@DontSaveState
@SearchTags(value={"LagSwitch", "lag switch"})
public final class BlinkHack
extends Hack
implements UpdateListener,
PacketOutputListener {
    private final SliderSetting limit = new SliderSetting("Limit", "Automatically restarts Blink once the given number of packets have been suspended.\n\n0 = no limit", 0.0, 0.0, 500.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withLabel(0.0, "disabled"));
    private final ArrayDeque<class_2828> packets = new ArrayDeque();
    private FakePlayerEntity fakePlayer;

    public BlinkHack() {
        super("Blink");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.limit);
    }

    @Override
    public String getRenderName() {
        if (this.limit.getValueI() == 0) {
            return this.getName() + " [" + this.packets.size() + "]";
        }
        return this.getName() + " [" + this.packets.size() + "/" + this.limit.getValueI() + "]";
    }

    @Override
    protected void onEnable() {
        this.fakePlayer = new FakePlayerEntity();
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PacketOutputListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PacketOutputListener.class, this);
        this.fakePlayer.despawn();
        this.packets.forEach(p -> BlinkHack.MC.field_1724.field_3944.method_52787((class_2596)p));
        this.packets.clear();
    }

    @Override
    public void onUpdate() {
        if (this.limit.getValueI() == 0) {
            return;
        }
        if (this.packets.size() >= this.limit.getValueI()) {
            this.setEnabled(false);
            this.setEnabled(true);
        }
    }

    @Override
    public void onSentPacket(PacketOutputListener.PacketOutputEvent event) {
        if (!(event.getPacket() instanceof class_2828)) {
            return;
        }
        event.cancel();
        class_2828 packet = (class_2828)event.getPacket();
        class_2828 prevPacket = this.packets.peekLast();
        if (prevPacket != null && packet.method_12273() == prevPacket.method_12273() && packet.method_12271(-1.0f) == prevPacket.method_12271(-1.0f) && packet.method_12270(-1.0f) == prevPacket.method_12270(-1.0f) && packet.method_12269(-1.0) == prevPacket.method_12269(-1.0) && packet.method_12268(-1.0) == prevPacket.method_12268(-1.0) && packet.method_12274(-1.0) == prevPacket.method_12274(-1.0)) {
            return;
        }
        this.packets.addLast(packet);
    }

    public void cancel() {
        this.packets.clear();
        this.fakePlayer.resetPlayerPosition();
        this.setEnabled(false);
    }
}
