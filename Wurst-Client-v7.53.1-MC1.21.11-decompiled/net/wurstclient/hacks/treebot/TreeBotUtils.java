package net.wurstclient.hacks.treebot;

import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.wurstclient.util.BlockUtils;

public final class TreeBotUtils
extends Enum<TreeBotUtils> {
    private static final /* synthetic */ TreeBotUtils[] $VALUES;

    public static TreeBotUtils[] values() {
        return (TreeBotUtils[])$VALUES.clone();
    }

    public static TreeBotUtils valueOf(String name) {
        return Enum.valueOf(TreeBotUtils.class, name);
    }

    public static boolean isLog(class_2338 pos) {
        return BlockUtils.getState(pos).method_26164(class_3481.field_15475);
    }

    public static boolean isLeaves(class_2338 pos) {
        class_2680 state = BlockUtils.getState(pos);
        return state.method_26164(class_3481.field_15503) || state.method_26164(class_3481.field_21954);
    }

    private static /* synthetic */ TreeBotUtils[] $values() {
        return new TreeBotUtils[0];
    }

    static {
        $VALUES = TreeBotUtils.$values();
    }
}
