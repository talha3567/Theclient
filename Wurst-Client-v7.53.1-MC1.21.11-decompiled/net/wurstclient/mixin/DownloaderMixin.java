package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.class_320;
import net.minecraft.class_4844;
import net.minecraft.class_9028;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={class_9028.class})
public abstract class DownloaderMixin
implements AutoCloseable {
    @Shadow
    @Final
    private Path field_47573;

    @WrapOperation(method={"method_55485"}, at={@At(value="INVOKE", target="Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;", ordinal=0, remap=false)}, remap=false)
    private Path wrapResolve(Path instance, String filename, Operation<Path> original) {
        Path result = (Path)original.call(new Object[]{instance, filename});
        if (result == null || !result.getParent().equals(this.field_47573)) {
            return result;
        }
        class_320 session = WurstClient.MC.method_1548();
        UUID uuid = session.method_44717();
        if (uuid == null) {
            uuid = class_4844.method_43344((String)session.method_1676());
        }
        return result.getParent().resolve(uuid.toString()).resolve(result.getFileName());
    }
}
