package net.wurstclient.hacks;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2404;
import net.minecraft.class_243;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.events.AirStrafingSpeedListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BlockUtils;

public final class GlideHack
extends Hack
implements UpdateListener,
AirStrafingSpeedListener {
    private final SliderSetting fallSpeed = new SliderSetting("Fall speed", 0.125, 0.005, 0.25, 0.005, SliderSetting.ValueDisplay.DECIMAL);
    private final SliderSetting moveSpeed = new SliderSetting("Move speed", "Horizontal movement factor.", 1.2, 1.0, 5.0, 0.05, SliderSetting.ValueDisplay.PERCENTAGE);
    private final SliderSetting minHeight = new SliderSetting("Min height", "Won't glide when you are too close to the ground.", 0.0, 0.0, 2.0, 0.01, SliderSetting.ValueDisplay.DECIMAL.withLabel(0.0, "disabled"));
    private final CheckboxSetting pauseOnSneak = new CheckboxSetting("Pause when sneaking", true);

    public GlideHack() {
        super("Glide");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.fallSpeed);
        this.addSetting(this.moveSpeed);
        this.addSetting(this.minHeight);
        this.addSetting(this.pauseOnSneak);
    }

    @Override
    public String getRenderName() {
        class_746 player = GlideHack.MC.field_1724;
        if (player == null) {
            return this.getName();
        }
        if (this.pauseOnSneak.isChecked() && player.method_5715()) {
            return this.getName() + " (paused)";
        }
        return this.getName();
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(AirStrafingSpeedListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(AirStrafingSpeedListener.class, this);
    }

    @Override
    public void onUpdate() {
        class_746 player = GlideHack.MC.field_1724;
        if (this.pauseOnSneak.isChecked() && player.method_5715()) {
            return;
        }
        class_243 v = player.method_18798();
        if (player.method_24828() || player.method_5799() || player.method_5771() || player.method_6101() || v.field_1351 >= 0.0) {
            return;
        }
        if (this.minHeight.getValue() > 0.0) {
            class_238 box = player.method_5829();
            if (!GlideHack.MC.field_1687.method_18026(box = box.method_991(box.method_989(0.0, -this.minHeight.getValue(), 0.0)))) {
                return;
            }
            class_2338 min = class_2338.method_49637((double)box.field_1323, (double)box.field_1322, (double)box.field_1321);
            class_2338 max = class_2338.method_49637((double)box.field_1320, (double)box.field_1325, (double)box.field_1324);
            Stream<class_2338> stream = StreamSupport.stream(BlockUtils.getAllInBox(min, max).spliterator(), true);
            if (stream.map(BlockUtils::getBlock).anyMatch(class_2404.class::isInstance)) {
                return;
            }
        }
        player.method_18800(v.field_1352, Math.max(v.field_1351, -this.fallSpeed.getValue()), v.field_1350);
    }

    @Override
    public void onGetAirStrafingSpeed(AirStrafingSpeedListener.AirStrafingSpeedEvent event) {
        event.setSpeed(event.getDefaultSpeed() * this.moveSpeed.getValueF());
    }
}
