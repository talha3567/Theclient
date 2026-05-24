package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import net.minecraft.class_11661;
import net.minecraft.class_11689;
import net.minecraft.class_12075;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_4587;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.NameTagsHack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={class_11689.class_12050.class})
public class LabelCommandRendererMixin {
    @Shadow
    @Final
    List<class_11661.class_11672> field_62987;
    @Shadow
    @Final
    List<class_11661.class_11672> field_62988;

    @WrapOperation(method={"method_74829"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_4587;method_22905(FFF)V")})
    private void wrapLabelScale(class_4587 matrices, float x, float y, float z, Operation<Void> original, class_4587 matrices2, @Nullable class_243 vec3d, int i, class_2561 text, boolean bl, int j, double d, class_12075 state) {
        NameTagsHack nameTags = WurstClient.INSTANCE.getHax().nameTagsHack;
        if (!nameTags.isEnabled()) {
            original.call(new Object[]{matrices, Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
            return;
        }
        float scale = 0.025f * nameTags.getScale();
        double distance = Math.sqrt(d);
        if (distance > 10.0) {
            scale = (float)((double)scale * (distance / 10.0));
        }
        original.call(new Object[]{matrices, Float.valueOf(scale), Float.valueOf(-scale), Float.valueOf(scale)});
    }

    @ModifyVariable(method={"method_74829"}, at=@At(value="HEAD"), argsOnly=true)
    private boolean forceNotSneaking(boolean notSneaking) {
        NameTagsHack nameTags = WurstClient.INSTANCE.getHax().nameTagsHack;
        return nameTags.isEnabled() || notSneaking;
    }

    @ModifyReceiver(at={@At(value="INVOKE", target="Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal=0)}, method={"method_74829"})
    private List<class_11661.class_11672> swapFirstList(List<class_11661.class_11672> originalList, Object labelCommand) {
        NameTagsHack nameTags = WurstClient.INSTANCE.getHax().nameTagsHack;
        if (nameTags.isEnabled() && nameTags.isSeeThrough() && originalList == this.field_62988) {
            return this.field_62987;
        }
        return originalList;
    }

    @ModifyReceiver(at={@At(value="INVOKE", target="Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal=1)}, method={"method_74829"})
    private List<class_11661.class_11672> swapSecondList(List<class_11661.class_11672> originalList, Object labelCommand) {
        NameTagsHack nameTags = WurstClient.INSTANCE.getHax().nameTagsHack;
        if (nameTags.isEnabled() && nameTags.isSeeThrough() && originalList == this.field_62987) {
            return this.field_62988;
        }
        return originalList;
    }
}
