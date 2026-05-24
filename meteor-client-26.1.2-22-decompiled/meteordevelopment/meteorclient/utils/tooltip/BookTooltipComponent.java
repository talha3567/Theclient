package meteordevelopment.meteorclient.utils.tooltip;

import meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fStack;

public class BookTooltipComponent
implements ClientTooltipComponent,
MeteorTooltipData {
    private static final Identifier TEXTURE_BOOK_BACKGROUND = Identifier.parse((String)"textures/gui/book.png");
    private final Component page;

    public BookTooltipComponent(Component page) {
        this.page = page;
    }

    @Override
    public ClientTooltipComponent getComponent() {
        return this;
    }

    public int getHeight(Font textRenderer) {
        return 134;
    }

    public int getWidth(Font textRenderer) {
        return 112;
    }

    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_BOOK_BACKGROUND, x - 10, y, 0.0f, 0.0f, 128, 128, 179, 179);
        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.translate((float)(x + 16), (float)(y + 12));
        matrices.scale(0.7f, 0.7f);
        int offset = 0;
        for (FormattedCharSequence line : font.split((FormattedText)this.page, 112)) {
            graphics.text(font, line, 0, offset, -16777216, false);
            offset += 8;
        }
        matrices.popMatrix();
    }
}
