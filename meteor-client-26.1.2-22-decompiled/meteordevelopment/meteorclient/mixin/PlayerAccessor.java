package meteordevelopment.meteorclient.mixin;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Player.class})
public interface PlayerAccessor {
    @Invoker(value="canPlayerFitWithinBlocksAndEntitiesWhen")
    public boolean meteor$canChangeIntoPose(Pose var1);
}
