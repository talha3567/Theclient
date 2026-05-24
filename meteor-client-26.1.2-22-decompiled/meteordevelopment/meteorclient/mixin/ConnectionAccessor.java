package meteordevelopment.meteorclient.mixin;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Connection.class})
public interface ConnectionAccessor {
    @Accessor(value="channel")
    public Channel meteor$getChannel();
}
