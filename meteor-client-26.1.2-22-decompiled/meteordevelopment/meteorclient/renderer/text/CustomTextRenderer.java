package meteordevelopment.meteorclient.renderer.text;

import java.io.IOException;
import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.text.Font;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.Minecraft;

public class CustomTextRenderer
implements TextRenderer {
    public static final Color SHADOW_COLOR = new Color(60, 60, 60, 180);
    private final MeshBuilder mesh = new MeshBuilder(MeteorRenderPipelines.UI_TEXT);
    public final FontFace fontFace;
    private final Font[] fonts;
    private Font font;
    private boolean building;
    private boolean scaleOnly;
    private double fontScale = 1.0;
    private double scale = 1.0;

    public CustomTextRenderer(FontFace fontFace) throws IOException {
        this.fontFace = fontFace;
        ByteBuffer buffer = fontFace.readToDirectByteBuffer();
        this.fonts = new Font[5];
        for (int i = 0; i < this.fonts.length; ++i) {
            this.fonts[i] = new Font(buffer, (int)Math.round(27.0 * ((double)i * 0.5 + 1.0)));
        }
    }

    @Override
    public void setAlpha(double a) {
        this.mesh.alpha = a;
    }

    @Override
    public void begin(double scale, boolean scaleOnly, boolean big) {
        if (this.building) {
            throw new RuntimeException("CustomTextRenderer.begin() called twice");
        }
        if (!scaleOnly) {
            this.mesh.begin();
        }
        if (big) {
            this.font = this.fonts[this.fonts.length - 1];
        } else {
            double scaleA = Math.floor(scale * 10.0) / 10.0;
            int scaleI = scaleA >= 3.0 ? 5 : (scaleA >= 2.5 ? 4 : (scaleA >= 2.0 ? 3 : (scaleA >= 1.5 ? 2 : 1)));
            this.font = this.fonts[scaleI - 1];
        }
        this.building = true;
        this.scaleOnly = scaleOnly;
        this.fontScale = (double)this.font.getHeight() / 27.0;
        this.scale = 1.0 + (scale - this.fontScale) / this.fontScale;
    }

    @Override
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) {
            return 0.0;
        }
        Font font = this.building ? this.font : this.fonts[0];
        return (font.getWidth(text, length) + (double)(shadow ? 1 : 0)) * this.scale / 1.5;
    }

    @Override
    public double getHeight(boolean shadow) {
        Font font = this.building ? this.font : this.fonts[0];
        return (double)(font.getHeight() + 1 + (shadow ? 1 : 0)) * this.scale / 1.5;
    }

    @Override
    public double render(String text, double x, double y, Color color, boolean shadow) {
        double width;
        boolean wasBuilding = this.building;
        if (!wasBuilding) {
            this.begin();
        }
        if (shadow) {
            int preShadowA = CustomTextRenderer.SHADOW_COLOR.a;
            CustomTextRenderer.SHADOW_COLOR.a = (int)((double)color.a / 255.0 * (double)preShadowA);
            width = this.font.render(this.mesh, text, x + this.fontScale * this.scale / 1.5, y + this.fontScale * this.scale / 1.5, SHADOW_COLOR, this.scale / 1.5);
            this.font.render(this.mesh, text, x, y, color, this.scale / 1.5);
            CustomTextRenderer.SHADOW_COLOR.a = preShadowA;
        } else {
            width = this.font.render(this.mesh, text, x, y, color, this.scale / 1.5);
        }
        if (!wasBuilding) {
            this.end();
        }
        return width;
    }

    @Override
    public boolean isBuilding() {
        return this.building;
    }

    @Override
    public void end() {
        if (!this.building) {
            throw new RuntimeException("CustomTextRenderer.end() called without calling begin()");
        }
        if (!this.scaleOnly) {
            this.mesh.end();
            MeshRenderer.begin().attachments(Minecraft.getInstance().getMainRenderTarget()).pipeline(MeteorRenderPipelines.UI_TEXT).mesh(this.mesh).sampler("u_Texture", this.font.texture.getTextureView(), this.font.texture.getSampler()).end();
        }
        this.building = false;
        this.scale = 1.0;
    }

    public void destroy() {
        for (Font font : this.fonts) {
            font.texture.close();
        }
    }
}
