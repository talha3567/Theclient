package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.stream.Collectors;
import net.minecraft.class_2189;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2404;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.BlockUtils;

@SearchTags(value={"WaterWalking", "water walking"})
public final class JesusHack
extends Hack
implements UpdateListener,
PacketOutputListener {
    private final CheckboxSetting bypass = new CheckboxSetting("NoCheat+ bypass", "Bypasses NoCheat+ but slows down your movement.", false);
    private int tickTimer = 10;
    private int packetTimer = 0;

    public JesusHack() {
        super("Jesus");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.bypass);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PacketOutputListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PacketOutputListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (JesusHack.MC.field_1690.field_1832.method_1434()) {
            return;
        }
        class_746 player = JesusHack.MC.field_1724;
        if (player.method_5799() || player.method_5771()) {
            class_243 velocity = player.method_18798();
            player.method_18800(velocity.field_1352, 0.11, velocity.field_1350);
            this.tickTimer = 0;
            return;
        }
        class_243 velocity = player.method_18798();
        if (this.tickTimer == 0) {
            player.method_18800(velocity.field_1352, 0.3, velocity.field_1350);
        } else if (this.tickTimer == 1) {
            player.method_18800(velocity.field_1352, 0.0, velocity.field_1350);
        }
        ++this.tickTimer;
    }

    @Override
    public void onSentPacket(PacketOutputListener.PacketOutputEvent event) {
        if (!(event.getPacket() instanceof class_2828)) {
            return;
        }
        class_2828 packet = (class_2828)event.getPacket();
        if (!(packet instanceof class_2828.class_2829) && !(packet instanceof class_2828.class_2830)) {
            return;
        }
        if (JesusHack.MC.field_1724.method_5799()) {
            return;
        }
        if (JesusHack.MC.field_1724.field_6017 > 3.0) {
            return;
        }
        if (!this.isOverLiquid()) {
            return;
        }
        if (JesusHack.MC.field_1724.field_3913 == null) {
            event.cancel();
            return;
        }
        ++this.packetTimer;
        if (this.packetTimer < 4) {
            return;
        }
        event.cancel();
        double x = packet.method_12269(0.0);
        double y = packet.method_12268(0.0);
        double z = packet.method_12274(0.0);
        y = this.bypass.isChecked() && JesusHack.MC.field_1724.field_6012 % 2 == 0 ? (y -= 0.05) : (y += 0.05);
        Object newPacket = packet instanceof class_2828.class_2829 ? new class_2828.class_2829(x, y, z, true, JesusHack.MC.field_1724.field_5976) : new class_2828.class_2830(x, y, z, packet.method_12271(0.0f), packet.method_12270(0.0f), true, JesusHack.MC.field_1724.field_5976);
        JesusHack.MC.field_1724.field_3944.method_48296().method_10743((class_2596)newPacket);
    }

    public boolean isOverLiquid() {
        boolean foundLiquid = false;
        boolean foundSolid = false;
        class_238 box = JesusHack.MC.field_1724.method_5829().method_989(0.0, -0.5, 0.0);
        ArrayList blockCollisions = BlockUtils.getBlockCollisions(box).map(bb -> BlockUtils.getBlock(class_2338.method_49638((class_2374)bb.method_1005()))).collect(Collectors.toCollection(ArrayList::new));
        for (class_2248 block : blockCollisions) {
            if (block instanceof class_2404) {
                foundLiquid = true;
                continue;
            }
            if (block instanceof class_2189) continue;
            foundSolid = true;
        }
        return foundLiquid && !foundSolid;
    }

    public boolean shouldBeSolid() {
        return this.isEnabled() && JesusHack.MC.field_1724 != null && JesusHack.MC.field_1724.field_6017 <= 3.0 && !JesusHack.MC.field_1690.field_1832.method_1434() && !JesusHack.MC.field_1724.method_5799() && !JesusHack.MC.field_1724.method_5771();
    }
}
