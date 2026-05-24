package net.wurstclient.hacks;

import java.util.ArrayList;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_238;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.RenderUtils;

@SearchTags(value={"prophunt esp"})
public final class ProphuntEspHack
extends Hack
implements RenderListener {
    private static final class_238 FAKE_BLOCK_BOX = new class_238(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5);

    public ProphuntEspHack() {
        super("ProphuntESP");
        this.setCategory(Category.RENDER);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(RenderListener.class, this);
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        float alpha = 0.5f + 0.25f * class_3532.method_15374((double)((float)(System.currentTimeMillis() % 1000L) / 500.0f * (float)Math.PI));
        int color = RenderUtils.toIntColor(new float[]{1.0f, 0.0f, 0.0f}, alpha);
        ArrayList<class_238> boxes = new ArrayList<class_238>();
        for (class_1297 entity : ProphuntEspHack.MC.field_1687.method_18112()) {
            if (!(entity instanceof class_1308) || !entity.method_5767() || ProphuntEspHack.MC.field_1724.method_5858(entity) < 0.25) continue;
            boxes.add(FAKE_BLOCK_BOX.method_997(entity.method_73189()));
        }
        RenderUtils.drawSolidBoxes(matrixStack, boxes, color, false);
        RenderUtils.drawOutlinedBoxes(matrixStack, boxes, color, false);
    }
}
