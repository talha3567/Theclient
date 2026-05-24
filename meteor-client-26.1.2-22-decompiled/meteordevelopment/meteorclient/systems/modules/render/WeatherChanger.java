package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;

public class WeatherChanger
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> rainLevel;
    private final Setting<Double> thunderLevel;
    private float oldThunderLevel;
    private float oldRainLevel;

    public WeatherChanger() {
        super(Categories.Render, "weather-changer", "Allows you to override the world's current weather.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.rainLevel = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rain-level")).description("The specified rain level to be set.")).defaultValue(0.0).sliderRange(0.0, 1.0).build());
        this.thunderLevel = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("thunder-level")).description("The specified thunder level to be set.")).defaultValue(0.0).sliderRange(0.0, 1.0).build());
    }

    @Override
    public void onActivate() {
        if (this.mc.level == null) {
            return;
        }
        this.oldThunderLevel = this.mc.level.getThunderLevel(1.0f);
        this.oldRainLevel = this.mc.level.getRainLevel(1.0f);
    }

    @Override
    public void onDeactivate() {
        if (this.mc.level == null) {
            return;
        }
        this.mc.level.setRainLevel(this.oldRainLevel);
        this.mc.level.setThunderLevel(this.oldThunderLevel);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if (!(packet instanceof ClientboundGameEventPacket)) {
            return;
        }
        ClientboundGameEventPacket packet2 = (ClientboundGameEventPacket)packet;
        ClientboundGameEventPacket.Type type = packet2.getEvent();
        if (!this.isWeatherPacket(type)) {
            return;
        }
        if (type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE) {
            this.oldThunderLevel = packet2.getParam();
        } else if (type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
            this.oldRainLevel = packet2.getParam();
        }
        event.cancel();
    }

    private boolean isWeatherPacket(ClientboundGameEventPacket.Type type) {
        return type == ClientboundGameEventPacket.START_RAINING || type == ClientboundGameEventPacket.STOP_RAINING || type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE || type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.level == null) {
            return;
        }
        this.mc.level.setRainLevel(this.rainLevel.get().floatValue());
        this.mc.level.setThunderLevel(this.thunderLevel.get().floatValue());
    }
}
