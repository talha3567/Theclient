package net.wurstclient.other_features;

import net.minecraft.class_437;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.options.KeybindManagerScreen;
import net.wurstclient.other_feature.OtherFeature;

@SearchTags(value={"KeybindManager", "keybind manager", "KeybindsManager", "keybinds manager"})
@DontBlock
public final class KeybindManagerOtf
extends OtherFeature {
    public KeybindManagerOtf() {
        super("Keybinds", "This is just a shortcut to let you open the Keybind Manager from within the GUI. Normally you would go to Wurst Options > Keybinds.");
    }

    @Override
    public String getPrimaryAction() {
        return "Open Keybind Manager";
    }

    @Override
    public void doPrimaryAction() {
        MC.method_1507((class_437)new KeybindManagerScreen(KeybindManagerOtf.MC.field_1755));
    }
}
