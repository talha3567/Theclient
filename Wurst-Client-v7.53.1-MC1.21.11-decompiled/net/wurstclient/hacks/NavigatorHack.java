package net.wurstclient.hacks;

import net.minecraft.class_437;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.navigator.NavigatorMainScreen;

@DontSaveState
@DontBlock
@SearchTags(value={"ClickGUI", "click gui", "SearchGUI", "search gui", "HackMenu", "hack menu"})
public final class NavigatorHack
extends Hack {
    public NavigatorHack() {
        super("Navigator");
    }

    @Override
    protected void onEnable() {
        if (!(NavigatorHack.MC.field_1755 instanceof NavigatorMainScreen)) {
            MC.method_1507((class_437)new NavigatorMainScreen());
        }
        this.setEnabled(false);
    }
}
