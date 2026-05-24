package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class Blink
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> renderOriginal;
    private final Setting<Integer> delay;
    private final Setting<Keybind> cancelBlink;
    private final List<ServerboundMovePlayerPacket> packets;
    private FakePlayerEntity model;
    private final Vector3d start;
    private boolean cancelled;
    private boolean sending;
    private int timer;

    public Blink() {
        super(Categories.Movement, "blink", "Allows you to essentially teleport while suspending motion updates.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.renderOriginal = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-original")).description("Renders your player model at the original position.")).defaultValue(true)).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("pulse-delay")).description("After the duration in ticks has elapsed, send all packets and start blinking again. 0 to disable.")).defaultValue(0)).min(0).sliderMax(60).build());
        this.cancelBlink = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("cancel-blink")).description("Cancels sending packets and sends you back to your original position.")).defaultValue(Keybind.none())).action(() -> {
            this.cancelled = true;
            this.disable();
        }).build());
        this.packets = new ArrayList<ServerboundMovePlayerPacket>();
        this.start = new Vector3d();
        this.timer = 0;
        this.runInMainMenu = true;
    }

    @Override
    public void onActivate() {
        if (!Utils.canUpdate()) {
            return;
        }
        if (this.renderOriginal.get().booleanValue()) {
            this.model = new FakePlayerEntity((Player)this.mc.player, this.mc.player.getGameProfile().name(), 20.0f, true);
            this.model.doNotPush = true;
            this.model.hideWhenInsideCamera = true;
            this.model.noHit = true;
            this.model.spawn();
        }
        Utils.set(this.start, this.mc.player.position());
    }

    @Override
    public void onDeactivate() {
        if (!Utils.canUpdate()) {
            return;
        }
        this.dumpPackets(!this.cancelled);
        if (this.cancelled) {
            this.mc.player.setPos(this.start.x, this.start.y, this.start.z);
            this.mc.player.setDeltaMovement(Vec3.ZERO);
        }
        this.cancelled = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) {
            return;
        }
        ++this.timer;
        if (this.delay.get() != 0 && this.delay.get() <= this.timer) {
            this.onDeactivate();
            this.onActivate();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        ServerboundMovePlayerPacket prev;
        if (!Utils.canUpdate()) {
            return;
        }
        if (this.sending) {
            return;
        }
        Packet<?> packet = event.packet;
        if (!(packet instanceof ServerboundMovePlayerPacket)) {
            return;
        }
        ServerboundMovePlayerPacket p = (ServerboundMovePlayerPacket)packet;
        event.cancel();
        ServerboundMovePlayerPacket serverboundMovePlayerPacket = prev = this.packets.isEmpty() ? null : this.packets.getLast();
        if (prev != null && p.isOnGround() == prev.isOnGround() && p.getYRot(-1.0f) == prev.getYRot(-1.0f) && p.getXRot(-1.0f) == prev.getXRot(-1.0f) && p.getX(-1.0) == prev.getX(-1.0) && p.getY(-1.0) == prev.getY(-1.0) && p.getZ(-1.0) == prev.getZ(-1.0)) {
            return;
        }
        List<ServerboundMovePlayerPacket> list = this.packets;
        synchronized (list) {
            this.packets.add(p);
        }
    }

    @EventHandler
    private void onJoinGame(GameJoinedEvent event) {
        this.warning("Blink is currently enabled; you won't be able to interact with anything properly until you disable it!", new Object[0]);
    }

    @EventHandler
    private void onLeaveGame(GameLeftEvent event) {
        this.onDeactivate();
    }

    @Override
    public String getInfoString() {
        return String.format("%.1f", Float.valueOf((float)this.timer / 20.0f));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dumpPackets(boolean send) {
        this.sending = true;
        List<ServerboundMovePlayerPacket> list = this.packets;
        synchronized (list) {
            if (send) {
                this.packets.forEach(arg_0 -> ((ClientPacketListener)this.mc.player.connection).send(arg_0));
            }
            this.packets.clear();
        }
        this.sending = false;
        if (this.model != null) {
            this.model.despawn();
            this.model = null;
        }
        this.timer = 0;
    }
}
