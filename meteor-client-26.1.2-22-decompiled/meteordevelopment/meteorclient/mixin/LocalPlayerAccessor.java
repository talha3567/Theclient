package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LocalPlayer.class})
public interface LocalPlayerAccessor {
    @Accessor(value="positionReminder")
    public void meteor$setPositionReminder(int var1);
}
