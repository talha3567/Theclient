package net.wurstclient.clickgui.components;

import net.minecraft.class_1297;
import net.minecraft.class_1421;
import net.minecraft.class_1429;
import net.minecraft.class_1480;
import net.minecraft.class_1569;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_332;
import net.minecraft.class_746;
import net.minecraft.class_9866;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.ClickGuiIcons;
import net.wurstclient.clickgui.Component;
import net.wurstclient.hacks.RadarHack;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RenderUtils;
import org.joml.Matrix3x2fStack;

public final class RadarComponent
extends Component {
    private final RadarHack hack;

    public RadarComponent(RadarHack hack) {
        this.hack = hack;
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        ClickGui gui = WURST.getGui();
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        float middleX = (float)(x1 + x2) / 2.0f;
        float middleY = (float)(y1 + y2) / 2.0f;
        if (this.isHovering(mouseX, mouseY)) {
            gui.setTooltip("");
        }
        context.method_25294(x1, y1, x2, y2, RenderUtils.toIntColor(gui.getBgColor(), gui.getOpacity()));
        Matrix3x2fStack matrixStack = context.method_51448();
        matrixStack.pushMatrix();
        matrixStack.translate(middleX, middleY);
        class_746 player = RadarComponent.MC.field_1724;
        if (!this.hack.isRotateEnabled()) {
            matrixStack.rotate((180.0f + player.method_36454()) * ((float)Math.PI / 180));
        }
        ClickGuiIcons.drawRadarArrow(context, -2.0f, -2.0f, 2.0f, 2.0f);
        matrixStack.popMatrix();
        class_243 lerpedPlayerPos = EntityUtils.getLerpedPos((class_1297)player, partialTicks);
        for (class_1297 e : this.hack.getEntities()) {
            class_243 lerpedEntityPos = EntityUtils.getLerpedPos(e, partialTicks);
            double diffX = lerpedEntityPos.field_1352 - lerpedPlayerPos.field_1352;
            double diffZ = lerpedEntityPos.field_1350 - lerpedPlayerPos.field_1350;
            double distance = Math.sqrt(diffX * diffX + diffZ * diffZ) * ((double)this.getWidth() * 0.5 / this.hack.getRadius());
            double neededRotation = Math.toDegrees(Math.atan2(diffZ, diffX));
            double angle = this.hack.isRotateEnabled() ? Math.toRadians((double)player.method_36454() - neededRotation - 90.0) : Math.toRadians(180.0 - neededRotation - 90.0);
            double renderX = Math.sin(angle) * distance;
            double renderY = Math.cos(angle) * distance;
            if (Math.abs(renderX) > (double)this.getWidth() / 2.0 || Math.abs(renderY) > (double)this.getHeight() / 2.0) continue;
            float ex1 = middleX + (float)renderX - 0.5f;
            float ex2 = middleX + (float)renderX + 0.5f;
            float ey1 = middleY + (float)renderY - 0.5f;
            float ey2 = middleY + (float)renderY + 0.5f;
            RenderUtils.fill2D(context, ex1, ey1, ex2, ey2, this.getEntityColor(e));
        }
    }

    private int getEntityColor(class_1297 e) {
        if (WURST.getFriends().isFriend(e)) {
            return -16776961;
        }
        if (e instanceof class_1657) {
            return -65536;
        }
        if (e instanceof class_1569) {
            return Short.MIN_VALUE;
        }
        if (e instanceof class_1429 || e instanceof class_1421 || e instanceof class_1480 || e instanceof class_9866) {
            return -16711936;
        }
        return -8355712;
    }

    @Override
    public int getDefaultWidth() {
        return 96;
    }

    @Override
    public int getDefaultHeight() {
        return 96;
    }
}
