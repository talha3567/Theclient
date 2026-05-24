package net.wurstclient;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_10799;
import net.minecraft.class_290;
import net.minecraft.class_2960;

public final class WurstShaderPipelines
extends Enum<WurstShaderPipelines> {
    public static final RenderPipeline.Snippet FOGLESS_LINES_SNIPPET;
    public static final RenderPipeline DEPTH_TEST_LINES;
    public static final RenderPipeline ESP_LINES;
    public static final RenderPipeline QUADS;
    public static final RenderPipeline ESP_QUADS;
    public static final RenderPipeline ESP_QUADS_NO_CULLING;
    private static final /* synthetic */ WurstShaderPipelines[] $VALUES;

    public static WurstShaderPipelines[] values() {
        return (WurstShaderPipelines[])$VALUES.clone();
    }

    public static WurstShaderPipelines valueOf(String name) {
        return Enum.valueOf(WurstShaderPipelines.class, name);
    }

    private static /* synthetic */ WurstShaderPipelines[] $values() {
        return new WurstShaderPipelines[0];
    }

    static {
        $VALUES = WurstShaderPipelines.$values();
        FOGLESS_LINES_SNIPPET = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60127, class_10799.field_60126}).withVertexShader(class_2960.method_60654((String)"wurst:core/fogless_lines")).withFragmentShader(class_2960.method_60654((String)"wurst:core/fogless_lines")).withBlend(BlendFunction.TRANSLUCENT).withCull(false).withVertexFormat(class_290.field_63455, VertexFormat.class_5596.field_27377).buildSnippet();
        DEPTH_TEST_LINES = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{FOGLESS_LINES_SNIPPET}).withLocation(class_2960.method_60654((String)"wurst:pipeline/wurst_depth_test_lines")).build());
        ESP_LINES = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{FOGLESS_LINES_SNIPPET}).withLocation(class_2960.method_60654((String)"wurst:pipeline/wurst_esp_lines")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build());
        QUADS = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56860}).withLocation(class_2960.method_60654((String)"wurst:pipeline/wurst_quads")).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).build());
        ESP_QUADS = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56860}).withLocation(class_2960.method_60654((String)"wurst:pipeline/wurst_esp_quads")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build());
        ESP_QUADS_NO_CULLING = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56860}).withLocation(class_2960.method_60654((String)"wurst:pipeline/wurst_esp_quads")).withCull(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build());
    }
}
