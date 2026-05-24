package net.wurstclient.mixinterface;

import net.minecraft.class_304;

public interface IKeyMapping {
    default public boolean isActuallyDown() {
        return this.wurst_isActuallyDown();
    }

    default public void resetPressedState() {
        this.wurst_resetPressedState();
    }

    default public void simulatePress(boolean pressed) {
        this.wurst_simulatePress(pressed);
    }

    default public void method_23481(boolean down) {
        this.asVanilla().method_23481(down);
    }

    default public class_304 asVanilla() {
        return (class_304)this;
    }

    public static IKeyMapping get(class_304 kb) {
        return (IKeyMapping)kb;
    }

    @Deprecated
    public boolean wurst_isActuallyDown();

    @Deprecated
    public void wurst_resetPressedState();

    @Deprecated
    public void wurst_simulatePress(boolean var1);
}
