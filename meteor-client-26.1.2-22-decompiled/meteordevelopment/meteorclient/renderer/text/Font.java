package meteordevelopment.meteorclient.renderer.text;

import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackRange;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Struct;

public class Font {
    public final Texture texture;
    private final int height;
    private final float scale;
    private final float ascent;
    private final Int2ObjectOpenHashMap<CharData> charMap = new Int2ObjectOpenHashMap();
    private static final int size = 2048;

    public Font(ByteBuffer buffer, int height) {
        this.height = height;
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTruetype.stbtt_InitFont((STBTTFontinfo)fontInfo, (ByteBuffer)buffer);
        ByteBuffer bitmap = BufferUtils.createByteBuffer((int)0x400000);
        STBTTPackedchar.Buffer[] cdata = new STBTTPackedchar.Buffer[]{STBTTPackedchar.create((int)95), STBTTPackedchar.create((int)96), STBTTPackedchar.create((int)128), STBTTPackedchar.create((int)144), STBTTPackedchar.create((int)256), STBTTPackedchar.create((int)1)};
        STBTTPackContext packContext = STBTTPackContext.create();
        STBTruetype.stbtt_PackBegin((STBTTPackContext)packContext, (ByteBuffer)bitmap, (int)2048, (int)2048, (int)0, (int)1);
        STBTTPackRange.Buffer packRange = STBTTPackRange.create((int)cdata.length);
        packRange.put((Struct)STBTTPackRange.create().set((float)height, 32, null, 95, cdata[0], (byte)2, (byte)2));
        packRange.put((Struct)STBTTPackRange.create().set((float)height, 160, null, 96, cdata[1], (byte)2, (byte)2));
        packRange.put((Struct)STBTTPackRange.create().set((float)height, 256, null, 128, cdata[2], (byte)2, (byte)2));
        packRange.put((Struct)STBTTPackRange.create().set((float)height, 880, null, 144, cdata[3], (byte)2, (byte)2));
        packRange.put((Struct)STBTTPackRange.create().set((float)height, 1024, null, 256, cdata[4], (byte)2, (byte)2));
        packRange.put((Struct)STBTTPackRange.create().set((float)height, 8734, null, 1, cdata[5], (byte)2, (byte)2));
        packRange.flip();
        STBTruetype.stbtt_PackFontRanges((STBTTPackContext)packContext, (ByteBuffer)buffer, (int)0, (STBTTPackRange.Buffer)packRange);
        STBTruetype.stbtt_PackEnd((STBTTPackContext)packContext);
        this.texture = new Texture(2048, 2048, TextureFormat.RED8, FilterMode.LINEAR, FilterMode.LINEAR);
        this.texture.upload(bitmap);
        this.scale = STBTruetype.stbtt_ScaleForPixelHeight((STBTTFontinfo)fontInfo, (float)height);
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer ascent = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics((STBTTFontinfo)fontInfo, (IntBuffer)ascent, null, null);
            this.ascent = ascent.get(0);
        }
        for (int i = 0; i < cdata.length; ++i) {
            STBTTPackedchar.Buffer cbuf = cdata[i];
            int offset = ((STBTTPackRange)packRange.get(i)).first_unicode_codepoint_in_range();
            for (int j = 0; j < cbuf.capacity(); ++j) {
                STBTTPackedchar packedChar = (STBTTPackedchar)cbuf.get(j);
                float ipw = 4.8828125E-4f;
                float iph = 4.8828125E-4f;
                this.charMap.put(j + offset, (Object)new CharData(packedChar.xoff(), packedChar.yoff(), packedChar.xoff2(), packedChar.yoff2(), (float)packedChar.x0() * ipw, (float)packedChar.y0() * iph, (float)packedChar.x1() * ipw, (float)packedChar.y1() * iph, packedChar.xadvance()));
            }
        }
    }

    public double getWidth(String string, int length) {
        double width = 0.0;
        for (int i = 0; i < length; ++i) {
            char cp = string.charAt(i);
            CharData c = (CharData)this.charMap.get((int)cp);
            if (c == null) {
                c = (CharData)this.charMap.get(32);
            }
            width += (double)c.xAdvance;
        }
        return width;
    }

    public int getHeight() {
        return this.height;
    }

    public double render(MeshBuilder mesh, String string, double x, double y, Color color, double scale) {
        y += (double)(this.ascent * this.scale) * scale;
        int length = string.length();
        mesh.ensureCapacity(length * 4, length * 6);
        for (int i = 0; i < length; ++i) {
            char cp = string.charAt(i);
            CharData c = (CharData)this.charMap.get((int)cp);
            if (c == null) {
                c = (CharData)this.charMap.get(32);
            }
            mesh.quad(mesh.vec2(x + (double)c.x0 * scale, y + (double)c.y0 * scale).vec2(c.u0, c.v0).color(color).next(), mesh.vec2(x + (double)c.x0 * scale, y + (double)c.y1 * scale).vec2(c.u0, c.v1).color(color).next(), mesh.vec2(x + (double)c.x1 * scale, y + (double)c.y1 * scale).vec2(c.u1, c.v1).color(color).next(), mesh.vec2(x + (double)c.x1 * scale, y + (double)c.y0 * scale).vec2(c.u1, c.v0).color(color).next());
            x += (double)c.xAdvance * scale;
        }
        return x;
    }

    private record CharData(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float xAdvance) {
    }
}
