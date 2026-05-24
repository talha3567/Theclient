package net.wurstclient.hacks;

import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.AirStrafingSpeedListener;
import net.wurstclient.events.IsPlayerInWaterListener;
import net.wurstclient.events.MouseScrollListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"FlyHack", "fly hack", "flying"})
public final class FlightHack
extends Hack
implements UpdateListener,
IsPlayerInWaterListener,
AirStrafingSpeedListener,
MouseScrollListener {
    private final SliderSetting horizontalSpeed = new SliderSetting("Horizontal speed", "description.wurst.setting.flight.horizontal_speed", 1.0, 0.05, 10.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final SliderSetting verticalSpeed = new SliderSetting("Vertical speed", "description.wurst.setting.flight.vertical_speed", 1.0, 0.05, 5.0, 0.05, v -> SliderSetting.ValueDisplay.DECIMAL.getValueString(this.getActualVerticalSpeed()));
    private final CheckboxSetting allowUnsafeVerticalSpeed = new CheckboxSetting("Allow unsafe vertical speed", "description.wurst.setting.flight.allow_unsafe_vertical_speed", false);
    private final CheckboxSetting scrollToChangeSpeed = new CheckboxSetting("Scroll to change speed", "description.wurst.setting.flight.scroll_to_change_speed", true);
    private final CheckboxSetting renderSpeed = new CheckboxSetting("Show speed in HackList", "description.wurst.setting.flight.show_speed_in_hacklist", true);
    private final CheckboxSetting antiKick = new CheckboxSetting("Anti-Kick", "description.wurst.setting.flight.anti-kick", false);
    private final SliderSetting antiKickInterval = new SliderSetting("Anti-Kick Interval", "description.wurst.setting.flight.anti-kick_interval", 70.0, 5.0, 80.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withSuffix(" ticks").withLabel(1.0, "1 tick"));
    private final SliderSetting antiKickDistance = new SliderSetting("Anti-Kick Distance", "description.wurst.setting.flight.anti-kick_distance", 0.035, 0.01, 0.2, 0.001, SliderSetting.ValueDisplay.DECIMAL.withSuffix("m"));
    private int tickCounter = 0;

    public FlightHack() {
        super("Flight");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.horizontalSpeed);
        this.addSetting(this.verticalSpeed);
        this.addSetting(this.allowUnsafeVerticalSpeed);
        this.addSetting(this.scrollToChangeSpeed);
        this.addSetting(this.renderSpeed);
        this.addSetting(this.antiKick);
        this.addSetting(this.antiKickInterval);
        this.addSetting(this.antiKickDistance);
    }

    @Override
    public String getRenderName() {
        if (!this.renderSpeed.isChecked()) {
            return this.getName();
        }
        return this.getName() + " [" + this.horizontalSpeed.getValueString() + ", " + this.verticalSpeed.getValueString() + "]";
    }

    @Override
    protected void onEnable() {
        this.tickCounter = 0;
        FlightHack.WURST.getHax().creativeFlightHack.setEnabled(false);
        FlightHack.WURST.getHax().jetpackHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(IsPlayerInWaterListener.class, this);
        EVENTS.add(AirStrafingSpeedListener.class, this);
        EVENTS.add(MouseScrollListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(IsPlayerInWaterListener.class, this);
        EVENTS.remove(AirStrafingSpeedListener.class, this);
        EVENTS.remove(MouseScrollListener.class, this);
    }

    @Override
    public void onUpdate() {
        class_746 player = FlightHack.MC.field_1724;
        player.method_18799(class_243.field_1353);
        player.method_31549().field_7479 = false;
        if (FlightHack.WURST.getHax().freecamHack.isMovingCamera()) {
            return;
        }
        double vSpeed = this.getActualVerticalSpeed();
        if (FlightHack.MC.field_1690.field_1903.method_1434()) {
            player.method_45319(new class_243(0.0, vSpeed, 0.0));
        }
        if (IKeyMapping.get(FlightHack.MC.field_1690.field_1832).isActuallyDown()) {
            FlightHack.MC.field_1690.field_1832.method_23481(false);
            player.method_45319(new class_243(0.0, -vSpeed, 0.0));
        }
        if (this.antiKick.isChecked()) {
            this.doAntiKick();
        }
    }

    @Override
    public void onGetAirStrafingSpeed(AirStrafingSpeedListener.AirStrafingSpeedEvent event) {
        if (FlightHack.WURST.getHax().freecamHack.isMovingCamera()) {
            return;
        }
        event.setSpeed(this.horizontalSpeed.getValueF());
    }

    @Override
    public void onMouseScroll(double amount) {
        if (!this.isControllingScrollEvents()) {
            return;
        }
        if (amount > 0.0) {
            this.horizontalSpeed.increaseValue();
        } else if (amount < 0.0) {
            this.horizontalSpeed.decreaseValue();
        }
    }

    public boolean isControllingScrollEvents() {
        return this.isEnabled() && this.scrollToChangeSpeed.isChecked() && FlightHack.MC.field_1755 == null && !FlightHack.WURST.getOtfs().zoomOtf.isControllingScrollEvents() && !FlightHack.WURST.getHax().freecamHack.isMovingCamera();
    }

    private void doAntiKick() {
        if (this.tickCounter > this.antiKickInterval.getValueI() + 1) {
            this.tickCounter = 0;
        }
        class_243 velocity = FlightHack.MC.field_1724.method_18798();
        switch (this.tickCounter) {
            case 0: {
                if (velocity.field_1351 <= -this.antiKickDistance.getValue()) {
                    this.tickCounter = 2;
                    break;
                }
                FlightHack.MC.field_1724.method_18800(velocity.field_1352, -this.antiKickDistance.getValue(), velocity.field_1350);
                break;
            }
            case 1: {
                FlightHack.MC.field_1724.method_18800(velocity.field_1352, this.antiKickDistance.getValue(), velocity.field_1350);
            }
        }
        ++this.tickCounter;
    }

    @Override
    public void onIsPlayerInWater(IsPlayerInWaterListener.IsPlayerInWaterEvent event) {
        event.setInWater(false);
    }

    public double getHorizontalSpeed() {
        return this.horizontalSpeed.getValue();
    }

    public double getActualVerticalSpeed() {
        boolean limitVerticalSpeed = !this.allowUnsafeVerticalSpeed.isChecked() && !FlightHack.MC.field_1724.method_31549().field_7480;
        return class_3532.method_15350((double)(this.horizontalSpeed.getValue() * this.verticalSpeed.getValue()), (double)0.05, (double)(limitVerticalSpeed ? 3.95 : 10.0));
    }
}
