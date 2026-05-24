package net.wurstclient.other_features;

import net.minecraft.class_2596;
import net.minecraft.class_2817;
import net.minecraft.class_8709;
import net.minecraft.class_8710;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ConnectionPacketOutputListener;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;

@DontBlock
@SearchTags(value={"vanilla spoof", "AntiFabric", "anti fabric", "LibHatesMods", "HackedServer"})
public final class VanillaSpoofOtf
extends OtherFeature
implements ConnectionPacketOutputListener {
    private final CheckboxSetting spoof = new CheckboxSetting("Spoof Vanilla", false);

    public VanillaSpoofOtf() {
        super("VanillaSpoof", "Bypasses anti-Fabric plugins by pretending to be a vanilla client.");
        this.addSetting(this.spoof);
        EVENTS.add(ConnectionPacketOutputListener.class, this);
    }

    @Override
    public void onSentConnectionPacket(ConnectionPacketOutputListener.ConnectionPacketOutputEvent event) {
        if (!this.spoof.isChecked()) {
            return;
        }
        class_2596<?> class_25962 = event.getPacket();
        if (!(class_25962 instanceof class_2817)) {
            return;
        }
        class_2817 packet = (class_2817)class_25962;
        if (packet.comp_1647() instanceof class_8709) {
            event.setPacket((class_2596<?>)new class_2817((class_8710)new class_8709("vanilla")));
        }
    }

    @Override
    public boolean isEnabled() {
        return this.spoof.isChecked();
    }

    @Override
    public String getPrimaryAction() {
        return this.isEnabled() ? "Disable" : "Enable";
    }

    @Override
    public void doPrimaryAction() {
        this.spoof.setChecked(!this.spoof.isChecked());
    }
}
