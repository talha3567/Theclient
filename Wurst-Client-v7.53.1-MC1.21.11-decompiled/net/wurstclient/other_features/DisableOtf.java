package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags(value={"turn off", "hide wurst logo", "ghost mode", "stealth mode", "vanilla Minecraft"})
@DontBlock
public final class DisableOtf
extends OtherFeature {
    private final CheckboxSetting hideEnableButton = new CheckboxSetting("Hide enable button", "Removes the \"Enable Wurst\" button as soon as you close the Statistics screen. You will have to restart the game to re-enable Wurst.", false);

    public DisableOtf() {
        super("DisableWurst", "To disable Wurst, go to the Statistics screen and press the \"Disable Wurst\" button.\nIt will turn into an \"Enable Wurst\" button once pressed.");
        this.addSetting(this.hideEnableButton);
    }

    public boolean shouldHideEnableButton() {
        return !WURST.isEnabled() && this.hideEnableButton.isChecked();
    }
}
