package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"portal gui"})
public final class PortalGuiHack
extends Hack {
    public PortalGuiHack() {
        super("PortalGUI");
        this.setCategory(Category.OTHER);
    }
}
