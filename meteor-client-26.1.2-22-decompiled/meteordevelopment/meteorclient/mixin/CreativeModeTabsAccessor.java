package meteordevelopment.meteorclient.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={CreativeModeTabs.class})
public interface CreativeModeTabsAccessor {
    @Accessor(value="INVENTORY")
    public static ResourceKey<CreativeModeTab> meteor$getInventory() {
        throw new AssertionError();
    }
}
