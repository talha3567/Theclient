package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface IEntityRenderState {
    @Nullable(value="EntityCulling mod can prevent the code that sets the entity from running")
    public @Nullable(value="EntityCulling mod can prevent the code that sets the entity from running") Entity meteor$getEntity();

    public void meteor$setEntity(Entity var1);
}
