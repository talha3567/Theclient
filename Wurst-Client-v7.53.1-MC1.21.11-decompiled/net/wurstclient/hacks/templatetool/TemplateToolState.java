package net.wurstclient.hacks.templatetool;

import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.TemplateToolHack;

public abstract class TemplateToolState {
    protected static final WurstClient WURST = WurstClient.INSTANCE;
    protected static final class_310 MC = WurstClient.MC;

    public void onEnter(TemplateToolHack hack) {
    }

    public void onExit(TemplateToolHack hack) {
    }

    public void onUpdate(TemplateToolHack hack) {
    }

    public void onRender(TemplateToolHack hack, class_4587 matrixStack, float partialTicks) {
    }

    public final void onRenderGUI(TemplateToolHack hack, class_332 context, float partialTicks) {
        String message = this.getMessage(hack);
        class_327 tr = TemplateToolState.MC.field_1772;
        int msgWidth = tr.method_1727(message);
        int msgX1 = context.method_51421() / 2 - msgWidth / 2;
        int msgX2 = msgX1 + msgWidth + 2;
        int msgY1 = context.method_51443() / 2 + 1;
        int msgY2 = msgY1 + 10;
        context.method_25294(msgX1, msgY1, msgX2, msgY2, Integer.MIN_VALUE);
        context.method_51433(tr, message, msgX1 + 2, msgY1 + 1, -1, false);
    }

    protected abstract String getMessage(TemplateToolHack var1);
}
