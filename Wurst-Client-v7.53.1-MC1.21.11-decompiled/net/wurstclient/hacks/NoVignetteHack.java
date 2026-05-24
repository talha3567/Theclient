package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"no vignette", "AntiVignette", "anti vignette"})
public final class NoVignetteHack
extends Hack {
    public NoVignetteHack() {
        super("NoVignette");
        this.setCategory(Category.RENDER);
    }
}
