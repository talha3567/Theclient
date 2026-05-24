package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"no fog", "AntiFog", "anti fog"})
public final class NoFogHack
extends Hack {
    public NoFogHack() {
        super("NoFog");
        this.setCategory(Category.RENDER);
    }
}
