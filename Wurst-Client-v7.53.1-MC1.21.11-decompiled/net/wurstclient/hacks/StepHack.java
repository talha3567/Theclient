package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_634;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BlockUtils;

public final class StepHack
extends Hack
implements UpdateListener {
    private final EnumSetting<Mode> mode = new EnumSetting("Mode", "\u00a7lSimple\u00a7r mode can step up multiple blocks (enables Height slider).\n\u00a7lLegit\u00a7r mode can bypass NoCheat+.", (Enum[])Mode.values(), (Enum)Mode.LEGIT);
    private final SliderSetting height = new SliderSetting("Height", "Only works in \u00a7lSimple\u00a7r mode.", 1.0, 1.0, 10.0, 1.0, SliderSetting.ValueDisplay.INTEGER);

    public StepHack() {
        super("Step");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.mode);
        this.addSetting(this.height);
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
        if (this.mode.getSelected() == Mode.SIMPLE) {
            return;
        }
        class_746 player = StepHack.MC.field_1724;
        if (!player.field_5976) {
            return;
        }
        if (!player.method_24828() || player.method_6101() || player.method_5799() || player.method_5771()) {
            return;
        }
        if (player.field_3913.method_3128().method_35584() <= 1.0E-5f) {
            return;
        }
        if (player.field_6282) {
            return;
        }
        class_238 box = player.method_5829().method_989(0.0, 0.05, 0.0).method_1014(0.05);
        if (!StepHack.MC.field_1687.method_8587((class_1297)player, box.method_989(0.0, 1.0, 0.0))) {
            return;
        }
        double stepHeight = BlockUtils.getBlockCollisions(box).mapToDouble(bb -> bb.field_1325).max().orElse(Double.NEGATIVE_INFINITY);
        if ((stepHeight -= player.method_23318()) < 0.0 || stepHeight > 1.0) {
            return;
        }
        class_634 netHandler = player.field_3944;
        netHandler.method_52787((class_2596)new class_2828.class_2829(player.method_23317(), player.method_23318() + 0.42 * stepHeight, player.method_23321(), player.method_24828(), StepHack.MC.field_1724.field_5976));
        netHandler.method_52787((class_2596)new class_2828.class_2829(player.method_23317(), player.method_23318() + 0.753 * stepHeight, player.method_23321(), player.method_24828(), StepHack.MC.field_1724.field_5976));
        player.method_5814(player.method_23317(), player.method_23318() + stepHeight, player.method_23321());
    }

    public float adjustStepHeight(float stepHeight) {
        if (this.isEnabled() && this.mode.getSelected() == Mode.SIMPLE) {
            return this.height.getValueF();
        }
        return stepHeight;
    }

    public boolean isAutoJumpAllowed() {
        return !this.isEnabled() && !StepHack.WURST.getCmds().goToCmd.isActive();
    }

    private static enum Mode {
        SIMPLE("Simple"),
        LEGIT("Legit");

        private final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
