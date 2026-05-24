package meteordevelopment.meteorclient.mixin;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientboundSetEntityMotionPacket.class})
public interface ClientboundSetEntityMotionPacketAccessor {
    @Mutable
    @Accessor(value="movement")
    public void meteor$setMovement(Vec3 var1);
}
