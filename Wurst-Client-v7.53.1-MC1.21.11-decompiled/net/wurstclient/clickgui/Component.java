package net.wurstclient.clickgui;

import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Window;

public abstract class Component {
    protected static final class_310 MC = WurstClient.MC;
    protected static final WurstClient WURST = WurstClient.INSTANCE;
    private int x;
    private int y;
    private int width;
    private int height;
    private Window parent;

    public void handleMouseClick(double mouseX, double mouseY, int mouseButton, class_11909 context) {
    }

    public abstract void render(class_332 var1, int var2, int var3, float var4);

    public abstract int getDefaultWidth();

    public abstract int getDefaultHeight();

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        if (this.x != x) {
            this.invalidateParent();
        }
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        if (this.y != y) {
            this.invalidateParent();
        }
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        if (this.width != width) {
            this.invalidateParent();
        }
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        if (this.height != height) {
            this.invalidateParent();
        }
        this.height = height;
    }

    public Window getParent() {
        return this.parent;
    }

    public void setParent(Window parent) {
        this.parent = parent;
    }

    private void invalidateParent() {
        if (this.parent != null) {
            this.parent.invalidate();
        }
    }

    protected boolean isHovering(int mouseX, int mouseY) {
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        Window parent = this.getParent();
        boolean scrollEnabled = parent.isScrollingEnabled();
        int scroll = scrollEnabled ? parent.getScrollOffset() : 0;
        return mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2 && mouseY >= -scroll && mouseY < parent.getHeight() - 13 - scroll;
    }
}
