package meteordevelopment.meteorclient.events.render;

import com.mojang.blaze3d.vertex.PoseStack;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.utils.Utils;

public class Render3DEvent {
    private static final Render3DEvent INSTANCE = new Render3DEvent();
    public PoseStack matrices;
    public Renderer3D renderer;
    public Renderer3D depthRenderer;
    public double frameTime;
    public float tickDelta;
    public double offsetX;
    public double offsetY;
    public double offsetZ;

    public static Render3DEvent get(PoseStack matrices, Renderer3D renderer, Renderer3D depthRenderer, float tickDelta, double offsetX, double offsetY, double offsetZ) {
        Render3DEvent.INSTANCE.matrices = matrices;
        Render3DEvent.INSTANCE.renderer = renderer;
        Render3DEvent.INSTANCE.depthRenderer = depthRenderer;
        Render3DEvent.INSTANCE.frameTime = Utils.frameTime;
        Render3DEvent.INSTANCE.tickDelta = tickDelta;
        Render3DEvent.INSTANCE.offsetX = offsetX;
        Render3DEvent.INSTANCE.offsetY = offsetY;
        Render3DEvent.INSTANCE.offsetZ = offsetZ;
        return INSTANCE;
    }
}
