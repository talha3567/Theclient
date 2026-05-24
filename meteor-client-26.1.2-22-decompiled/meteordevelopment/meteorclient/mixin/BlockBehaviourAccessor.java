package meteordevelopment.meteorclient.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={BlockBehaviour.class})
public interface BlockBehaviourAccessor {
    @Accessor(value="hasCollision")
    public boolean meteor$isHasCollision();
}
