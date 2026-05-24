package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"camera noclip", "camera no clip"})
public final class CameraNoClipHack
extends Hack {
    public CameraNoClipHack() {
        super("CameraNoClip");
        this.setCategory(Category.RENDER);
    }
}
