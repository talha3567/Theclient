package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_310;
import net.minecraft.class_350;
import net.minecraft.class_4265;
import net.minecraft.class_459;
import net.minecraft.class_7417;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={class_459.class})
public abstract class ControlsListWidgetMixin
extends class_4265<class_459.class_461> {
    public ControlsListWidgetMixin(WurstClient wurst, class_310 client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    @WrapOperation(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_459;method_25321(Lnet/minecraft/class_350$class_351;)I", ordinal=1)})
    private int dontAddZoomEntry(class_459 instance, class_350.class_351<?> entry, Operation<Integer> original) {
        class_7417 class_74172;
        if (!(entry instanceof class_459.class_462)) {
            return (Integer)original.call(new Object[]{instance, entry});
        }
        class_459.class_462 kbEntry = (class_459.class_462)entry;
        class_2561 name = kbEntry.field_2741;
        if (name == null || !((class_74172 = name.method_10851()) instanceof class_2588)) {
            return (Integer)original.call(new Object[]{instance, entry});
        }
        class_2588 trContent = (class_2588)class_74172;
        if (!"key.wurst.zoom".equals(trContent.method_11022())) {
            return (Integer)original.call(new Object[]{instance, entry});
        }
        return 0;
    }
}
