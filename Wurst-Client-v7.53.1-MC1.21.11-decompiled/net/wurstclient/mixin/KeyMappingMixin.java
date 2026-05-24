package net.wurstclient.mixin;

import net.minecraft.class_1041;
import net.minecraft.class_11908;
import net.minecraft.class_11910;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.wurstclient.WurstClient;
import net.wurstclient.mixinterface.IKeyMapping;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={class_304.class})
public abstract class KeyMappingMixin
implements IKeyMapping {
    @Shadow
    private class_3675.class_306 field_1655;

    @Override
    @Unique
    @Deprecated
    public boolean wurst_isActuallyDown() {
        class_1041 window = WurstClient.MC.method_22683();
        int code = this.field_1655.method_1444();
        if (this.field_1655.method_1442() == class_3675.class_307.field_1672) {
            return GLFW.glfwGetMouseButton((long)window.method_4490(), (int)code) == 1;
        }
        return class_3675.method_15987((class_1041)window, (int)code);
    }

    @Override
    @Unique
    @Deprecated
    public void wurst_resetPressedState() {
        this.method_23481(this.wurst_isActuallyDown());
    }

    @Override
    @Unique
    @Deprecated
    public void wurst_simulatePress(boolean pressed) {
        class_310 mc = WurstClient.MC;
        class_1041 window = mc.method_22683();
        int action = pressed ? 1 : 0;
        switch (this.field_1655.method_1442()) {
            case field_1668: {
                mc.field_1774.method_1466(window.method_4490(), action, new class_11908(this.field_1655.method_1444(), 0, 0));
                break;
            }
            case field_1671: {
                mc.field_1774.method_1466(window.method_4490(), action, new class_11908(-1, this.field_1655.method_1444(), 0));
                break;
            }
            case field_1672: {
                mc.field_1729.method_1601(window.method_4490(), new class_11910(this.field_1655.method_1444(), 0), action);
                break;
            }
            default: {
                System.out.println("Unknown key mapping type: " + String.valueOf(this.field_1655.method_1442()));
            }
        }
    }

    @Override
    @Shadow
    public abstract void method_23481(boolean var1);
}
