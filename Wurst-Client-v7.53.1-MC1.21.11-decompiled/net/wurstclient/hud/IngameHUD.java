package net.wurstclient.hud;

import net.minecraft.class_332;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.screens.ClickGuiScreen;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.hud.HackListHUD;
import net.wurstclient.hud.TabGui;
import net.wurstclient.hud.WurstLogo;

public final class IngameHUD
implements GUIRenderListener {
    private final WurstLogo wurstLogo = new WurstLogo();
    private final HackListHUD hackList = new HackListHUD();
    private TabGui tabGui;

    @Override
    public void onRenderGUI(class_332 context, float partialTicks) {
        if (!WurstClient.INSTANCE.isEnabled()) {
            return;
        }
        if (this.tabGui == null) {
            this.tabGui = new TabGui();
        }
        ClickGui clickGui = WurstClient.INSTANCE.getGui();
        clickGui.updateColors();
        this.wurstLogo.render(context);
        this.hackList.render(context, partialTicks);
        this.tabGui.render(context, partialTicks);
        if (!(WurstClient.MC.field_1755 instanceof ClickGuiScreen)) {
            clickGui.renderPinnedWindows(context, partialTicks);
        }
    }

    public HackListHUD getHackList() {
        return this.hackList;
    }
}
