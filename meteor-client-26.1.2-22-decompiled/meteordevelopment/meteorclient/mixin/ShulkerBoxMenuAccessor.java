package meteordevelopment.meteorclient.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ShulkerBoxMenu.class})
public interface ShulkerBoxMenuAccessor {
    @Accessor(value="container")
    public Container meteor$getContainer();
}
