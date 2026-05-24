package meteordevelopment.meteorclient.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={KeyMapping.class})
public interface KeyMappingAccessor {
    @Accessor(value="ALL")
    public static Map<String, KeyMapping> getKeysById() {
        return null;
    }

    @Accessor(value="key")
    public InputConstants.Key meteor$getKey();

    @Accessor(value="clickCount")
    public int meteor$getClickCount();

    @Accessor(value="clickCount")
    public void meteor$setClickCount(int var1);

    @Invoker(value="release")
    public void meteor$invokeRelease();
}
