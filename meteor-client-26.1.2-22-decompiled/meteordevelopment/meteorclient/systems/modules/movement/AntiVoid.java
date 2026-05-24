package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

public class AntiVoid
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private boolean wasFlightEnabled;
    private boolean hasRun;

    public AntiVoid() {
        super(Categories.Movement, "anti-void", "Attempts to prevent you from falling into the void.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The method to prevent you from falling into the void.")).defaultValue(Mode.Jump)).onChanged(mode -> this.onActivate())).build());
    }

    @Override
    public void onActivate() {
        if (this.mode.get() == Mode.Flight) {
            this.wasFlightEnabled = Modules.get().isActive(Flight.class);
        }
    }

    @Override
    public void onDeactivate() {
        if (!this.wasFlightEnabled && this.mode.get() == Mode.Flight && Utils.canUpdate()) {
            Modules.get().get(Flight.class).disable();
        }
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        int minY = this.mc.level.getMinY();
        if (this.mc.player.getY() > (double)minY || this.mc.player.getY() < (double)(minY - 15)) {
            if (this.hasRun && this.mode.get() == Mode.Flight) {
                Modules.get().get(Flight.class).disable();
                this.hasRun = false;
            }
            return;
        }
        switch (this.mode.get().ordinal()) {
            case 0: {
                Modules.get().get(Flight.class).enable();
                this.hasRun = true;
                break;
            }
            case 1: {
                this.mc.player.jumpFromGround();
            }
        }
    }

    public static enum Mode {
        Flight,
        Jump;

    }
}
