package net.wurstclient.hacks;

import net.minecraft.class_1802;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"no fall"})
public final class NoFallHack
extends Hack
implements UpdateListener {
    private final CheckboxSetting allowElytra = new CheckboxSetting("Allow elytra", "description.wurst.setting.nofall.allow_elytra", false);
    private final CheckboxSetting pauseForMace = new CheckboxSetting("Pause for mace", "description.wurst.setting.nofall.pause_for_mace", false);
    private final SliderSetting minFallDistance = new SliderSetting("Min fall distance", "description.wurst.setting.nofall.min_fall_distance", 1.0, 0.0, 10.0, 0.1, SliderSetting.ValueDisplay.DECIMAL.withSuffix("m").withLabel(0.0, "off"));
    private final SliderSetting minFallDistanceElytra = new SliderSetting("Min elytra fall distance", "description.wurst.setting.nofall.min_elytra_fall_distance", 2.0, 0.0, 10.0, 0.1, SliderSetting.ValueDisplay.DECIMAL.withSuffix("m").withLabel(0.0, "off"));

    public NoFallHack() {
        super("NoFall");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.allowElytra);
        this.addSetting(this.pauseForMace);
        this.addSetting(this.minFallDistance);
        this.addSetting(this.minFallDistanceElytra);
    }

    @Override
    public String getRenderName() {
        if (NoFallHack.MC.field_1724 != null && this.isPaused()) {
            return this.getName() + " (paused)";
        }
        return this.getName();
    }

    @Override
    protected void onEnable() {
        NoFallHack.WURST.getHax().antiHungerHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (this.isPaused()) {
            return;
        }
        NoFallHack.MC.field_1724.field_3944.method_52787((class_2596)new class_2828.class_5911(true, NoFallHack.MC.field_1724.field_5976));
    }

    private boolean isPaused() {
        boolean creativeFlying;
        class_746 player = NoFallHack.MC.field_1724;
        if (player.method_31549().field_7480) {
            return true;
        }
        boolean fallFlying = player.method_6128();
        if (fallFlying && !this.allowElytra.isChecked()) {
            return true;
        }
        if (this.pauseForMace.isChecked() && player.method_6047().method_31574(class_1802.field_49814)) {
            return true;
        }
        boolean bl = creativeFlying = NoFallHack.WURST.getHax().creativeFlightHack.isEnabled() && player.method_31549().field_7479;
        if (!creativeFlying) {
            double d = player.field_6017;
            double d2 = fallFlying ? this.minFallDistanceElytra.getValue() : this.minFallDistance.getValue();
            if (d <= d2) {
                return true;
            }
        }
        return fallFlying && player.method_5715() && !this.isFallingFastEnoughToCauseDamage(player);
    }

    private boolean isFallingFastEnoughToCauseDamage(class_746 player) {
        return player.method_18798().field_1351 < -0.5;
    }
}
