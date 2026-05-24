package net.wurstclient.util;

import java.util.List;
import net.minecraft.class_11244;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_1935;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2791;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_8030;
import net.wurstclient.WurstClient;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.util.CustomQuadRenderState;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.Rotation;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class RenderUtils
extends Enum<RenderUtils> {
    private static final /* synthetic */ RenderUtils[] $VALUES;

    public static RenderUtils[] values() {
        return (RenderUtils[])$VALUES.clone();
    }

    public static RenderUtils valueOf(String name) {
        return Enum.valueOf(RenderUtils.class, name);
    }

    public static void applyRegionalRenderOffset(class_4587 matrixStack) {
        RenderUtils.applyRegionalRenderOffset(matrixStack, RenderUtils.getCameraRegion());
    }

    public static void applyRegionalRenderOffset(class_4587 matrixStack, class_2791 chunk) {
        RenderUtils.applyRegionalRenderOffset(matrixStack, RegionPos.of(chunk.method_12004()));
    }

    public static void applyRegionalRenderOffset(class_4587 matrixStack, RegionPos region) {
        class_243 offset = region.toVec3d().method_1020(RenderUtils.getCameraPos());
        matrixStack.method_22904(offset.field_1352, offset.field_1351, offset.field_1350);
    }

    public static void applyRenderOffset(class_4587 matrixStack) {
        class_243 camPos = RenderUtils.getCameraPos();
        matrixStack.method_22904(-camPos.field_1352, -camPos.field_1351, -camPos.field_1350);
    }

    public static class_243 getCameraPos() {
        class_4184 camera = WurstClient.MC.field_1773.method_19418();
        if (camera == null) {
            return class_243.field_1353;
        }
        return camera.method_71156();
    }

    public static Rotation getCameraRotation() {
        class_4184 camera = WurstClient.MC.field_1773.method_19418();
        if (camera == null) {
            return new Rotation(0.0f, 0.0f);
        }
        return new Rotation(camera.method_19330(), camera.method_19329());
    }

    public static class_2338 getCameraBlockPos() {
        class_4184 camera = WurstClient.MC.field_1773.method_19418();
        if (camera == null) {
            return class_2338.field_10980;
        }
        return camera.method_19328();
    }

    public static RegionPos getCameraRegion() {
        return RegionPos.of(RenderUtils.getCameraBlockPos());
    }

    public static class_4597.class_4598 getVCP() {
        return WurstClient.MC.method_22940().method_23000();
    }

    public static float[] getRainbowColor() {
        float x = (float)(System.currentTimeMillis() % 2000L) / 1000.0f;
        float pi = (float)Math.PI;
        float[] rainbow = new float[]{0.5f + 0.5f * class_3532.method_15374((double)(x * pi)), 0.5f + 0.5f * class_3532.method_15374((double)((x + 1.3333334f) * pi)), 0.5f + 0.5f * class_3532.method_15374((double)((x + 2.6666667f) * pi))};
        return rainbow;
    }

    public static int toIntColor(float[] rgb, float opacity) {
        return (int)(class_3532.method_15363((float)opacity, (float)0.0f, (float)1.0f) * 255.0f) << 24 | (int)(class_3532.method_15363((float)rgb[0], (float)0.0f, (float)1.0f) * 255.0f) << 16 | (int)(class_3532.method_15363((float)rgb[1], (float)0.0f, (float)1.0f) * 255.0f) << 8 | (int)(class_3532.method_15363((float)rgb[2], (float)0.0f, (float)1.0f) * 255.0f);
    }

    public static void drawLine(class_4587 matrices, class_243 start, class_243 end, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 offset = RenderUtils.getCameraPos().method_22882();
        RenderUtils.drawLine(matrices, buffer, start.method_1019(offset), end.method_1019(offset), color);
        vcp.method_22994(layer);
    }

    private static class_243 getTracerOrigin(float partialTicks) {
        return RenderUtils.getCameraRotation().toLookVec().method_1021(10.0);
    }

    public static void drawTracer(class_4587 matrices, float partialTicks, class_243 end, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 start = RenderUtils.getTracerOrigin(partialTicks);
        class_243 offset = RenderUtils.getCameraPos().method_22882();
        RenderUtils.drawLine(matrices, buffer, start, end.method_1019(offset), color);
        vcp.method_22994(layer);
    }

    public static void drawTracers(class_4587 matrices, float partialTicks, List<class_243> ends, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 start = RenderUtils.getTracerOrigin(partialTicks);
        class_243 offset = RenderUtils.getCameraPos().method_22882();
        for (class_243 end : ends) {
            RenderUtils.drawLine(matrices, buffer, start, end.method_1019(offset), color);
        }
        vcp.method_22994(layer);
    }

    public static void drawTracers(class_4587 matrices, float partialTicks, List<ColoredPoint> ends, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 start = RenderUtils.getTracerOrigin(partialTicks);
        class_243 offset = RenderUtils.getCameraPos().method_22882();
        for (ColoredPoint end : ends) {
            RenderUtils.drawLine(matrices, buffer, start, end.point().method_1019(offset), end.color());
        }
        vcp.method_22994(layer);
    }

    public static void drawLine(class_4587 matrices, class_4588 buffer, class_243 start, class_243 end, int color) {
        class_4587.class_4665 entry = matrices.method_23760();
        float x1 = (float)start.field_1352;
        float y1 = (float)start.field_1351;
        float z1 = (float)start.field_1350;
        float x2 = (float)end.field_1352;
        float y2 = (float)end.field_1351;
        float z2 = (float)end.field_1350;
        RenderUtils.drawLine(entry, buffer, x1, y1, z1, x2, y2, z2, color);
    }

    public static void drawLine(class_4587.class_4665 entry, class_4588 buffer, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        Vector3f normal = new Vector3f(x2, y2, z2).sub(x1, y1, z1).normalize();
        buffer.method_56824(entry, x1, y1, z1).method_39415(color).method_61959(entry, normal).method_75298(2.0f);
        float t = new Vector3f(x1, y1, z1).negate().dot((Vector3fc)normal);
        float length = new Vector3f(x2, y2, z2).sub(x1, y1, z1).length();
        if (t > 0.0f && t < length) {
            Vector3f closeToCam = new Vector3f((Vector3fc)normal).mul(t).add(x1, y1, z1);
            buffer.method_61032(entry, closeToCam).method_39415(color).method_61959(entry, normal).method_75298(2.0f);
            buffer.method_61032(entry, closeToCam).method_39415(color).method_61959(entry, normal).method_75298(2.0f);
        }
        buffer.method_56824(entry, x2, y2, z2).method_39415(color).method_61959(entry, normal).method_75298(2.0f);
    }

    public static void drawLine(class_4588 buffer, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        Vector3f n = new Vector3f(x2, y2, z2).sub(x1, y1, z1).normalize();
        buffer.method_22912(x1, y1, z1).method_39415(color).method_22914(n.x, n.y, n.z).method_75298(2.0f);
        buffer.method_22912(x2, y2, z2).method_39415(color).method_22914(n.x, n.y, n.z).method_75298(2.0f);
    }

    public static void drawCurvedLine(class_4587 matrices, List<class_243> points, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 offset = RenderUtils.getCameraPos().method_22882();
        List<class_243> points2 = points.stream().map(v -> v.method_1019(offset)).toList();
        RenderUtils.drawCurvedLine(matrices, buffer, points2, color);
        vcp.method_22994(layer);
    }

    public static void drawCurvedLine(class_4587 matrices, class_4588 buffer, List<class_243> points, int color) {
        if (points.size() < 2) {
            return;
        }
        class_4587.class_4665 entry = matrices.method_23760();
        for (int i = 1; i < points.size(); ++i) {
            Vector3f prev = points.get(i - 1).method_46409();
            Vector3f current = points.get(i).method_46409();
            Vector3f normal = new Vector3f((Vector3fc)current).sub((Vector3fc)prev).normalize();
            buffer.method_61032(entry, prev).method_39415(color).method_61959(entry, normal).method_75298(2.0f);
            buffer.method_61032(entry, current).method_39415(color).method_61959(entry, normal).method_75298(2.0f);
        }
    }

    public static void drawSolidBox(class_4587 matrices, class_238 box, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getQuads(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        RenderUtils.drawSolidBox(matrices, buffer, box.method_997(RenderUtils.getCameraPos().method_22882()), color);
        vcp.method_22994(layer);
    }

    public static void drawSolidBoxes(class_4587 matrices, List<class_238> boxes, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getQuads(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 camOffset = RenderUtils.getCameraPos().method_22882();
        for (class_238 box : boxes) {
            RenderUtils.drawSolidBox(matrices, buffer, box.method_997(camOffset), color);
        }
        vcp.method_22994(layer);
    }

    public static void drawSolidBoxes(class_4587 matrices, List<ColoredBox> boxes, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getQuads(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 camOffset = RenderUtils.getCameraPos().method_22882();
        for (ColoredBox box : boxes) {
            RenderUtils.drawSolidBox(matrices, buffer, box.box().method_997(camOffset), box.color());
        }
        vcp.method_22994(layer);
    }

    public static void drawSolidBox(class_4588 buffer, class_238 box, int color) {
        RenderUtils.drawSolidBox(new class_4587(), buffer, box, color);
    }

    public static void drawSolidBox(class_4587 matrices, class_4588 buffer, class_238 box, int color) {
        class_4587.class_4665 entry = matrices.method_23760();
        float x1 = (float)box.field_1323;
        float y1 = (float)box.field_1322;
        float z1 = (float)box.field_1321;
        float x2 = (float)box.field_1320;
        float y2 = (float)box.field_1325;
        float z2 = (float)box.field_1324;
        buffer.method_56824(entry, x1, y1, z1).method_39415(color);
        buffer.method_56824(entry, x2, y1, z1).method_39415(color);
        buffer.method_56824(entry, x2, y1, z2).method_39415(color);
        buffer.method_56824(entry, x1, y1, z2).method_39415(color);
        buffer.method_56824(entry, x1, y2, z1).method_39415(color);
        buffer.method_56824(entry, x1, y2, z2).method_39415(color);
        buffer.method_56824(entry, x2, y2, z2).method_39415(color);
        buffer.method_56824(entry, x2, y2, z1).method_39415(color);
        buffer.method_56824(entry, x1, y1, z1).method_39415(color);
        buffer.method_56824(entry, x1, y2, z1).method_39415(color);
        buffer.method_56824(entry, x2, y2, z1).method_39415(color);
        buffer.method_56824(entry, x2, y1, z1).method_39415(color);
        buffer.method_56824(entry, x2, y1, z1).method_39415(color);
        buffer.method_56824(entry, x2, y2, z1).method_39415(color);
        buffer.method_56824(entry, x2, y2, z2).method_39415(color);
        buffer.method_56824(entry, x2, y1, z2).method_39415(color);
        buffer.method_56824(entry, x1, y1, z2).method_39415(color);
        buffer.method_56824(entry, x2, y1, z2).method_39415(color);
        buffer.method_56824(entry, x2, y2, z2).method_39415(color);
        buffer.method_56824(entry, x1, y2, z2).method_39415(color);
        buffer.method_56824(entry, x1, y1, z1).method_39415(color);
        buffer.method_56824(entry, x1, y1, z2).method_39415(color);
        buffer.method_56824(entry, x1, y2, z2).method_39415(color);
        buffer.method_56824(entry, x1, y2, z1).method_39415(color);
    }

    public static void drawOutlinedBox(class_4587 matrices, class_238 box, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        RenderUtils.drawOutlinedBox(matrices, buffer, box.method_997(RenderUtils.getCameraPos().method_22882()), color);
        vcp.method_22994(layer);
    }

    public static void drawOutlinedBoxes(class_4587 matrices, List<class_238> boxes, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 camOffset = RenderUtils.getCameraPos().method_22882();
        for (class_238 box : boxes) {
            RenderUtils.drawOutlinedBox(matrices, buffer, box.method_997(camOffset), color);
        }
        vcp.method_22994(layer);
    }

    public static void drawOutlinedBoxes(class_4587 matrices, List<ColoredBox> boxes, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 camOffset = RenderUtils.getCameraPos().method_22882();
        for (ColoredBox box : boxes) {
            RenderUtils.drawOutlinedBox(matrices, buffer, box.box().method_997(camOffset), box.color());
        }
        vcp.method_22994(layer);
    }

    public static void drawOutlinedBox(class_4588 buffer, class_238 box, int color) {
        RenderUtils.drawOutlinedBox(new class_4587(), buffer, box, color);
    }

    public static void drawOutlinedBox(class_4587 matrices, class_4588 buffer, class_238 box, int color) {
        class_4587.class_4665 entry = matrices.method_23760();
        float x1 = (float)box.field_1323;
        float y1 = (float)box.field_1322;
        float z1 = (float)box.field_1321;
        float x2 = (float)box.field_1320;
        float y2 = (float)box.field_1325;
        float z2 = (float)box.field_1324;
        buffer.method_56824(entry, x1, y1, z1).method_39415(color).method_60831(entry, 1.0f, 0.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z1).method_39415(color).method_60831(entry, 1.0f, 0.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y1, z1).method_39415(color).method_60831(entry, 0.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y1, z2).method_39415(color).method_60831(entry, 0.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z1).method_39415(color).method_60831(entry, 0.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z2).method_39415(color).method_60831(entry, 0.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y1, z2).method_39415(color).method_60831(entry, 1.0f, 0.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z2).method_39415(color).method_60831(entry, 1.0f, 0.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z1).method_39415(color).method_60831(entry, 1.0f, 0.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z1).method_39415(color).method_60831(entry, 1.0f, 0.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z1).method_39415(color).method_60831(entry, 0.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z2).method_39415(color).method_60831(entry, 0.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z1).method_39415(color).method_60831(entry, 0.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z2).method_39415(color).method_60831(entry, 0.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z2).method_39415(color).method_60831(entry, 1.0f, 0.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z2).method_39415(color).method_60831(entry, 1.0f, 0.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y1, z1).method_39415(color).method_60831(entry, 0.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z1).method_39415(color).method_60831(entry, 0.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z1).method_39415(color).method_60831(entry, 0.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z1).method_39415(color).method_60831(entry, 0.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y1, z2).method_39415(color).method_60831(entry, 0.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z2).method_39415(color).method_60831(entry, 0.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z2).method_39415(color).method_60831(entry, 0.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z2).method_39415(color).method_60831(entry, 0.0f, 1.0f, 0.0f).method_75298(2.0f);
    }

    public static void drawCrossBox(class_4587 matrices, class_238 box, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        RenderUtils.drawCrossBox(matrices, buffer, box.method_997(RenderUtils.getCameraPos().method_22882()), color);
        vcp.method_22994(layer);
    }

    public static void drawCrossBoxes(class_4587 matrices, List<class_238> boxes, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 camOffset = RenderUtils.getCameraPos().method_22882();
        for (class_238 box : boxes) {
            RenderUtils.drawCrossBox(matrices, buffer, box.method_997(camOffset), color);
        }
        vcp.method_22994(layer);
    }

    public static void drawCrossBoxes(class_4587 matrices, List<ColoredBox> boxes, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 camOffset = RenderUtils.getCameraPos().method_22882();
        for (ColoredBox box : boxes) {
            RenderUtils.drawCrossBox(matrices, buffer, box.box().method_997(camOffset), box.color());
        }
        vcp.method_22994(layer);
    }

    public static void drawCrossBox(class_4588 buffer, class_238 box, int color) {
        RenderUtils.drawCrossBox(new class_4587(), buffer, box, color);
    }

    public static void drawCrossBox(class_4587 matrices, class_4588 buffer, class_238 box, int color) {
        class_4587.class_4665 entry = matrices.method_23760();
        float x1 = (float)box.field_1323;
        float y1 = (float)box.field_1322;
        float z1 = (float)box.field_1321;
        float x2 = (float)box.field_1320;
        float y2 = (float)box.field_1325;
        float z2 = (float)box.field_1324;
        buffer.method_56824(entry, x1, y1, z1).method_39415(color).method_60831(entry, 1.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z1).method_39415(color).method_60831(entry, 1.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z1).method_39415(color).method_60831(entry, -1.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z1).method_39415(color).method_60831(entry, -1.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z1).method_39415(color).method_60831(entry, 0.0f, 1.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z2).method_39415(color).method_60831(entry, 0.0f, 1.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z2).method_39415(color).method_60831(entry, 0.0f, 1.0f, -1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z1).method_39415(color).method_60831(entry, 0.0f, 1.0f, -1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z2).method_39415(color).method_60831(entry, -1.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z2).method_39415(color).method_60831(entry, -1.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y1, z2).method_39415(color).method_60831(entry, 1.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z2).method_39415(color).method_60831(entry, 1.0f, 1.0f, 0.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y1, z2).method_39415(color).method_60831(entry, 0.0f, 1.0f, -1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z1).method_39415(color).method_60831(entry, 0.0f, 1.0f, -1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y1, z1).method_39415(color).method_60831(entry, 0.0f, 1.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z2).method_39415(color).method_60831(entry, 0.0f, 1.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z2).method_39415(color).method_60831(entry, 1.0f, 0.0f, -1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z1).method_39415(color).method_60831(entry, 1.0f, 0.0f, -1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y2, z1).method_39415(color).method_60831(entry, 1.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y2, z2).method_39415(color).method_60831(entry, 1.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z1).method_39415(color).method_60831(entry, -1.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y1, z2).method_39415(color).method_60831(entry, -1.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x1, y1, z1).method_39415(color).method_60831(entry, 1.0f, 0.0f, 1.0f).method_75298(2.0f);
        buffer.method_56824(entry, x2, y1, z2).method_39415(color).method_60831(entry, 1.0f, 0.0f, 1.0f).method_75298(2.0f);
    }

    public static void drawNode(class_4587 matrices, class_238 box, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        RenderUtils.drawNode(matrices, buffer, box.method_997(RenderUtils.getCameraPos().method_22882()), color);
        vcp.method_22994(layer);
    }

    public static void drawNodes(class_4587 matrices, List<class_238> boxes, int color, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 camOffset = RenderUtils.getCameraPos().method_22882();
        for (class_238 box : boxes) {
            RenderUtils.drawNode(matrices, buffer, box.method_997(camOffset), color);
        }
        vcp.method_22994(layer);
    }

    public static void drawNodes(class_4587 matrices, List<ColoredBox> boxes, boolean depthTest) {
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_1921 layer = WurstRenderLayers.getLines(depthTest);
        class_4588 buffer = vcp.method_73477(layer);
        class_243 camOffset = RenderUtils.getCameraPos().method_22882();
        for (ColoredBox box : boxes) {
            RenderUtils.drawNode(matrices, buffer, box.box().method_997(camOffset), box.color());
        }
        vcp.method_22994(layer);
    }

    public static void drawNode(class_4588 buffer, class_238 box, int color) {
        RenderUtils.drawNode(new class_4587(), buffer, box, color);
    }

    public static void drawNode(class_4587 matrices, class_4588 buffer, class_238 box, int color) {
        class_4587.class_4665 entry = matrices.method_23760();
        float x1 = (float)box.field_1323;
        float y1 = (float)box.field_1322;
        float z1 = (float)box.field_1321;
        float x2 = (float)box.field_1320;
        float y2 = (float)box.field_1325;
        float z2 = (float)box.field_1324;
        float x3 = (x1 + x2) / 2.0f;
        float y3 = (y1 + y2) / 2.0f;
        float z3 = (z1 + z2) / 2.0f;
        RenderUtils.drawLine(entry, buffer, x3, y3, z2, x1, y3, z3, color);
        RenderUtils.drawLine(entry, buffer, x1, y3, z3, x3, y3, z1, color);
        RenderUtils.drawLine(entry, buffer, x3, y3, z1, x2, y3, z3, color);
        RenderUtils.drawLine(entry, buffer, x2, y3, z3, x3, y3, z2, color);
        RenderUtils.drawLine(entry, buffer, x3, y2, z3, x2, y3, z3, color);
        RenderUtils.drawLine(entry, buffer, x3, y2, z3, x1, y3, z3, color);
        RenderUtils.drawLine(entry, buffer, x3, y2, z3, x3, y3, z1, color);
        RenderUtils.drawLine(entry, buffer, x3, y2, z3, x3, y3, z2, color);
        RenderUtils.drawLine(entry, buffer, x3, y1, z3, x2, y3, z3, color);
        RenderUtils.drawLine(entry, buffer, x3, y1, z3, x1, y3, z3, color);
        RenderUtils.drawLine(entry, buffer, x3, y1, z3, x3, y3, z1, color);
        RenderUtils.drawLine(entry, buffer, x3, y1, z3, x3, y3, z2, color);
    }

    public static void drawArrow(class_4587 matrices, class_4588 buffer, class_2338 from, class_2338 to, RegionPos region, int color) {
        class_243 fromVec = from.method_46558().method_1023((double)region.x(), 0.0, (double)region.z());
        class_243 toVec = to.method_46558().method_1023((double)region.x(), 0.0, (double)region.z());
        RenderUtils.drawArrow(matrices, buffer, fromVec, toVec, color, 0.0625f);
    }

    public static void drawArrow(class_4588 buffer, class_243 from, class_243 to, int color, float headSize) {
        RenderUtils.drawArrow(new class_4587(), buffer, from, to, color, headSize);
    }

    public static void drawArrow(class_4587 matrices, class_4588 buffer, class_243 from, class_243 to, int color, float headSize) {
        matrices.method_22903();
        class_4587.class_4665 entry = matrices.method_23760();
        Matrix4f matrix = entry.method_23761();
        RenderUtils.drawLine(matrices, buffer, from, to, color);
        matrices.method_61958(to);
        matrices.method_22905(headSize, headSize, headSize);
        double xDiff = to.field_1352 - from.field_1352;
        double yDiff = to.field_1351 - from.field_1351;
        double zDiff = to.field_1350 - from.field_1350;
        float xAngle = (float)(Math.atan2(yDiff, -zDiff) + Math.toRadians(90.0));
        matrix.rotate(xAngle, (Vector3fc)new Vector3f(1.0f, 0.0f, 0.0f));
        double yzDiff = Math.sqrt(yDiff * yDiff + zDiff * zDiff);
        float zAngle = (float)Math.atan2(xDiff, yzDiff);
        matrix.rotate(zAngle, (Vector3fc)new Vector3f(0.0f, 0.0f, 1.0f));
        RenderUtils.drawLine(entry, buffer, 0.0f, 2.0f, 1.0f, -1.0f, 2.0f, 0.0f, color);
        RenderUtils.drawLine(entry, buffer, -1.0f, 2.0f, 0.0f, 0.0f, 2.0f, -1.0f, color);
        RenderUtils.drawLine(entry, buffer, 0.0f, 2.0f, -1.0f, 1.0f, 2.0f, 0.0f, color);
        RenderUtils.drawLine(entry, buffer, 1.0f, 2.0f, 0.0f, 0.0f, 2.0f, 1.0f, color);
        RenderUtils.drawLine(entry, buffer, 1.0f, 2.0f, 0.0f, -1.0f, 2.0f, 0.0f, color);
        RenderUtils.drawLine(entry, buffer, 0.0f, 2.0f, 1.0f, 0.0f, 2.0f, -1.0f, color);
        RenderUtils.drawLine(entry, buffer, 0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 0.0f, color);
        RenderUtils.drawLine(entry, buffer, 0.0f, 0.0f, 0.0f, -1.0f, 2.0f, 0.0f, color);
        RenderUtils.drawLine(entry, buffer, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, -1.0f, color);
        RenderUtils.drawLine(entry, buffer, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 1.0f, color);
        matrices.method_22909();
    }

    public static void drawItem(class_332 context, class_1799 stack, int x, int y, boolean large) {
        Matrix3x2fStack matrixStack = context.method_51448();
        matrixStack.pushMatrix();
        matrixStack.translate((float)x, (float)y);
        if (large) {
            matrixStack.scale(1.5f, 1.5f);
        } else {
            matrixStack.scale(0.75f, 0.75f);
        }
        class_1799 renderStack = stack.method_7960() || stack.method_7909() == null ? new class_1799((class_1935)class_2246.field_10219) : stack;
        context.method_51427(renderStack, 0, 0);
        matrixStack.popMatrix();
        if (stack.method_7960()) {
            context.field_59826.method_71067();
            matrixStack.pushMatrix();
            matrixStack.translate((float)x, (float)y);
            if (large) {
                matrixStack.scale(2.0f, 2.0f);
            }
            class_327 tr = WurstClient.MC.field_1772;
            context.method_51433(tr, "?", 3, 2, -986896, true);
            matrixStack.popMatrix();
        }
    }

    public static void fill2D(class_332 context, float x1, float y1, float x2, float y2, int color) {
        int scale = WurstClient.MC.method_22683().method_4495();
        int xs1 = (int)(x1 * (float)scale);
        int ys1 = (int)(y1 * (float)scale);
        int xs2 = (int)(x2 * (float)scale);
        int ys2 = (int)(y2 * (float)scale);
        context.method_51448().pushMatrix();
        context.method_51448().scale(1.0f / (float)scale);
        context.method_25294(xs1, ys1, xs2, ys2, color);
        context.method_51448().popMatrix();
    }

    public static void fillQuads2D(class_332 context, float[][] vertices, int color) {
        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)context.method_51448());
        class_8030 scissor = context.field_44659.method_70863();
        for (int i = 0; i < vertices.length - 3 && i + 3 < vertices.length; i += 4) {
            float x1 = vertices[i][0];
            float y1 = vertices[i][1];
            float x2 = vertices[i + 1][0];
            float y2 = vertices[i + 1][1];
            float x3 = vertices[i + 2][0];
            float y3 = vertices[i + 2][1];
            float x4 = vertices[i + 3][0];
            float y4 = vertices[i + 3][1];
            context.field_59826.method_70919((class_11244)new CustomQuadRenderState(pose, x1, y1, x2, y2, x3, y3, x4, y4, color, scissor));
        }
    }

    public static void fillTriangle2D(class_332 context, float[][] vertices, int color) {
        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)context.method_51448());
        class_8030 scissor = context.field_44659.method_70863();
        for (int i = 0; i < vertices.length - 2 && i + 2 < vertices.length; i += 3) {
            float x1 = vertices[i][0];
            float y1 = vertices[i][1];
            float x2 = vertices[i + 1][0];
            float y2 = vertices[i + 1][1];
            float x3 = vertices[i + 2][0];
            float y3 = vertices[i + 2][1];
            context.field_59826.method_70919((class_11244)new CustomQuadRenderState(pose, x1, y1, x2, y2, x3, y3, x3, y3, color, scissor));
        }
    }

    public static void drawLine2D(class_332 context, float x1, float y1, float x2, float y2, int color) {
        int scale = WurstClient.MC.method_22683().method_4495();
        float x = x1 * (float)scale;
        float y = y1 * (float)scale;
        float w = (x2 - x1) * (float)scale;
        float h = (y2 - y1) * (float)scale;
        float angle = (float)class_3532.method_15349((double)h, (double)w);
        int length = Math.round(class_3532.method_15355((float)(w * w + h * h)));
        context.method_51448().pushMatrix();
        context.method_51448().scale(1.0f / (float)scale);
        context.method_51448().translate(x, y);
        context.method_51448().rotate(angle);
        context.method_51448().translate(-0.5f, -0.5f);
        context.method_51738(0, length - 1, 0, color);
        context.method_51448().popMatrix();
    }

    public static void drawBorder2D(class_332 context, float x1, float y1, float x2, float y2, int color) {
        int scale = WurstClient.MC.method_22683().method_4495();
        int x = (int)(x1 * (float)scale);
        int y = (int)(y1 * (float)scale);
        int w = (int)((x2 - x1) * (float)scale);
        int h = (int)((y2 - y1) * (float)scale);
        context.method_51448().pushMatrix();
        context.method_51448().scale(1.0f / (float)scale);
        context.method_51738(x, x + w - 1, y, color);
        context.method_51738(x, x + w - 1, y + h - 1, color);
        context.method_51742(x, y + 1, y + h - 1, color);
        context.method_51742(x + w - 1, y + 1, y + h - 1, color);
        context.method_51448().popMatrix();
    }

    public static void drawLineStrip2D(class_332 context, float[][] vertices, int color) {
        if (vertices.length < 2) {
            return;
        }
        for (int i = 1; i < vertices.length; ++i) {
            RenderUtils.drawLine2D(context, vertices[i - 1][0], vertices[i - 1][1], vertices[i][0], vertices[i][1], color);
        }
        RenderUtils.drawLine2D(context, vertices[vertices.length - 1][0], vertices[vertices.length - 1][1], vertices[0][0], vertices[0][1], color);
    }

    public static void drawBoxShadow2D(class_332 context, int x1, int y1, int x2, int y2) {
        float[] acColor = WurstClient.INSTANCE.getGui().getAcColor();
        int outlineColor = RenderUtils.toIntColor(acColor, 0.5f);
        RenderUtils.drawBorder2D(context, x1, y1, x2, y2, outlineColor);
        float xs1 = x1 - 1;
        float xs2 = x2 + 1;
        float ys1 = y1 - 1;
        float ys2 = y2 + 1;
        int shadowColor1 = RenderUtils.toIntColor(acColor, 0.75f);
        int shadowColor2 = 0;
        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)context.method_51448());
        class_8030 scissor = context.field_44659.method_70863();
        context.field_59826.method_70919((class_11244)new CustomQuadRenderState(pose, x1, y1, x2, y1, xs2, ys1, xs1, ys1, shadowColor1, shadowColor1, shadowColor2, shadowColor2, scissor));
        context.field_59826.method_70919((class_11244)new CustomQuadRenderState(pose, xs1, ys1, xs1, ys2, x1, y2, x1, y1, shadowColor2, shadowColor2, shadowColor1, shadowColor1, scissor));
        context.field_59826.method_70919((class_11244)new CustomQuadRenderState(pose, x2, y1, x2, y2, xs2, ys2, xs2, ys1, shadowColor1, shadowColor1, shadowColor2, shadowColor2, scissor));
        context.field_59826.method_70919((class_11244)new CustomQuadRenderState(pose, x2, y2, x1, y2, xs1, ys2, xs2, ys2, shadowColor1, shadowColor1, shadowColor2, shadowColor2, scissor));
    }

    private static /* synthetic */ RenderUtils[] $values() {
        return new RenderUtils[0];
    }

    static {
        $VALUES = RenderUtils.$values();
    }

    public record ColoredPoint(class_243 point, int color) {
    }

    public record ColoredBox(class_238 box, int color) {
    }
}
