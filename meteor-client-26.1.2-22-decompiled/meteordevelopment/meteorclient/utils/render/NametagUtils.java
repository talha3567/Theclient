package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Zoom;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector4f;

public class NametagUtils {
    private static final Vector4f vec4 = new Vector4f();
    private static final Vector4f mmMat4 = new Vector4f();
    private static final Vector4f pmMat4 = new Vector4f();
    private static final Vector3d camera = new Vector3d();
    private static final Vector3d cameraNegated = new Vector3d();
    private static final Matrix4f model = new Matrix4f();
    private static final Matrix4f projection = new Matrix4f();
    private static double windowScale;
    public static double scale;

    private NametagUtils() {
    }

    public static void onRender(Matrix4fc modelView) {
        model.set(modelView);
        projection.set((Matrix4fc)RenderUtils.projection);
        Utils.set(camera, MeteorClient.mc.gameRenderer.getMainCamera().position());
        cameraNegated.set((Vector3dc)camera);
        cameraNegated.negate();
        windowScale = MeteorClient.mc.getWindow().calculateScale(1, false);
    }

    public static boolean to2D(Vector3d pos, double scale) {
        return NametagUtils.to2D(pos, scale, true);
    }

    public static boolean to2D(Vector3d pos, double scale, boolean distanceScaling) {
        return NametagUtils.to2D(pos, scale, distanceScaling, false);
    }

    public static boolean to2D(Vector3d pos, double scale, boolean distanceScaling, boolean allowBehind) {
        boolean behind;
        Zoom zoom = Modules.get().get(Zoom.class);
        NametagUtils.scale = scale * zoom.getScaling();
        if (distanceScaling) {
            NametagUtils.scale *= NametagUtils.getScale(pos);
        }
        vec4.set(NametagUtils.cameraNegated.x + pos.x, NametagUtils.cameraNegated.y + pos.y, NametagUtils.cameraNegated.z + pos.z, 1.0);
        vec4.mul((Matrix4fc)model, mmMat4);
        mmMat4.mul((Matrix4fc)projection, pmMat4);
        boolean bl = behind = NametagUtils.pmMat4.w <= 0.0f;
        if (behind && !allowBehind) {
            return false;
        }
        NametagUtils.toScreen(pmMat4);
        double x = NametagUtils.pmMat4.x * (float)MeteorClient.mc.getWindow().getWidth();
        double y = NametagUtils.pmMat4.y * (float)MeteorClient.mc.getWindow().getHeight();
        if (behind) {
            x = (double)MeteorClient.mc.getWindow().getWidth() - x;
            y = (double)MeteorClient.mc.getWindow().getHeight() - y;
        }
        if (Double.isInfinite(x) || Double.isInfinite(y)) {
            return false;
        }
        pos.set(x / windowScale, (double)MeteorClient.mc.getWindow().getHeight() - y / windowScale, allowBehind ? (double)NametagUtils.pmMat4.w : (double)NametagUtils.pmMat4.z);
        return true;
    }

    public static void begin(Vector3d pos) {
        Matrix4fStack matrices = RenderSystem.getModelViewStack();
        NametagUtils.begin(matrices, pos);
    }

    public static void begin(Vector3d pos, GuiGraphicsExtractor graphics) {
        NametagUtils.begin(pos);
        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.scale(1.0f / (float)MeteorClient.mc.getWindow().getGuiScale());
        matrices.translate((float)pos.x, (float)pos.y);
        matrices.scale((float)scale, (float)scale);
    }

    private static void begin(Matrix4fStack matrices, Vector3d pos) {
        matrices.pushMatrix();
        matrices.translate((float)pos.x, (float)pos.y, 0.0f);
        matrices.scale((float)scale, (float)scale, 1.0f);
    }

    public static void end() {
        RenderSystem.getModelViewStack().popMatrix();
    }

    public static void end(GuiGraphicsExtractor graphics) {
        NametagUtils.end();
        graphics.pose().popMatrix();
    }

    private static double getScale(Vector3d pos) {
        double dist = camera.distance((Vector3dc)pos);
        return Mth.clamp((double)(1.0 - dist * 0.01), (double)0.5, (double)2.147483647E9);
    }

    private static void toScreen(Vector4f vec) {
        float newW = 1.0f / vec.w * 0.5f;
        vec.x = vec.x * newW + 0.5f;
        vec.y = vec.y * newW + 0.5f;
        vec.z = vec.z * newW + 0.5f;
        vec.w = newW;
    }
}
