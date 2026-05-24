package meteordevelopment.meteorclient.systems.modules.misc;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.ServerConnectBeginEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public class AutoReconnect
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Double> time;
    public final Setting<Boolean> button;
    public Pair<ServerAddress, ServerData> lastServerConnection;

    public AutoReconnect() {
        super(Categories.Misc, "auto-reconnect", "Automatically reconnects when disconnected from a server.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.time = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("delay")).description("The amount of seconds to wait before reconnecting to the server.")).defaultValue(3.5).min(0.0).decimalPlaces(1).build());
        this.button = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hide-buttons")).description("Will hide the buttons related to Auto Reconnect.")).defaultValue(false)).build());
        MeteorClient.EVENT_BUS.subscribe(new StaticListener(this));
    }

    private class StaticListener {
        final /* synthetic */ AutoReconnect this$0;

        private StaticListener(AutoReconnect autoReconnect) {
            AutoReconnect autoReconnect2 = autoReconnect;
            Objects.requireNonNull(autoReconnect2);
            this.this$0 = autoReconnect2;
        }

        @EventHandler
        private void onGameJoined(ServerConnectBeginEvent event) {
            this.this$0.lastServerConnection = new ObjectObjectImmutablePair((Object)event.address, (Object)event.info);
        }
    }
}
