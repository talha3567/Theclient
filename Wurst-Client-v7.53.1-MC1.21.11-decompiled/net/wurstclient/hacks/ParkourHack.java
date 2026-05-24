package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

public final class ParkourHack
extends Hack
implements UpdateListener {
    private final SliderSetting minDepth = new SliderSetting("Min depth", "Won't jump over a pit if it isn't at least this deep.\nIncrease to stop Parkour from jumping down stairs.\nDecrease to make Parkour jump at the edge of carpets.", 0.5, 0.05, 10.0, 0.05, SliderSetting.ValueDisplay.DECIMAL.withSuffix("m"));
    private final SliderSetting edgeDistance = new SliderSetting("Edge distance", "How close Parkour will let you get to the edge before jumping.", 0.001, 0.001, 0.25, 0.001, SliderSetting.ValueDisplay.DECIMAL.withSuffix("m"));
    private final CheckboxSetting sneak = new CheckboxSetting("Jump while sneaking", "Keeps Parkour active even while you are sneaking.\nYou may want to increase the \u00a7lEdge \u00a7ldistance\u00a7r slider when using this option.", false);

    public ParkourHack() {
        super("Parkour");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.minDepth);
        this.addSetting(this.edgeDistance);
        this.addSetting(this.sneak);
    }

    @Override
    protected void onEnable() {
        ParkourHack.WURST.getHax().safeWalkHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (!ParkourHack.MC.field_1724.method_24828() || ParkourHack.MC.field_1690.field_1903.method_1434()) {
            return;
        }
        if (!this.sneak.isChecked() && (ParkourHack.MC.field_1724.method_5715() || ParkourHack.MC.field_1690.field_1832.method_1434())) {
            return;
        }
        class_238 box = ParkourHack.MC.field_1724.method_5829();
        class_238 adjustedBox = box.method_1012(0.0, -this.minDepth.getValue(), 0.0).method_1009(-this.edgeDistance.getValue(), 0.0, -this.edgeDistance.getValue());
        if (!ParkourHack.MC.field_1687.method_8587((class_1297)ParkourHack.MC.field_1724, adjustedBox)) {
            return;
        }
        ParkourHack.MC.field_1724.method_6043();
    }
}
