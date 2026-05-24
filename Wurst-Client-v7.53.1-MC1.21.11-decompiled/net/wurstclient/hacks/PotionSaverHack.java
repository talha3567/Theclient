package net.wurstclient.hacks;

import net.minecraft.class_2828;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"potion saver"})
public final class PotionSaverHack
extends Hack
implements PacketOutputListener {
    public PotionSaverHack() {
        super("PotionSaver");
        this.setCategory(Category.OTHER);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(PacketOutputListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(PacketOutputListener.class, this);
    }

    @Override
    public void onSentPacket(PacketOutputListener.PacketOutputEvent event) {
        if (!this.isFrozen()) {
            return;
        }
        if (event.getPacket() instanceof class_2828) {
            event.cancel();
        }
    }

    public boolean isFrozen() {
        return this.isEnabled() && PotionSaverHack.MC.field_1724 != null && !PotionSaverHack.MC.field_1724.method_6088().isEmpty() && PotionSaverHack.MC.field_1724.method_18798().field_1352 == 0.0 && PotionSaverHack.MC.field_1724.method_18798().field_1350 == 0.0;
    }
}
