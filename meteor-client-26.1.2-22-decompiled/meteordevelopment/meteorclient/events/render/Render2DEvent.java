package meteordevelopment.meteorclient.events.render;

import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Render2DEvent {
    private static final Render2DEvent INSTANCE = new Render2DEvent();
    public GuiGraphicsExtractor graphics;
    public int screenWidth;
    public int screenHeight;
    public double frameTime;
    public float tickDelta;

    public static Render2DEvent get(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, float tickDelta) {
        Render2DEvent.INSTANCE.graphics = graphics;
        Render2DEvent.INSTANCE.screenWidth = screenWidth;
        Render2DEvent.INSTANCE.screenHeight = screenHeight;
        Render2DEvent.INSTANCE.frameTime = Utils.frameTime;
        Render2DEvent.INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
