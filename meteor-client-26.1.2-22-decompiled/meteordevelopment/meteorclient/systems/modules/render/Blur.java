package meteordevelopment.meteorclient.systems.modules.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.IntFloatImmutablePair;
import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ResolutionChangedEvent;
import meteordevelopment.meteorclient.events.render.RenderAfterWorldEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.renderer.FixedUniformStorage;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.listeners.ConsumerListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.DynamicUniformStorage;

public class Blur
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScreens;
    private final IntFloatImmutablePair[] strengths;
    private final Setting<Integer> strength;
    private final Setting<Integer> fadeTime;
    private final Setting<Boolean> meteor;
    private final Setting<Boolean> inventories;
    private final Setting<Boolean> chat;
    private final Setting<Boolean> other;
    private final GpuTextureView[] fbos;
    private GpuBufferSlice[] ubos;
    private boolean enabled;
    private long fadeEndAt;
    private float previousOffset;
    private static final int UNIFORM_SIZE = new Std140SizeCalculator().putVec2().putFloat().get();
    private static final FixedUniformStorage<BlurUniformData> UNIFORM_STORAGE = new FixedUniformStorage("Meteor - Blur UBO", UNIFORM_SIZE, 6);

    public Blur() {
        super(Categories.Render, "blur", "Blurs background when in GUI screens.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScreens = this.settings.createGroup("Screens");
        this.strengths = new IntFloatImmutablePair[]{IntFloatImmutablePair.of((int)1, (float)1.25f), IntFloatImmutablePair.of((int)1, (float)2.25f), IntFloatImmutablePair.of((int)2, (float)2.0f), IntFloatImmutablePair.of((int)2, (float)3.0f), IntFloatImmutablePair.of((int)2, (float)4.25f), IntFloatImmutablePair.of((int)3, (float)2.5f), IntFloatImmutablePair.of((int)3, (float)3.25f), IntFloatImmutablePair.of((int)3, (float)4.25f), IntFloatImmutablePair.of((int)3, (float)5.5f), IntFloatImmutablePair.of((int)4, (float)3.25f), IntFloatImmutablePair.of((int)4, (float)4.0f), IntFloatImmutablePair.of((int)4, (float)5.0f), IntFloatImmutablePair.of((int)4, (float)6.0f), IntFloatImmutablePair.of((int)4, (float)7.25f), IntFloatImmutablePair.of((int)4, (float)8.25f), IntFloatImmutablePair.of((int)5, (float)4.5f), IntFloatImmutablePair.of((int)5, (float)5.25f), IntFloatImmutablePair.of((int)5, (float)6.25f), IntFloatImmutablePair.of((int)5, (float)7.25f), IntFloatImmutablePair.of((int)5, (float)8.5f)};
        this.strength = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("strength")).description("How strong the blur should be.")).defaultValue(5)).min(1).max(20).sliderRange(1, 20).build());
        this.fadeTime = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("fade-time")).description("How long the fade will last in milliseconds.")).defaultValue(100)).min(0).sliderMax(500).build());
        this.meteor = this.sgScreens.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("meteor")).description("Applies blur to Meteor screens.")).defaultValue(true)).build());
        this.inventories = this.sgScreens.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("inventories")).description("Applies blur to inventory screens.")).defaultValue(true)).build());
        this.chat = this.sgScreens.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat")).description("Applies blur when in chat.")).defaultValue(false)).build());
        this.other = this.sgScreens.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("other")).description("Applies blur to all other screen types.")).defaultValue(true)).build());
        this.fbos = new GpuTextureView[6];
        this.previousOffset = -1.0f;
        for (int i = 0; i < this.fbos.length; ++i) {
            this.fbos[i] = this.createFbo(i);
        }
        MeteorClient.EVENT_BUS.subscribe(new ConsumerListener<ResolutionChangedEvent>(ResolutionChangedEvent.class, event -> {
            for (int i = 0; i < this.fbos.length; ++i) {
                if (this.fbos[i] != null) {
                    this.fbos[i].close();
                }
                this.fbos[i] = this.createFbo(i);
            }
            this.previousOffset = -1.0f;
        }));
        MeteorClient.EVENT_BUS.subscribe(new ConsumerListener<RenderAfterWorldEvent>(RenderAfterWorldEvent.class, event -> this.onRenderAfterWorld()));
    }

    private GpuTextureView createFbo(int i) {
        double scale = 1.0 / Math.pow(2.0, i);
        int width = (int)((double)this.mc.getWindow().getWidth() * scale);
        int height = (int)((double)this.mc.getWindow().getHeight() * scale);
        return RenderSystem.getDevice().createTextureView(RenderSystem.getDevice().createTexture("Blur - " + i, 15, TextureFormat.RGBA8, width, height, 1, 1));
    }

    private void onRenderAfterWorld() {
        int i;
        boolean shouldRender = this.shouldRender();
        long time = System.currentTimeMillis();
        if (this.enabled) {
            if (!shouldRender) {
                if (this.fadeEndAt == -1L) {
                    this.fadeEndAt = System.currentTimeMillis() + (long)this.fadeTime.get().intValue();
                }
                if (time >= this.fadeEndAt) {
                    this.enabled = false;
                    this.fadeEndAt = -1L;
                }
            }
        } else if (shouldRender) {
            this.enabled = true;
            this.fadeEndAt = System.currentTimeMillis() + (long)this.fadeTime.get().intValue();
        }
        if (!this.enabled) {
            return;
        }
        double progress = 1.0;
        if (time < this.fadeEndAt) {
            progress = shouldRender ? 1.0 - (double)(this.fadeEndAt - time) / this.fadeTime.get().doubleValue() : (double)(this.fadeEndAt - time) / this.fadeTime.get().doubleValue();
        } else {
            this.fadeEndAt = -1L;
        }
        IntFloatImmutablePair strength = this.strengths[(int)((double)(this.strength.get() - 1) * progress)];
        int iterations = strength.leftInt();
        float offset = strength.rightFloat();
        if (this.previousOffset != offset) {
            this.updateUniforms(offset);
            this.previousOffset = offset;
        }
        this.renderToFbo(this.fbos[0], this.mc.getMainRenderTarget().getColorTextureView(), MeteorRenderPipelines.BLUR_DOWN, this.ubos[0]);
        for (i = 0; i < iterations; ++i) {
            this.renderToFbo(this.fbos[i + 1], this.fbos[i], MeteorRenderPipelines.BLUR_DOWN, this.ubos[i + 1]);
        }
        for (i = iterations; i >= 1; --i) {
            this.renderToFbo(this.fbos[i - 1], this.fbos[i], MeteorRenderPipelines.BLUR_UP, this.ubos[i - 1]);
        }
        MeshRenderer.begin().attachments(this.mc.getMainRenderTarget()).pipeline(MeteorRenderPipelines.BLUR_PASSTHROUGH).fullscreen().sampler("u_Texture", this.fbos[0], RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)).end();
    }

    private void renderToFbo(GpuTextureView targetFbo, GpuTextureView sourceTexture, RenderPipeline pipeline, GpuBufferSlice ubo) {
        MeshRenderer.begin().attachments(targetFbo, null).pipeline(pipeline).fullscreen().uniform("BlurData", ubo).sampler("u_Texture", sourceTexture, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)).end();
    }

    private boolean shouldRender() {
        if (!this.isActive()) {
            return false;
        }
        Screen screen = this.mc.screen;
        if (screen instanceof WidgetScreen) {
            return this.meteor.get();
        }
        if (screen instanceof AbstractContainerScreen) {
            return this.inventories.get();
        }
        if (screen instanceof ChatScreen) {
            return this.chat.get();
        }
        if (screen != null) {
            return this.other.get();
        }
        return false;
    }

    private void updateUniforms(float offset) {
        UNIFORM_STORAGE.clear();
        DynamicUniformStorage.DynamicUniform[] uboData = new BlurUniformData[6];
        for (int i = 0; i < uboData.length; ++i) {
            GpuTextureView fbo = this.fbos[i];
            uboData[i] = new BlurUniformData(0.5f / (float)fbo.getWidth(0), 0.5f / (float)fbo.getHeight(0), offset);
        }
        this.ubos = UNIFORM_STORAGE.writeAll(uboData);
    }

    private record BlurUniformData(float halfTexelSizeX, float halfTexelSizeY, float offset) implements DynamicUniformStorage.DynamicUniform
    {
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer((ByteBuffer)buffer).putVec2(this.halfTexelSizeX, this.halfTexelSizeY).putFloat(this.offset);
        }
    }
}
