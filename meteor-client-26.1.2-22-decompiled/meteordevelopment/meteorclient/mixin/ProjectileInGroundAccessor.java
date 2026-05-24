package meteordevelopment.meteorclient.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={AbstractArrow.class})
public interface ProjectileInGroundAccessor {
    @Invoker(value="isInGround")
    public boolean meteor$invokeIsInGround();
}
