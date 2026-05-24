package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.ServerboundMovePlayerPacketAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class AntiHunger
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> sprint;
    private final Setting<Boolean> onGround;
    private boolean lastOnGround;
    private boolean ignorePacket;

    public AntiHunger() {
        super(Categories.Player, "anti-hunger", "Reduces (does NOT remove) hunger consumption.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sprint = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sprint")).description("Spoofs sprinting packets.")).defaultValue(true)).build());
        this.onGround = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("on-ground")).description("Spoofs the onGround flag.")).defaultValue(true)).build());
    }

    @Override
    public void onActivate() {
        this.lastOnGround = this.mc.player.onGround();
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        ServerboundPlayerCommandPacket packet;
        if (this.ignorePacket && event.packet instanceof ServerboundMovePlayerPacket) {
            this.ignorePacket = false;
            return;
        }
        if (this.mc.player.isPassenger() || this.mc.player.isInWater() || this.mc.player.isUnderWater()) {
            return;
        }
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof ServerboundPlayerCommandPacket) {
            packet = (ServerboundPlayerCommandPacket)packet2;
            if (this.sprint.get().booleanValue() && packet.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING) {
                event.cancel();
            }
        }
        if ((packet2 = event.packet) instanceof ServerboundMovePlayerPacket) {
            packet = (ServerboundMovePlayerPacket)packet2;
            if (this.onGround.get().booleanValue() && this.mc.player.onGround() && this.mc.player.fallDistance <= 0.0 && !this.mc.gameMode.isDestroying()) {
                ((ServerboundMovePlayerPacketAccessor)packet).meteor$setOnGround(false);
            }
        }
    }

    @EventHandler
    private void onTick(SendMovementPacketsEvent.Pre event) {
        if (this.mc.player.onGround() && !this.lastOnGround && this.onGround.get().booleanValue()) {
            this.ignorePacket = true;
        }
        this.lastOnGround = this.mc.player.onGround();
    }
}
