package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"no overlay", "NoWaterOverlay", "no water overlay", "NoSnowOverlay", "no snow overlay"})
public final class NoOverlayHack
extends Hack {
    public NoOverlayHack() {
        super("NoOverlay");
        this.setCategory(Category.RENDER);
    }
}
