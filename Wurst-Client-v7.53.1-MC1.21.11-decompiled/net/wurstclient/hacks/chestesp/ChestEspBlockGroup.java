package net.wurstclient.hacks.chestesp;

import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2680;
import net.minecraft.class_2745;
import net.minecraft.class_2769;
import net.wurstclient.hacks.chestesp.ChestEspGroup;
import net.wurstclient.util.BlockUtils;

public abstract class ChestEspBlockGroup
extends ChestEspGroup {
    protected abstract boolean matches(class_2586 var1);

    public final void addIfMatches(class_2586 be) {
        if (!this.matches(be)) {
            return;
        }
        class_238 box = this.getBox(be);
        if (box == null) {
            return;
        }
        this.boxes.add(box);
    }

    private class_238 getBox(class_2586 be) {
        class_2338 pos = be.method_11016();
        if (!BlockUtils.canBeClicked(pos)) {
            return null;
        }
        if (be instanceof class_2595) {
            return this.getChestBox((class_2595)be);
        }
        return BlockUtils.getBoundingBox(pos);
    }

    private class_238 getChestBox(class_2595 chestBE) {
        class_2338 pos2;
        class_2680 state = chestBE.method_11010();
        if (!state.method_28498((class_2769)class_2281.field_10770)) {
            return null;
        }
        class_2745 chestType = (class_2745)state.method_11654((class_2769)class_2281.field_10770);
        if (chestType == class_2745.field_12574) {
            return null;
        }
        class_2338 pos = chestBE.method_11016();
        class_238 box = BlockUtils.getBoundingBox(pos);
        if (chestType != class_2745.field_12569 && BlockUtils.canBeClicked(pos2 = pos.method_10093(class_2281.method_9758((class_2680)state)))) {
            class_238 box2 = BlockUtils.getBoundingBox(pos2);
            box = box.method_991(box2);
        }
        return box;
    }
}
