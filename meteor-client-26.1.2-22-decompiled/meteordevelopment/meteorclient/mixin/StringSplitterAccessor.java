package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.StringSplitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={StringSplitter.class})
public interface StringSplitterAccessor {
    @Accessor(value="widthProvider")
    public StringSplitter.WidthProvider meteor$getWidthProvider();
}
