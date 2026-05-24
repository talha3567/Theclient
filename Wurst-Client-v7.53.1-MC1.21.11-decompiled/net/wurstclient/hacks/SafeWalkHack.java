package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"safe walk", "SneakSafety", "sneak safety", "SpeedBridgeHelper", "speed bridge helper"})
public final class SafeWalkHack
extends Hack {
    private final CheckboxSetting sneak = new CheckboxSetting("Sneak at edges", "Visibly sneak at edges.", false);
    private final SliderSetting edgeDistance = new SliderSetting("Sneak edge distance", "How close SafeWalk will let you get to the edge before sneaking.\n\nThis setting is only used when \"Sneak at edges\" is enabled.", 0.05, 0.05, 0.25, 0.001, SliderSetting.ValueDisplay.DECIMAL.withSuffix("m"));
    private boolean sneaking;

    public SafeWalkHack() {
        super("SafeWalk");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.sneak);
        this.addSetting(this.edgeDistance);
    }

    @Override
    protected void onEnable() {
        SafeWalkHack.WURST.getHax().parkourHack.setEnabled(false);
        this.sneaking = false;
    }

    @Override
    protected void onDisable() {
        if (this.sneaking) {
            this.setSneaking(false);
        }
    }

    public void onClipAtLedge(boolean clipping) {
        class_746 player = SafeWalkHack.MC.field_1724;
        if (!(this.isEnabled() && this.sneak.isChecked() && player.method_24828())) {
            if (this.sneaking) {
                this.setSneaking(false);
            }
            return;
        }
        class_238 box = player.method_5829();
        class_238 adjustedBox = box.method_1012(0.0, (double)(-player.method_49476()), 0.0).method_1009(-this.edgeDistance.getValue(), 0.0, -this.edgeDistance.getValue());
        if (SafeWalkHack.MC.field_1687.method_8587((class_1297)player, adjustedBox)) {
            clipping = true;
        }
        this.setSneaking(clipping);
    }

    private void setSneaking(boolean sneaking) {
        IKeyMapping sneakKey = IKeyMapping.get(SafeWalkHack.MC.field_1690.field_1832);
        if (sneaking) {
            sneakKey.method_23481(true);
        } else {
            sneakKey.resetPressedState();
        }
        this.sneaking = sneaking;
    }
}
