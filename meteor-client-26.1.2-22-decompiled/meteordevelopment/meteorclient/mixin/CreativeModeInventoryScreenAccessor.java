package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={CreativeModeInventoryScreen.class})
public interface CreativeModeInventoryScreenAccessor {
    @Accessor(value="selectedTab")
    public static CreativeModeTab meteor$getSelectedTab() {
        return null;
    }
}
