package net.wurstclient.hacks;

import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_465;
import net.minecraft.class_481;
import net.minecraft.class_490;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IClientPlayerInteractionManager;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.InventoryUtils;

@SearchTags(value={"auto totem", "offhand", "off-hand"})
public final class AutoTotemHack
extends Hack
implements UpdateListener {
    private final CheckboxSetting showCounter = new CheckboxSetting("Show totem counter", "Displays the number of totems you have.", true);
    private final SliderSetting delay = new SliderSetting("Delay", "Amount of ticks to wait before equipping the next totem.", 0.0, 0.0, 20.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final SliderSetting health = new SliderSetting("Health", "Won't equip a totem until your health reaches this value or falls below it.\n0 = always active", 0.0, 0.0, 10.0, 0.5, SliderSetting.ValueDisplay.DECIMAL.withSuffix(" hearts").withLabel(1.0, "1 heart").withLabel(0.0, "ignore"));
    private int nextTickSlot;
    private int totems;
    private int timer;
    private boolean wasTotemInOffhand;

    public AutoTotemHack() {
        super("AutoTotem");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.showCounter);
        this.addSetting(this.delay);
        this.addSetting(this.health);
    }

    @Override
    public String getRenderName() {
        if (!this.showCounter.isChecked()) {
            return this.getName();
        }
        if (this.totems == 1) {
            return this.getName() + " [1 totem]";
        }
        return this.getName() + " [" + this.totems + " totems]";
    }

    @Override
    protected void onEnable() {
        this.nextTickSlot = -1;
        this.totems = 0;
        this.timer = 0;
        this.wasTotemInOffhand = false;
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        this.finishMovingTotem();
        int nextTotemSlot = this.searchForTotems();
        if (this.isTotem(AutoTotemHack.MC.field_1724.method_6079())) {
            this.wasTotemInOffhand = true;
            return;
        }
        if (this.wasTotemInOffhand) {
            this.timer = this.delay.getValueI();
            this.wasTotemInOffhand = false;
        }
        if (nextTotemSlot == -1) {
            return;
        }
        float healthF = this.health.getValueF();
        if (healthF > 0.0f && AutoTotemHack.MC.field_1724.method_6032() > healthF * 2.0f) {
            return;
        }
        if (AutoTotemHack.MC.field_1755 instanceof class_465 && !(AutoTotemHack.MC.field_1755 instanceof class_490) && !(AutoTotemHack.MC.field_1755 instanceof class_481)) {
            return;
        }
        if (this.timer > 0) {
            --this.timer;
            return;
        }
        this.moveToOffhand(nextTotemSlot);
    }

    private void moveToOffhand(int itemSlot) {
        boolean offhandEmpty = AutoTotemHack.MC.field_1724.method_6079().method_7960();
        IClientPlayerInteractionManager im = IMC.getInteractionManager();
        im.windowClick_PICKUP(itemSlot);
        im.windowClick_PICKUP(45);
        if (!offhandEmpty) {
            this.nextTickSlot = itemSlot;
        }
    }

    private void finishMovingTotem() {
        if (this.nextTickSlot == -1) {
            return;
        }
        IClientPlayerInteractionManager im = IMC.getInteractionManager();
        im.windowClick_PICKUP(this.nextTickSlot);
        this.nextTickSlot = -1;
    }

    private int searchForTotems() {
        this.totems = InventoryUtils.count(this::isTotem, 40, true);
        if (this.totems <= 0) {
            return -1;
        }
        int totemSlot = InventoryUtils.indexOf(this::isTotem, 40);
        return InventoryUtils.toNetworkSlot(totemSlot);
    }

    private boolean isTotem(class_1799 stack) {
        return stack.method_31574(class_1802.field_8288);
    }
}
