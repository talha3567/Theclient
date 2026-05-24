package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_5819;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

public final class KaboomHack
extends Hack
implements UpdateListener {
    private final SliderSetting power = new SliderSetting("Power", "description.wurst.setting.kaboom.power", 128.0, 32.0, 512.0, 32.0, SliderSetting.ValueDisplay.INTEGER);
    private final CheckboxSetting sound = new CheckboxSetting("Sound", "description.wurst.setting.kaboom.sound", true);
    private final CheckboxSetting particles = new CheckboxSetting("Particles", "description.wurst.setting.kaboom.particles", true);
    private final class_5819 random = class_5819.method_43047();

    public KaboomHack() {
        super("Kaboom");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.power);
        this.addSetting(this.sound);
        this.addSetting(this.particles);
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
        if (!KaboomHack.MC.field_1724.method_31549().field_7477 && !KaboomHack.MC.field_1724.method_24828()) {
            return;
        }
        double x = KaboomHack.MC.field_1724.method_23317();
        double y = KaboomHack.MC.field_1724.method_23318();
        double z = KaboomHack.MC.field_1724.method_23321();
        if (this.sound.isChecked()) {
            float soundPitch = (1.0f + (this.random.method_43057() - this.random.method_43057()) * 0.2f) * 0.7f;
            KaboomHack.MC.field_1687.method_8486(x, y, z, (class_3414)class_3417.field_15152.comp_349(), class_3419.field_15245, 4.0f, soundPitch, false);
        }
        if (this.particles.isChecked()) {
            KaboomHack.MC.field_1687.method_8406((class_2394)class_2398.field_11221, x, y, z, 1.0, 0.0, 0.0);
        }
        ArrayList<class_2338> blocks = this.getBlocksByDistanceReversed();
        for (int i = 0; i < this.power.getValueI(); ++i) {
            BlockBreaker.breakBlocksWithPacketSpam(blocks);
        }
        this.setEnabled(false);
    }

    private ArrayList<class_2338> getBlocksByDistanceReversed() {
        class_243 eyesVec = RotationUtils.getEyesPos();
        class_2338 eyesBlock = class_2338.method_49638((class_2374)eyesVec);
        double rangeSq = 36.0;
        int blockRange = 6;
        return BlockUtils.getAllInBoxStream(eyesBlock, blockRange).filter(pos -> pos.method_19770((class_2374)eyesVec) <= rangeSq).sorted(Comparator.comparingDouble(pos -> -pos.method_19770((class_2374)eyesVec))).collect(Collectors.toCollection(ArrayList::new));
    }
}
