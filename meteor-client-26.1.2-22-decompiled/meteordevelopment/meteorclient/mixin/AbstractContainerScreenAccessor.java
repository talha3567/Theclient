package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={AbstractContainerScreen.class})
public interface AbstractContainerScreenAccessor {
    @Accessor(value="hoveredSlot")
    public Slot meteor$getHoveredSlot();
}
