package net.wurstclient.util;

import net.minecraft.class_2586;

public final class LootrModCompat
extends Enum<LootrModCompat> {
    private static final Class<?> lootrBarrelClass;
    private static final Class<?> lootrShulkerBoxClass;
    private static final Class<?> lootrTrappedChestClass;
    private static final /* synthetic */ LootrModCompat[] $VALUES;

    public static LootrModCompat[] values() {
        return (LootrModCompat[])$VALUES.clone();
    }

    public static LootrModCompat valueOf(String name) {
        return Enum.valueOf(LootrModCompat.class, name);
    }

    public static boolean isLootrBarrel(class_2586 blockEntity) {
        if (blockEntity == null || lootrBarrelClass == null) {
            return false;
        }
        return lootrBarrelClass.isInstance(blockEntity);
    }

    public static boolean isLootrShulkerBox(class_2586 blockEntity) {
        if (blockEntity == null || lootrShulkerBoxClass == null) {
            return false;
        }
        return lootrShulkerBoxClass.isInstance(blockEntity);
    }

    public static boolean isLootrTrappedChest(class_2586 blockEntity) {
        if (blockEntity == null || lootrTrappedChestClass == null) {
            return false;
        }
        return lootrTrappedChestClass.isInstance(blockEntity);
    }

    private static Class<?> getClassIfExists(String name) {
        try {
            return Class.forName(name, false, LootrModCompat.class.getClassLoader());
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static /* synthetic */ LootrModCompat[] $values() {
        return new LootrModCompat[0];
    }

    static {
        $VALUES = LootrModCompat.$values();
        lootrBarrelClass = LootrModCompat.getClassIfExists("noobanidus.mods.lootr.common.block.entity.LootrBarrelBlockEntity");
        lootrShulkerBoxClass = LootrModCompat.getClassIfExists("noobanidus.mods.lootr.common.block.entity.LootrShulkerBlockEntity");
        lootrTrappedChestClass = LootrModCompat.getClassIfExists("noobanidus.mods.lootr.common.block.entity.LootrTrappedChestBlockEntity");
    }
}
