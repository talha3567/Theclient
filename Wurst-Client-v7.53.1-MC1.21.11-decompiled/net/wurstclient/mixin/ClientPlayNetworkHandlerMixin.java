package net.wurstclient.mixin;

import net.minecraft.class_2338;
import net.minecraft.class_2535;
import net.minecraft.class_2561;
import net.minecraft.class_2602;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2678;
import net.minecraft.class_310;
import net.minecraft.class_368;
import net.minecraft.class_370;
import net.minecraft.class_5250;
import net.minecraft.class_634;
import net.minecraft.class_6603;
import net.minecraft.class_7633;
import net.minecraft.class_8673;
import net.minecraft.class_8675;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_634.class})
public abstract class ClientPlayNetworkHandlerMixin
extends class_8673
implements class_7633,
class_2602 {
    private ClientPlayNetworkHandlerMixin(WurstClient wurst, class_310 client, class_2535 connection, class_8675 connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method={"method_11120"}, at={@At(value="TAIL")})
    public void onOnGameJoin(class_2678 packet, CallbackInfo ci) {
        WurstClient wurst = WurstClient.INSTANCE;
        if (!wurst.isEnabled()) {
            return;
        }
        if (!packet.comp_2200()) {
            this.field_45588.method_1566().field_2240.removeIf(toast -> toast.method_1987() == class_370.class_9037.field_47589);
            return;
        }
        class_5250 title = class_2561.method_43470((String)("\u00a7c[\u00a76Wurst\u00a7c]\u00a7r " + wurst.translate("toast.wurst.nochatreports.unsafe_server.title", new Object[0])));
        class_5250 message = class_2561.method_43470((String)wurst.translate("toast.wurst.nochatreports.unsafe_server.message", new Object[0]));
        class_370 systemToast = class_370.method_29047((class_310)this.field_45588, (class_370.class_9037)class_370.class_9037.field_47589, (class_2561)title, (class_2561)message);
        this.field_45588.method_1566().method_1999((class_368)systemToast);
    }

    @Inject(method={"method_38539"}, at={@At(value="TAIL")})
    private void onLoadChunk(int x, int z, class_6603 chunkData, CallbackInfo ci) {
        WurstClient.INSTANCE.getHax().newChunksHack.afterLoadChunk(x, z);
    }

    @Inject(method={"method_11136"}, at={@At(value="TAIL")})
    private void onOnBlockUpdate(class_2626 packet, CallbackInfo ci) {
        WurstClient.INSTANCE.getHax().newChunksHack.afterUpdateBlock(packet.method_11309());
    }

    @Inject(method={"method_11100"}, at={@At(value="TAIL")})
    private void onOnChunkDeltaUpdate(class_2637 packet, CallbackInfo ci) {
        packet.method_30621((pos, state) -> WurstClient.INSTANCE.getHax().newChunksHack.afterUpdateBlock((class_2338)pos));
    }
}
