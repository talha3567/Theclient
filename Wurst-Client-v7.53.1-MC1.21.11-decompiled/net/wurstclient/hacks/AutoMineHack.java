package net.wurstclient.hacks;

import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_239;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_636;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.HandleBlockBreakingListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags(value={"auto mine", "AutoBreak", "auto break"})
public final class AutoMineHack
extends Hack
implements UpdateListener,
HandleBlockBreakingListener {
    private final CheckboxSetting superFastMode = new CheckboxSetting("Super fast mode", "Breaks blocks faster than you normally could. May get detected by anti-cheat plugins.", false);

    public AutoMineHack() {
        super("AutoMine");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.superFastMode);
    }

    @Override
    protected void onEnable() {
        AutoMineHack.WURST.getHax().autoFarmHack.setEnabled(false);
        AutoMineHack.WURST.getHax().excavatorHack.setEnabled(false);
        AutoMineHack.WURST.getHax().nukerHack.setEnabled(false);
        AutoMineHack.WURST.getHax().nukerLegitHack.setEnabled(false);
        AutoMineHack.WURST.getHax().speedNukerHack.setEnabled(false);
        AutoMineHack.WURST.getHax().tunnellerHack.setEnabled(false);
        AutoMineHack.WURST.getHax().veinMinerHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(HandleBlockBreakingListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(HandleBlockBreakingListener.class, this);
        IKeyMapping.get(AutoMineHack.MC.field_1690.field_1886).resetPressedState();
        AutoMineHack.MC.field_1761.method_2925();
    }

    @Override
    public void onUpdate() {
        class_636 im = AutoMineHack.MC.field_1761;
        if (AutoMineHack.MC.field_1724.method_3144()) {
            im.method_2925();
            return;
        }
        class_239 hitResult = AutoMineHack.MC.field_1765;
        if (hitResult == null || hitResult.method_17783() != class_239.class_240.field_1332 || !(hitResult instanceof class_3965)) {
            im.method_2925();
            return;
        }
        class_3965 bHitResult = (class_3965)hitResult;
        class_2338 pos = bHitResult.method_17777();
        class_2680 state = AutoMineHack.MC.field_1687.method_8320(pos);
        class_2350 side = bHitResult.method_17780();
        if (state.method_26215()) {
            im.method_2925();
            return;
        }
        AutoMineHack.WURST.getHax().autoToolHack.equipIfEnabled(pos);
        if (AutoMineHack.MC.field_1724.method_6115()) {
            return;
        }
        if (!im.method_2923()) {
            im.method_2910(pos, side);
        }
        if (im.method_2902(pos, side)) {
            AutoMineHack.MC.field_1687.method_74254(pos, side);
            AutoMineHack.MC.field_1724.method_6104(class_1268.field_5808);
            AutoMineHack.MC.field_1690.field_1886.method_23481(true);
        }
    }

    @Override
    public void onHandleBlockBreaking(HandleBlockBreakingListener.HandleBlockBreakingEvent event) {
        if (!this.superFastMode.isChecked()) {
            event.cancel();
        }
    }
}
