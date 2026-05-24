package net.wurstclient.hacks.chestesp;

import java.util.ArrayList;
import net.minecraft.class_1297;
import net.wurstclient.hacks.chestesp.ChestEspGroup;
import net.wurstclient.util.EntityUtils;

public abstract class ChestEspEntityGroup
extends ChestEspGroup {
    private final ArrayList<class_1297> entities = new ArrayList();

    protected abstract boolean matches(class_1297 var1);

    public final void addIfMatches(class_1297 e) {
        if (!this.matches(e)) {
            return;
        }
        this.entities.add(e);
    }

    @Override
    public final void clear() {
        this.entities.clear();
        super.clear();
    }

    public final void updateBoxes(float partialTicks) {
        this.boxes.clear();
        for (class_1297 e : this.entities) {
            this.boxes.add(EntityUtils.getLerpedBox(e, partialTicks));
        }
    }
}
