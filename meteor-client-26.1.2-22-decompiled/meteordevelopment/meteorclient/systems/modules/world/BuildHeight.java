package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.BlockHitResultAccessor;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;

public class BuildHeight
extends Module {
    public BuildHeight() {
        super(Categories.World, "build-height", "Allows you to interact with objects at the build limit.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        Packet<?> packet = event.packet;
        if (!(packet instanceof ServerboundUseItemOnPacket)) {
            return;
        }
        ServerboundUseItemOnPacket p = (ServerboundUseItemOnPacket)packet;
        if (this.mc.level == null) {
            return;
        }
        if (p.getHitResult().getLocation().y >= (double)this.mc.level.getHeight() && p.getHitResult().getDirection() == Direction.UP) {
            ((BlockHitResultAccessor)p.getHitResult()).meteor$setDirection(Direction.DOWN);
        }
    }
}
