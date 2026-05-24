package meteordevelopment.meteorclient.mixin;

import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={CrossbowItem.class})
public interface CrossbowItemAccessor {
    @Invoker(value="getShootingPower")
    public static float meteor$getSpeed(ChargedProjectiles itemStack) {
        return 0.0f;
    }
}
