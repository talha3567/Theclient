package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={GuiGraphicsExtractor.class})
public interface GuiGraphicsExtractorAccessor {
    @Accessor(value="guiRenderState")
    public GuiRenderState getGuiRenderState();

    @Accessor(value="scissorStack")
    public GuiGraphicsExtractor.ScissorStack getScissorStack();
}
