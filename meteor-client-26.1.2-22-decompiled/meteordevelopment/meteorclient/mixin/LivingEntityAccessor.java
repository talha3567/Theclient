package meteordevelopment.meteorclient.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={LivingEntity.class})
public interface LivingEntityAccessor {
    @Invoker(value="jumpInLiquid")
    public void meteor$swimUpwards(TagKey<Fluid> var1);

    @Accessor(value="jumping")
    public boolean meteor$isJumping();

    @Accessor(value="noJumpDelay")
    public int meteor$getJumpCooldown();

    @Accessor(value="noJumpDelay")
    public void meteor$setJumpCooldown(int var1);
}
