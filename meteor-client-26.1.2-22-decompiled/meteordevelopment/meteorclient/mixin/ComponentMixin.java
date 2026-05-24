package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={Component.class})
public interface ComponentMixin
extends IComponent {
    @Override
    default public void meteor$invalidateCache() {
    }
}
