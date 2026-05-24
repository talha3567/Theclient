package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;

@SearchTags(value={"auto sign"})
@DontSaveState
public final class AutoSignHack
extends Hack {
    private String[] signText;

    public AutoSignHack() {
        super("AutoSign");
        this.setCategory(Category.BLOCKS);
    }

    @Override
    protected void onDisable() {
        this.signText = null;
    }

    public String[] getSignText() {
        return this.signText;
    }

    public void setSignText(String[] signText) {
        if (this.isEnabled() && this.signText == null) {
            this.signText = signText;
        }
    }
}
