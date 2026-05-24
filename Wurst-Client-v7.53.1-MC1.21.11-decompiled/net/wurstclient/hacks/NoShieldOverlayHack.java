package net.wurstclient.hacks;

import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

public final class NoShieldOverlayHack
extends Hack {
    public final SliderSetting blockingOffset = new SliderSetting("Blocking offset", "The amount to lower the shield overlay by when blocking.", 0.5, 0.0, 0.8, 0.01, SliderSetting.ValueDisplay.DECIMAL);
    public final SliderSetting nonBlockingOffset = new SliderSetting("Non-blocking offset", "The amount to lower the shield overlay when not blocking.", 0.2, 0.0, 0.5, 0.01, SliderSetting.ValueDisplay.DECIMAL);

    public NoShieldOverlayHack() {
        super("NoShieldOverlay");
        this.setCategory(Category.RENDER);
        this.addSetting(this.blockingOffset);
        this.addSetting(this.nonBlockingOffset);
    }

    public void adjustShieldPosition(class_4587 matrixStack, boolean blocking) {
        if (!this.isEnabled()) {
            return;
        }
        if (blocking) {
            matrixStack.method_22904(0.0, -this.blockingOffset.getValue(), 0.0);
        } else {
            matrixStack.method_22904(0.0, -this.nonBlockingOffset.getValue(), 0.0);
        }
    }
}
