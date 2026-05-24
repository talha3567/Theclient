package net.wurstclient.mixin;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.class_310;
import net.minecraft.class_7172;
import net.wurstclient.mixinterface.ISimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_7172.class})
public class SimpleOptionMixin<T>
implements ISimpleOption<T> {
    @Shadow
    T field_37868;
    @Shadow
    @Final
    private Consumer<T> field_37867;

    @Override
    public void forceSetValue(T newValue) {
        if (!class_310.method_1551().method_22108()) {
            this.field_37868 = newValue;
            return;
        }
        if (!Objects.equals(this.field_37868, newValue)) {
            this.field_37868 = newValue;
            this.field_37867.accept(this.field_37868);
        }
    }
}
