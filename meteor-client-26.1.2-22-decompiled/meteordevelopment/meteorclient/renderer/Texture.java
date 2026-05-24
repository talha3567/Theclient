package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class Texture
extends AbstractTexture {
    public Texture(int width, int height, TextureFormat format, FilterMode min, FilterMode mag) {
        this.texture = RenderSystem.getDevice().createTexture("", 15, format, width, height, 1, 1);
        this.sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, min, mag, false);
        this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
    }

    public int getWidth() {
        return this.getTexture().getWidth(0);
    }

    public int getHeight() {
        return this.getTexture().getHeight(0);
    }

    public void upload(byte[] bytes) {
        this.upload(BufferUtils.createByteBuffer((int)bytes.length).put(bytes));
    }

    public void upload(ByteBuffer buffer) {
        NativeImage image = this.getImage();
        buffer.rewind();
        MemoryUtil.memCopy((long)MemoryUtil.memAddress((ByteBuffer)buffer), (long)image.getPointer(), (long)buffer.remaining());
        RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, image);
        image.close();
    }

    @NotNull
    private NativeImage getImage() {
        NativeImage.Format imageFormat = switch (this.texture.getFormat()) {
            case TextureFormat.RGBA8 -> NativeImage.Format.RGBA;
            case TextureFormat.RED8 -> NativeImage.Format.LUMINANCE;
            default -> throw new IllegalArgumentException();
        };
        return new NativeImage(imageFormat, this.getWidth(), this.getHeight(), false);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static Texture readResource(String path, boolean flipY, FilterMode filter) {
        try (InputStream in = Texture.class.getResourceAsStream(path);){
            Texture texture;
            block16: {
                if (in == null) {
                    Texture texture2 = null;
                    return texture2;
                }
                ByteBuffer data = TextureUtil.readResource((InputStream)in).rewind();
                MemoryStack stack = MemoryStack.stackPush();
                try {
                    IntBuffer width = stack.mallocInt(1);
                    IntBuffer height = stack.mallocInt(1);
                    IntBuffer comp = stack.mallocInt(1);
                    STBImage.stbi_set_flip_vertically_on_load((boolean)flipY);
                    ByteBuffer image = STBImage.stbi_load_from_memory((ByteBuffer)data, (IntBuffer)width, (IntBuffer)height, (IntBuffer)comp, (int)4);
                    Texture texture3 = new Texture(width.get(0), height.get(0), TextureFormat.RGBA8, filter, filter);
                    texture3.upload(image);
                    STBImage.stbi_image_free((ByteBuffer)image);
                    STBImage.stbi_set_flip_vertically_on_load((boolean)false);
                    texture = texture3;
                    if (stack == null) break block16;
                }
                catch (Throwable throwable) {
                    if (stack != null) {
                        try {
                            stack.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                stack.close();
            }
            return texture;
        }
        catch (IOException iOException) {
            return null;
        }
    }
}
