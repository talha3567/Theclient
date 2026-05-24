package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={MapTextureManager.class})
public interface MapTextureManagerAccessor {
    @Invoker(value="getOrCreateMapInstance")
    public MapTextureManager.MapInstance meteor$invokeGetOrCreateMapInstance(MapId var1, MapItemSavedData var2);
}
