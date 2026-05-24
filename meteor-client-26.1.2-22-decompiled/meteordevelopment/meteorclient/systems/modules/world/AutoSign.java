package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayDeque;
import java.util.Queue;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.AbstractSignEditScreenAccessor;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class AutoSign
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> delay;
    private String[] text;
    private final Queue<ServerboundSignUpdatePacket> queue;
    private int timer;

    public AutoSign() {
        super(Categories.World, "auto-sign", "Automatically writes signs. The first sign's text will be used.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The tick delay between sign update packets.")).defaultValue(10)).range(0, 100).sliderRange(0, 100).build());
        this.queue = new ArrayDeque<ServerboundSignUpdatePacket>();
        this.timer = 0;
    }

    @Override
    public void onDeactivate() {
        this.text = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.player == null || this.queue.peek() == null) {
            this.timer = 0;
            return;
        }
        if (this.timer < this.delay.get()) {
            ++this.timer;
            return;
        }
        this.mc.player.connection.send((Packet)this.queue.poll());
        this.timer = 0;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof ServerboundSignUpdatePacket)) {
            return;
        }
        this.text = ((ServerboundSignUpdatePacket)event.packet).getLines();
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(event.screen instanceof AbstractSignEditScreen) || this.text == null) {
            return;
        }
        SignBlockEntity sign = ((AbstractSignEditScreenAccessor)event.screen).meteor$getSign();
        this.queue.add(new ServerboundSignUpdatePacket(sign.getBlockPos(), true, this.text[0], this.text[1], this.text[2], this.text[3]));
        event.cancel();
    }
}
