package meteordevelopment.meteorclient.mixin;

import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Direction.class})
public interface DirectionAccessor {
    @Accessor(value="BY_2D_DATA")
    public static Direction[] meteor$getHorizontal() {
        throw new AssertionError();
    }
}
