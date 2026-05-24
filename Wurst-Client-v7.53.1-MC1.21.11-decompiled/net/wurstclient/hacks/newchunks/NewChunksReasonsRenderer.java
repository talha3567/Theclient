package net.wurstclient.hacks.newchunks;

import java.util.List;
import net.minecraft.class_1921;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_4588;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;

public final class NewChunksReasonsRenderer {
    private final SliderSetting drawDistance;

    public NewChunksReasonsRenderer(SliderSetting drawDistance) {
        this.drawDistance = drawDistance;
    }

    public void buildBuffer(class_4588 buffer, List<class_2338> reasons) {
        class_1923 camChunkPos = new class_1923(RenderUtils.getCameraBlockPos());
        RegionPos region = RegionPos.of(camChunkPos);
        int drawDistance = this.drawDistance.getValueI();
        for (class_2338 pos : reasons) {
            class_1923 chunkPos = new class_1923(pos);
            if (chunkPos.method_24022(camChunkPos) > drawDistance) continue;
            class_238 box = new class_238(pos).method_989((double)(-region.x()), 0.0, (double)(-region.z()));
            RenderUtils.drawSolidBox(buffer, box, -1);
        }
    }

    public class_1921 getLayer() {
        return WurstRenderLayers.ESP_QUADS;
    }
}
