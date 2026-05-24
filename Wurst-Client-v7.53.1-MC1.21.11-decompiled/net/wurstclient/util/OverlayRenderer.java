package net.wurstclient.util;

import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.wurstclient.WurstClient;
import net.wurstclient.mixinterface.IMinecraftClient;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RenderUtils;

public final class OverlayRenderer {
    protected static final class_310 MC = WurstClient.MC;
    protected static final IMinecraftClient IMC = WurstClient.IMC;
    private float progress;
    private float prevProgress;
    private class_2338 prevPos;

    public void resetProgress() {
        this.progress = 0.0f;
        this.prevProgress = 0.0f;
        this.prevPos = null;
    }

    public void updateProgress() {
        this.prevProgress = this.progress;
        this.progress = OverlayRenderer.MC.field_1761.field_3715;
        if (this.progress < this.prevProgress) {
            this.prevProgress = this.progress;
        }
    }

    public void render(class_4587 matrixStack, float partialTicks, class_2338 pos) {
        if (pos == null) {
            return;
        }
        if (this.prevPos != null && !pos.equals((Object)this.prevPos)) {
            this.resetProgress();
        }
        this.prevPos = pos;
        boolean breaksInstantly = OverlayRenderer.MC.field_1724.method_31549().field_7477 || BlockUtils.getHardness(pos) >= 1.0f;
        float p = breaksInstantly ? 1.0f : class_3532.method_16439((float)partialTicks, (float)this.prevProgress, (float)this.progress);
        float red = p * 2.0f;
        float green = 2.0f - red;
        float[] rgb = new float[]{red, green, 0.0f};
        int quadColor = RenderUtils.toIntColor(rgb, 0.25f);
        int lineColor = RenderUtils.toIntColor(rgb, 0.5f);
        class_238 box = new class_238(pos);
        if (p < 1.0f) {
            box = box.method_1011((double)(1.0f - p) * 0.5);
        }
        RenderUtils.drawSolidBox(matrixStack, box, quadColor, false);
        RenderUtils.drawOutlinedBox(matrixStack, box, lineColor, false);
    }
}
