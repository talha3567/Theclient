package net.wurstclient.clickgui;

import net.minecraft.class_332;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.util.RenderUtils;

public final class ClickGuiIcons
extends Enum<ClickGuiIcons> {
    private static final /* synthetic */ ClickGuiIcons[] $VALUES;

    public static ClickGuiIcons[] values() {
        return (ClickGuiIcons[])$VALUES.clone();
    }

    public static ClickGuiIcons valueOf(String name) {
        return Enum.valueOf(ClickGuiIcons.class, name);
    }

    public static void drawMinimizeArrow(class_332 context, float x1, float y1, float x2, float y2, boolean hovering, boolean minimized) {
        float[][] arrowVertices;
        int arrowColor;
        float xa1 = x1 + 1.0f;
        float xa2 = (x1 + x2) / 2.0f;
        float xa3 = x2 - 1.0f;
        if (minimized) {
            float ya1 = y1 + 3.0f;
            float ya2 = y2 - 2.5f;
            arrowColor = hovering ? -16711936 : -16721664;
            arrowVertices = new float[][]{{xa1, ya1}, {xa2, ya2}, {xa3, ya1}};
        } else {
            float ya1 = y2 - 3.0f;
            float ya2 = y1 + 2.5f;
            arrowColor = hovering ? -65536 : -2555904;
            arrowVertices = new float[][]{{xa1, ya1}, {xa3, ya1}, {xa2, ya2}};
        }
        RenderUtils.fillTriangle2D(context, arrowVertices, arrowColor);
        int outlineColor = -2146430960;
        RenderUtils.drawLineStrip2D(context, arrowVertices, outlineColor);
    }

    public static void drawRadarArrow(class_332 context, float x1, float y1, float x2, float y2) {
        float x3 = x1 + (x2 - x1) / 2.0f;
        float y3 = y1 + (y2 - y1) * 0.75f;
        ClickGui gui = WurstClient.INSTANCE.getGui();
        int arrowColor = RenderUtils.toIntColor(gui.getAcColor(), gui.getOpacity());
        float[][] arrowVertices = new float[][]{{x3, y1}, {x1, y2}, {x3, y3}, {x2, y2}};
        RenderUtils.fillQuads2D(context, arrowVertices, arrowColor);
        int outlineColor = -2146430960;
        RenderUtils.drawLineStrip2D(context, arrowVertices, outlineColor);
    }

    public static void drawPin(class_332 context, float x1, float y1, float x2, float y2, boolean hovering, boolean pinned) {
        int needleColor = hovering ? -1 : -2500135;
        int outlineColor = -2146430960;
        if (pinned) {
            float xk1 = x1 + 2.0f;
            float xk2 = x2 - 2.0f;
            float xk3 = x1 + 1.0f;
            float xk4 = x2 - 1.0f;
            float yk1 = y1 + 2.0f;
            float yk2 = y2 - 2.0f;
            float yk3 = y2 - 0.5f;
            int knobColor = hovering ? -65536 : -2555904;
            RenderUtils.fill2D(context, xk1, yk1, xk2, yk2, knobColor);
            RenderUtils.fill2D(context, xk3, yk2, xk4, yk3, knobColor);
            float xn1 = x1 + 3.5f;
            float xn2 = x2 - 3.5f;
            float yn1 = y2 - 0.5f;
            float yn2 = y2;
            RenderUtils.fill2D(context, xn1, yn1, xn2, yn2, needleColor);
            RenderUtils.drawBorder2D(context, xk1, yk1, xk2, yk2, outlineColor);
            RenderUtils.drawBorder2D(context, xk3, yk2, xk4, yk3, outlineColor);
            RenderUtils.drawBorder2D(context, xn1, yn1, xn2, yn2, outlineColor);
        } else {
            float xk1 = x2 - 3.5f;
            float xk2 = x2 - 0.5f;
            float xk3 = x2 - 3.0f;
            float xk4 = x1 + 3.0f;
            float xk5 = x1 + 2.0f;
            float xk6 = x2 - 2.0f;
            float xk7 = x1 + 1.0f;
            float yk1 = y1 + 0.5f;
            float yk2 = y1 + 3.5f;
            float yk3 = y2 - 3.0f;
            float yk4 = y1 + 3.0f;
            float yk5 = y1 + 2.0f;
            float yk6 = y2 - 2.0f;
            float yk7 = y2 - 1.0f;
            int knobColor = hovering ? -16711936 : -16721664;
            float[][] knobVertices = new float[][]{{xk4, yk4}, {xk3, yk3}, {xk2, yk2}, {xk1, yk1}, {xk5, yk5}, {xk7, yk4}, {xk3, yk7}, {xk6, yk6}};
            RenderUtils.fillQuads2D(context, knobVertices, knobColor);
            float xn1 = x1 + 3.0f;
            float xn2 = x1 + 4.0f;
            float xn3 = x1 + 1.0f;
            float yn1 = y2 - 4.0f;
            float yn2 = y2 - 3.0f;
            float yn3 = y2 - 1.0f;
            float[][] needleVertices = new float[][]{{xn3, yn3}, {xn2, yn2}, {xn1, yn1}};
            RenderUtils.fillTriangle2D(context, needleVertices, needleColor);
            float[][] knobPart1 = new float[4][2];
            System.arraycopy(knobVertices, 0, knobPart1, 0, 4);
            RenderUtils.drawLineStrip2D(context, knobPart1, outlineColor);
            float[][] knobPart2 = new float[4][2];
            System.arraycopy(knobVertices, 4, knobPart2, 0, 4);
            RenderUtils.drawLineStrip2D(context, knobPart2, outlineColor);
            RenderUtils.drawLineStrip2D(context, needleVertices, outlineColor);
        }
    }

    public static void drawCheck(class_332 context, float x1, float y1, float x2, float y2, boolean hovering, boolean grayedOut) {
        float xc1 = x1 + 2.5f;
        float xc2 = x1 + 3.5f;
        float xc3 = (x1 + x2) / 2.0f - 1.0f;
        float xc4 = x2 - 3.5f;
        float xc5 = x2 - 2.5f;
        float yc1 = y1 + 2.5f;
        float yc2 = y1 + 3.5f;
        float yc3 = (y1 + y2) / 2.0f;
        float yc4 = yc3 + 1.0f;
        float yc5 = y2 - 4.5f;
        float yc6 = y2 - 2.5f;
        int checkColor = grayedOut ? -1065320320 : (hovering ? -16711936 : -16721664);
        float[][] checkVertices = new float[][]{{xc2, yc3}, {xc1, yc4}, {xc3, yc6}, {xc3, yc5}, {xc3, yc5}, {xc3, yc6}, {xc5, yc2}, {xc4, yc1}};
        RenderUtils.fillQuads2D(context, checkVertices, checkColor);
        int outlineColor = -2146430960;
        float[][] outlineVertices = new float[][]{{xc2, yc3}, {xc3, yc5}, {xc4, yc1}, {xc5, yc2}, {xc3, yc6}, {xc1, yc4}, {xc2, yc3}};
        RenderUtils.drawLineStrip2D(context, outlineVertices, outlineColor);
    }

    public static void drawIndeterminateCheck(class_332 context, float x1, float y1, float x2, float y2, boolean hovering, boolean grayedOut) {
        float xc1 = x1 + 2.5f;
        float xc2 = x2 - 2.5f;
        float yc1 = y1 + 2.5f;
        float yc2 = y2 - 2.5f;
        int checkColor = grayedOut ? -1065320320 : (hovering ? -16711936 : -16721664);
        RenderUtils.fill2D(context, xc1, yc1, xc2, yc2, checkColor);
        int outlineColor = -2146430960;
        RenderUtils.drawBorder2D(context, xc1, yc1, xc2, yc2, outlineColor);
    }

    public static void drawCross(class_332 context, float x1, float y1, float x2, float y2, boolean hovering) {
        float xc1 = x1 + 2.0f;
        float xc2 = x1 + 3.0f;
        float xc3 = x2 - 2.0f;
        float xc4 = x2 - 3.0f;
        float xc5 = x1 + 3.5f;
        float xc6 = (x1 + x2) / 2.0f;
        float xc7 = x2 - 3.5f;
        float yc1 = y1 + 3.0f;
        float yc2 = y1 + 2.0f;
        float yc3 = y2 - 3.0f;
        float yc4 = y2 - 2.0f;
        float yc5 = y1 + 3.5f;
        float yc6 = (y1 + y2) / 2.0f;
        float yc7 = y2 - 3.5f;
        int crossColor = hovering ? -65536 : -2555904;
        float[][] crossVertices = new float[][]{{xc2, yc2}, {xc1, yc1}, {xc4, yc4}, {xc3, yc3}, {xc3, yc1}, {xc4, yc2}, {xc6, yc5}, {xc7, yc6}, {xc6, yc7}, {xc5, yc6}, {xc1, yc3}, {xc2, yc4}};
        RenderUtils.fillQuads2D(context, crossVertices, crossColor);
        int outlineColor = -2146430960;
        float[][] outlineVertices = new float[][]{{xc1, yc1}, {xc2, yc2}, {xc6, yc5}, {xc4, yc2}, {xc3, yc1}, {xc7, yc6}, {xc3, yc3}, {xc4, yc4}, {xc6, yc7}, {xc2, yc4}, {xc1, yc3}, {xc5, yc6}};
        RenderUtils.drawLineStrip2D(context, outlineVertices, outlineColor);
    }

    private static /* synthetic */ ClickGuiIcons[] $values() {
        return new ClickGuiIcons[0];
    }

    static {
        $VALUES = ClickGuiIcons.$values();
    }
}
