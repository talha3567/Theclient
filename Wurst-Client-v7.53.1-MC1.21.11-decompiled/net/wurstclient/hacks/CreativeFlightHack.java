package net.wurstclient.hacks;

import net.minecraft.class_1656;
import net.minecraft.class_243;
import net.minecraft.class_304;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"creative flight", "CreativeFly", "creative fly"})
public final class CreativeFlightHack
extends Hack
implements UpdateListener {
    private final CheckboxSetting antiKick = new CheckboxSetting("Anti-Kick", "Makes you fall a little bit every now and then to prevent you from getting kicked.", false);
    private final SliderSetting antiKickInterval = new SliderSetting("Anti-Kick Interval", "How often Anti-Kick should prevent you from getting kicked.\nMost servers will kick you after 80 ticks.", 30.0, 5.0, 80.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withSuffix(" ticks").withLabel(1.0, "1 tick"));
    private final SliderSetting antiKickDistance = new SliderSetting("Anti-Kick Distance", "How far Anti-Kick should make you fall.\nMost servers require at least 0.032m to stop you from getting kicked.", 0.07, 0.01, 0.2, 0.001, SliderSetting.ValueDisplay.DECIMAL.withSuffix("m"));
    private int tickCounter = 0;

    public CreativeFlightHack() {
        super("CreativeFlight");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.antiKick);
        this.addSetting(this.antiKickInterval);
        this.addSetting(this.antiKickDistance);
    }

    @Override
    protected void onEnable() {
        this.tickCounter = 0;
        CreativeFlightHack.WURST.getHax().jetpackHack.setEnabled(false);
        CreativeFlightHack.WURST.getHax().flightHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        class_746 player = CreativeFlightHack.MC.field_1724;
        class_1656 abilities = player.method_31549();
        boolean creative = player.method_31549().field_7477;
        abilities.field_7479 = creative && !player.method_24828();
        abilities.field_7478 = creative;
        this.restoreKeyPresses();
    }

    @Override
    public void onUpdate() {
        class_1656 abilities = CreativeFlightHack.MC.field_1724.method_31549();
        abilities.field_7478 = true;
        if (this.antiKick.isChecked() && abilities.field_7479) {
            this.doAntiKick();
        }
    }

    private void doAntiKick() {
        if (this.tickCounter > this.antiKickInterval.getValueI() + 2) {
            this.tickCounter = 0;
        }
        switch (this.tickCounter) {
            case 0: {
                if (CreativeFlightHack.MC.field_1690.field_1832.method_1434() && !CreativeFlightHack.MC.field_1690.field_1903.method_1434()) {
                    this.tickCounter = 3;
                    break;
                }
                this.setMotionY(-this.antiKickDistance.getValue());
                break;
            }
            case 1: {
                this.setMotionY(this.antiKickDistance.getValue());
                break;
            }
            case 2: {
                this.setMotionY(0.0);
                break;
            }
            case 3: {
                this.restoreKeyPresses();
            }
        }
        ++this.tickCounter;
    }

    private void setMotionY(double motionY) {
        CreativeFlightHack.MC.field_1690.field_1832.method_23481(false);
        CreativeFlightHack.MC.field_1690.field_1903.method_23481(false);
        class_243 velocity = CreativeFlightHack.MC.field_1724.method_18798();
        CreativeFlightHack.MC.field_1724.method_18800(velocity.field_1352, motionY, velocity.field_1350);
    }

    private void restoreKeyPresses() {
        class_304[] keys;
        for (class_304 key : keys = new class_304[]{CreativeFlightHack.MC.field_1690.field_1903, CreativeFlightHack.MC.field_1690.field_1832}) {
            IKeyMapping.get(key).resetPressedState();
        }
    }
}
