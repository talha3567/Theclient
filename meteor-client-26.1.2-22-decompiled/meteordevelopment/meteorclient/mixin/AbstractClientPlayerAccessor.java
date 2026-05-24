package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={AbstractClientPlayer.class})
public interface AbstractClientPlayerAccessor {
    @Accessor(value="playerInfo")
    public void meteor$setPlayerInfo(PlayerInfo var1);
}
