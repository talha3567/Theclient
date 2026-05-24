package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.IsPlayerInWaterListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.events.VelocityFromFluidListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags(value={"anti water push", "NoWaterPush", "no water push"})
public final class AntiWaterPushHack
extends Hack
implements UpdateListener,
VelocityFromFluidListener,
IsPlayerInWaterListener {
    private final CheckboxSetting preventSlowdown = new CheckboxSetting("Prevent slowdown", "Allows you to walk underwater at full speed.\nSome servers consider this a speedhack.", false);

    public AntiWaterPushHack() {
        super("AntiWaterPush");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.preventSlowdown);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(VelocityFromFluidListener.class, this);
        EVENTS.add(IsPlayerInWaterListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(VelocityFromFluidListener.class, this);
        EVENTS.remove(IsPlayerInWaterListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (!this.preventSlowdown.isChecked()) {
            return;
        }
        if (!AntiWaterPushHack.MC.field_1690.field_1903.method_1434()) {
            return;
        }
        if (!AntiWaterPushHack.MC.field_1724.method_24828()) {
            return;
        }
        if (!IMC.getPlayer().isTouchingWaterBypass()) {
            return;
        }
        AntiWaterPushHack.MC.field_1724.method_6043();
    }

    @Override
    public void onVelocityFromFluid(VelocityFromFluidListener.VelocityFromFluidEvent event) {
        if (event.getEntity() == AntiWaterPushHack.MC.field_1724) {
            event.cancel();
        }
    }

    @Override
    public void onIsPlayerInWater(IsPlayerInWaterListener.IsPlayerInWaterEvent event) {
        if (this.preventSlowdown.isChecked()) {
            event.setInWater(false);
        }
    }

    public boolean isPreventingSlowdown() {
        return this.preventSlowdown.isChecked();
    }
}
