package meteordevelopment.meteorclient.utils.render.postprocess;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ResourcePacksReloadedEvent;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.postprocess.EntityShader;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class ChamsShader
extends EntityShader {
    private static final String[] FILE_FORMATS = new String[]{"png", "jpg"};
    private static Texture IMAGE_TEX;
    private static Chams chams;
    private static final int UNIFORM_SIZE;
    private static final DynamicUniformStorage<UniformData> UNIFORM_STORAGE;

    public ChamsShader() {
        super(MeteorRenderPipelines.POST_IMAGE);
        MeteorClient.EVENT_BUS.subscribe(ChamsShader.class);
    }

    @PostInit
    public static void load() {
        try {
            ByteBuffer data = null;
            for (String fileFormat : FILE_FORMATS) {
                Optional optional = MeteorClient.mc.getResourceManager().getResource(MeteorClient.identifier("textures/chams." + fileFormat));
                if (optional.isEmpty() || ((Resource)optional.get()).open() == null) continue;
                data = TextureUtil.readResource((InputStream)((Resource)optional.get()).open());
                break;
            }
            if (data == null) {
                return;
            }
            data.rewind();
            try (MemoryStack stack = MemoryStack.stackPush();){
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);
                STBImage.stbi_set_flip_vertically_on_load((boolean)true);
                ByteBuffer image = STBImage.stbi_load_from_memory((ByteBuffer)data, (IntBuffer)width, (IntBuffer)height, (IntBuffer)comp, (int)4);
                IMAGE_TEX = new Texture(width.get(0), height.get(0), TextureFormat.RGBA8, FilterMode.NEAREST, FilterMode.NEAREST);
                IMAGE_TEX.upload(image);
                STBImage.stbi_image_free((ByteBuffer)image);
                STBImage.stbi_set_flip_vertically_on_load((boolean)false);
            }
        }
        catch (IOException e) {
            MeteorClient.LOG.error("Error loading the chams shader", e);
        }
    }

    @EventHandler
    private static void onResourcePacksReloaded(ResourcePacksReloadedEvent event) {
        ChamsShader.load();
    }

    @Override
    protected void setupPass(MeshRenderer renderer) {
        Color color = ChamsShader.chams.shaderColor.get();
        renderer.uniform("ImageData", UNIFORM_STORAGE.writeUniform((DynamicUniformStorage.DynamicUniform)new UniformData((float)color.r / 255.0f, (float)color.g / 255.0f, (float)color.b / 255.0f, (float)color.a / 255.0f)));
        if (chams.isShader() && ChamsShader.chams.shader.get() == Chams.Shader.Image && IMAGE_TEX != null) {
            renderer.sampler("u_TextureI", IMAGE_TEX.getTextureView(), IMAGE_TEX.getSampler());
        }
    }

    @Override
    protected boolean shouldDraw() {
        if (chams == null) {
            chams = Modules.get().get(Chams.class);
        }
        return chams.isShader();
    }

    @Override
    public boolean shouldDraw(Entity entity) {
        if (!this.shouldDraw()) {
            return false;
        }
        return ChamsShader.chams.entities.get().contains(entity.getType()) && (entity != MeteorClient.mc.player || ChamsShader.chams.ignoreSelfDepth.get() == false);
    }

    public static void flipFrame() {
        UNIFORM_STORAGE.endFrame();
    }

    static {
        UNIFORM_SIZE = new Std140SizeCalculator().putVec4().get();
        UNIFORM_STORAGE = new DynamicUniformStorage("Meteor - Image UBO", UNIFORM_SIZE, 16);
    }

    private record UniformData(float r, float g, float b, float a) implements DynamicUniformStorage.DynamicUniform
    {
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer((ByteBuffer)buffer).putVec4(this.r, this.g, this.b, this.a);
        }
    }
}
