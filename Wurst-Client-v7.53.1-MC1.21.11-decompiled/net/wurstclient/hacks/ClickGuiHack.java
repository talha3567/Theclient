package net.wurstclient.hacks;

import java.awt.Color;
import net.minecraft.class_437;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.clickgui.screens.ClickGuiScreen;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.SliderSetting;

@DontSaveState
@DontBlock
@SearchTags(value={"click gui", "WindowGUI", "window gui", "HackMenu", "hack menu"})
public final class ClickGuiHack
extends Hack {
    private final ColorSetting bgColor = new ColorSetting("Background", "Background color", new Color(0x404040));
    private final ColorSetting acColor = new ColorSetting("Accent", "Accent color", new Color(0x101010));
    private final ColorSetting txtColor = new ColorSetting("Text", "Text color", new Color(0xF0F0F0));
    private final SliderSetting opacity = new SliderSetting("Opacity", 0.5, 0.15, 0.85, 0.01, SliderSetting.ValueDisplay.PERCENTAGE);
    private final SliderSetting ttOpacity = new SliderSetting("Tooltip opacity", 0.75, 0.15, 1.0, 0.01, SliderSetting.ValueDisplay.PERCENTAGE);
    private final SliderSetting maxHeight = new SliderSetting("Max height", "Maximum window height\n0 = no limit", 200.0, 0.0, 1000.0, 50.0, SliderSetting.ValueDisplay.INTEGER);
    private final SliderSetting maxSettingsHeight = new SliderSetting("Max settings height", "Maximum height for settings windows\n0 = no limit", 200.0, 0.0, 1000.0, 50.0, SliderSetting.ValueDisplay.INTEGER);

    public ClickGuiHack() {
        super("ClickGUI");
        this.addSetting(this.bgColor);
        this.addSetting(this.acColor);
        this.addSetting(this.txtColor);
        this.addSetting(this.opacity);
        this.addSetting(this.ttOpacity);
        this.addSetting(this.maxHeight);
        this.addSetting(this.maxSettingsHeight);
    }

    @Override
    protected void onEnable() {
        MC.method_1507((class_437)new ClickGuiScreen(WURST.getGui()));
        this.setEnabled(false);
    }

    public float[] getBackgroundColor() {
        return this.bgColor.getColorF();
    }

    public float[] getAccentColor() {
        return this.acColor.getColorF();
    }

    public int getTextColor() {
        return this.txtColor.getColorI();
    }

    public float getOpacity() {
        return this.opacity.getValueF();
    }

    public float getTooltipOpacity() {
        return this.ttOpacity.getValueF();
    }

    public int getMaxHeight() {
        return this.maxHeight.getValueI();
    }

    public int getMaxSettingsHeight() {
        return this.maxSettingsHeight.getValueI();
    }
}
