package net.wurstclient.ai;

import net.minecraft.class_2338;
import net.minecraft.class_2382;

public class PathPos
extends class_2338 {
    private final boolean jumping;

    public PathPos(class_2338 pos) {
        this(pos, false);
    }

    public PathPos(class_2338 pos, boolean jumping) {
        super((class_2382)pos);
        this.jumping = jumping;
    }

    public boolean isJumping() {
        return this.jumping;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PathPos)) {
            return false;
        }
        PathPos node = (PathPos)((Object)obj);
        return this.method_10263() == node.method_10263() && this.method_10264() == node.method_10264() && this.method_10260() == node.method_10260() && this.isJumping() == node.isJumping();
    }

    public int hashCode() {
        return super.hashCode() * 2 + (this.isJumping() ? 1 : 0);
    }
}
