package net.wurstclient.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public final class CustomQuadRenderState
extends Record
implements class_11244 {
    private final RenderPipeline pipeline;
    private final class_11231 textureSetup;
    private final Matrix3x2f pose;
    private final float x1;
    private final float y1;
    private final float x2;
    private final float y2;
    private final float x3;
    private final float y3;
    private final float x4;
    private final float y4;
    private final int color1;
    private final int color2;
    private final int color3;
    private final int color4;
    @Nullable
    private final class_8030 scissorArea;
    @Nullable
    private final class_8030 bounds;

    public CustomQuadRenderState(Matrix3x2f pose, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int color1, int color2, int color3, int color4, @Nullable class_8030 scissorArea) {
        this(class_10799.field_56879, class_11231.method_70899(), pose, x1, y1, x2, y2, x3, y3, x4, y4, color1, color2, color3, color4, scissorArea, CustomQuadRenderState.createBounds(x1, y1, x2, y2, x3, y3, x4, y4, pose, scissorArea));
    }

    public CustomQuadRenderState(Matrix3x2f pose, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int color, @Nullable class_8030 scissorArea) {
        this(pose, x1, y1, x2, y2, x3, y3, x4, y4, color, color, color, color, scissorArea);
    }

    public CustomQuadRenderState(RenderPipeline pipeline, class_11231 textureSetup, Matrix3x2f pose, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int color1, int color2, int color3, int color4, @Nullable class_8030 scissorArea, @Nullable class_8030 bounds) {
        this.pipeline = pipeline;
        this.textureSetup = textureSetup;
        this.pose = pose;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
        this.x4 = x4;
        this.y4 = y4;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.color4 = color4;
        this.scissorArea = scissorArea;
        this.bounds = bounds;
    }

    public void method_70917(class_4588 vertices) {
        vertices.method_70815((Matrix3x2fc)this.pose(), this.x1(), this.y1()).method_39415(this.color1());
        vertices.method_70815((Matrix3x2fc)this.pose(), this.x2(), this.y2()).method_39415(this.color2());
        vertices.method_70815((Matrix3x2fc)this.pose(), this.x3(), this.y3()).method_39415(this.color3());
        vertices.method_70815((Matrix3x2fc)this.pose(), this.x4(), this.y4()).method_39415(this.color4());
    }

    @Nullable
    private static class_8030 createBounds(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, Matrix3x2f pose, @Nullable class_8030 scissorArea) {
        float minX = Math.min(x1, Math.min(x2, Math.min(x3, x4)));
        float maxX = Math.max(x1, Math.max(x2, Math.max(x3, x4)));
        float minY = Math.min(y1, Math.min(y2, Math.min(y3, y4)));
        float maxY = Math.max(y1, Math.max(y2, Math.max(y3, y4)));
        class_8030 screenRect = new class_8030((int)minX, (int)minY, (int)(maxX - minX), (int)(maxY - minY)).method_71523((Matrix3x2fc)pose);
        return scissorArea != null ? scissorArea.method_49701(screenRect) : screenRect;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{CustomQuadRenderState.class, "pipeline;textureSetup;pose;x1;y1;x2;y2;x3;y3;x4;y4;color1;color2;color3;color4;scissorArea;bounds", "pipeline", "textureSetup", "pose", "x1", "y1", "x2", "y2", "x3", "y3", "x4", "y4", "color1", "color2", "color3", "color4", "scissorArea", "bounds"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CustomQuadRenderState.class, "pipeline;textureSetup;pose;x1;y1;x2;y2;x3;y3;x4;y4;color1;color2;color3;color4;scissorArea;bounds", "pipeline", "textureSetup", "pose", "x1", "y1", "x2", "y2", "x3", "y3", "x4", "y4", "color1", "color2", "color3", "color4", "scissorArea", "bounds"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CustomQuadRenderState.class, "pipeline;textureSetup;pose;x1;y1;x2;y2;x3;y3;x4;y4;color1;color2;color3;color4;scissorArea;bounds", "pipeline", "textureSetup", "pose", "x1", "y1", "x2", "y2", "x3", "y3", "x4", "y4", "color1", "color2", "color3", "color4", "scissorArea", "bounds"}, this, o);
    }

    public RenderPipeline comp_4055() {
        return this.pipeline;
    }

    public class_11231 comp_4056() {
        return this.textureSetup;
    }

    public Matrix3x2f pose() {
        return this.pose;
    }

    public float x1() {
        return this.x1;
    }

    public float y1() {
        return this.y1;
    }

    public float x2() {
        return this.x2;
    }

    public float y2() {
        return this.y2;
    }

    public float x3() {
        return this.x3;
    }

    public float y3() {
        return this.y3;
    }

    public float x4() {
        return this.x4;
    }

    public float y4() {
        return this.y4;
    }

    public int color1() {
        return this.color1;
    }

    public int color2() {
        return this.color2;
    }

    public int color3() {
        return this.color3;
    }

    public int color4() {
        return this.color4;
    }

    @Nullable
    public class_8030 comp_4069() {
        return this.scissorArea;
    }

    @Nullable
    public class_8030 comp_4274() {
        return this.bounds;
    }
}
