package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2848;
import net.minecraft.class_3532;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags(value={"EasyElytra", "extra elytra", "easy elytra"})
public final class ExtraElytraHack
extends Hack
implements UpdateListener {
    private final CheckboxSetting instantFly = new CheckboxSetting("Instant fly", "Jump to fly, no weird double-jump needed!", true);
    private final CheckboxSetting speedCtrl = new CheckboxSetting("Speed control", "Control your speed with the Forward and Back keys.\n(default: W and S)\nNo fireworks needed!", true);
    private final CheckboxSetting heightCtrl = new CheckboxSetting("Height control", "Control your height with the Jump and Sneak keys.\n(default: Spacebar and Shift)\nNo fireworks needed!", false);
    private final CheckboxSetting stopInWater = new CheckboxSetting("Stop flying in water", true);
    private int jumpTimer;

    public ExtraElytraHack() {
        super("ExtraElytra");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.instantFly);
        this.addSetting(this.speedCtrl);
        this.addSetting(this.heightCtrl);
        this.addSetting(this.stopInWater);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        this.jumpTimer = 0;
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (this.jumpTimer > 0) {
            --this.jumpTimer;
        }
        if (!ExtraElytraHack.MC.field_1724.method_63628()) {
            return;
        }
        if (ExtraElytraHack.MC.field_1724.method_6128()) {
            if (this.stopInWater.isChecked() && ExtraElytraHack.MC.field_1724.method_5799()) {
                this.sendStartStopPacket();
                return;
            }
            this.controlSpeed();
            this.controlHeight();
            return;
        }
        if (ExtraElytraHack.MC.field_1690.field_1903.method_1434()) {
            this.doInstantFly();
        }
    }

    private void sendStartStopPacket() {
        class_2848 packet = new class_2848((class_1297)ExtraElytraHack.MC.field_1724, class_2848.class_2849.field_12982);
        ExtraElytraHack.MC.field_1724.field_3944.method_52787((class_2596)packet);
    }

    private void controlHeight() {
        if (!this.heightCtrl.isChecked()) {
            return;
        }
        class_243 v = ExtraElytraHack.MC.field_1724.method_18798();
        boolean jump = ExtraElytraHack.MC.field_1690.field_1903.method_1434();
        boolean sneak = IKeyMapping.get(ExtraElytraHack.MC.field_1690.field_1832).isActuallyDown();
        if (sneak) {
            ExtraElytraHack.MC.field_1690.field_1832.method_23481(false);
        }
        if (jump && !sneak) {
            ExtraElytraHack.MC.field_1724.method_18800(v.field_1352, v.field_1351 + 0.08, v.field_1350);
        } else if (sneak && !jump) {
            ExtraElytraHack.MC.field_1724.method_18800(v.field_1352, v.field_1351 - 0.04, v.field_1350);
        }
    }

    private void controlSpeed() {
        if (!this.speedCtrl.isChecked()) {
            return;
        }
        float yaw = (float)Math.toRadians(ExtraElytraHack.MC.field_1724.method_36454());
        class_243 forward = new class_243((double)(-class_3532.method_15374((double)yaw)) * 0.05, 0.0, (double)class_3532.method_15362((double)yaw) * 0.05);
        class_243 v = ExtraElytraHack.MC.field_1724.method_18798();
        if (ExtraElytraHack.MC.field_1690.field_1894.method_1434()) {
            ExtraElytraHack.MC.field_1724.method_18799(v.method_1019(forward));
        } else if (ExtraElytraHack.MC.field_1690.field_1881.method_1434()) {
            ExtraElytraHack.MC.field_1724.method_18799(v.method_1020(forward));
        }
    }

    private void doInstantFly() {
        if (!this.instantFly.isChecked()) {
            return;
        }
        if (this.jumpTimer <= 0) {
            this.jumpTimer = 20;
            ExtraElytraHack.MC.field_1724.method_6100(false);
            ExtraElytraHack.MC.field_1724.method_5728(true);
            ExtraElytraHack.MC.field_1724.method_6043();
        }
        this.sendStartStopPacket();
    }
}
