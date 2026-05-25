package com.pulseclient.modules.render;

import com.pulseclient.Category;
import com.pulseclient.Module;
import com.pulseclient.gui.ClickGuiScreen;
import net.minecraft.client.Minecraft;

public class PulseClickGui extends Module {
    public PulseClickGui() {
        super("ClickGUI", "Opens the ClickGUI. Press Right Shift.", Category.RENDER);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setScreen(new ClickGuiScreen());
        }
        this.setEnabled(false); // Toggle off immediately so it can be re-opened
    }
}
