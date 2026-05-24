package net.wurstclient;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.class_12245;
import net.minecraft.class_12246;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.wurstclient.WurstShaderPipelines;

public final class WurstRenderLayers
extends Enum<WurstRenderLayers> {
    public static final class_1921 LINES;
    public static final class_1921 ESP_LINES;
    public static final class_1921 QUADS;
    public static final class_1921 ESP_QUADS;
    public static final class_1921 ESP_QUADS_NO_CULLING;
    private static final /* synthetic */ WurstRenderLayers[] $VALUES;

    public static WurstRenderLayers[] values() {
        return (WurstRenderLayers[])$VALUES.clone();
    }

    public static WurstRenderLayers valueOf(String name) {
        return Enum.valueOf(WurstRenderLayers.class, name);
    }

    public static class_1921 getQuads(boolean depthTest) {
        return depthTest ? QUADS : ESP_QUADS;
    }

    public static class_1921 getLines(boolean depthTest) {
        return depthTest ? LINES : ESP_LINES;
    }

    private static /* synthetic */ WurstRenderLayers[] $values() {
        return new WurstRenderLayers[0];
    }

    static {
        $VALUES = WurstRenderLayers.$values();
        LINES = class_1921.method_75940((String)"wurst:lines", (class_12247)class_12247.method_75927((RenderPipeline)WurstShaderPipelines.DEPTH_TEST_LINES).method_75930(class_12245.field_63976).method_75931(class_12246.field_63983).method_75938());
        ESP_LINES = class_1921.method_75940((String)"wurst:esp_lines", (class_12247)class_12247.method_75927((RenderPipeline)WurstShaderPipelines.ESP_LINES).method_75930(class_12245.field_63976).method_75931(class_12246.field_63983).method_75938());
        QUADS = class_1921.method_75940((String)"wurst:quads", (class_12247)class_12247.method_75927((RenderPipeline)WurstShaderPipelines.QUADS).method_75937().method_75938());
        ESP_QUADS = class_1921.method_75940((String)"wurst:esp_quads", (class_12247)class_12247.method_75927((RenderPipeline)WurstShaderPipelines.ESP_QUADS).method_75937().method_75938());
        ESP_QUADS_NO_CULLING = class_1921.method_75940((String)"wurst:esp_quads_no_culling", (class_12247)class_12247.method_75927((RenderPipeline)WurstShaderPipelines.ESP_QUADS_NO_CULLING).method_75937().method_75928().method_75938());
    }
}
