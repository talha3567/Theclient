package net.wurstclient.hacks;

import java.util.Arrays;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1922;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2346;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2682;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"scaffold walk", "BridgeWalk", "bridge walk", "AutoBridge", "auto bridge", "tower"})
public final class ScaffoldWalkHack
extends Hack
implements UpdateListener {
    public ScaffoldWalkHack() {
        super("ScaffoldWalk");
        this.setCategory(Category.BLOCKS);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        class_2338 belowPlayer = class_2338.method_49638((class_2374)ScaffoldWalkHack.MC.field_1724.method_73189()).method_10074();
        if (!BlockUtils.getState(belowPlayer).method_45474()) {
            return;
        }
        int newSlot = -1;
        for (int i = 0; i < 9; ++i) {
            class_2248 block;
            class_2680 state;
            class_1799 stack = ScaffoldWalkHack.MC.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || !(stack.method_7909() instanceof class_1747) || !(state = (block = class_2248.method_9503((class_1792)stack.method_7909())).method_9564()).method_26234((class_1922)class_2682.field_12294, class_2338.field_10980) || block instanceof class_2346 && class_2346.method_10128((class_2680)BlockUtils.getState(belowPlayer.method_10074()))) continue;
            newSlot = i;
            break;
        }
        if (newSlot == -1) {
            return;
        }
        int oldSlot = ScaffoldWalkHack.MC.field_1724.method_31548().method_67532();
        ScaffoldWalkHack.MC.field_1724.method_31548().method_61496(newSlot);
        this.scaffoldTo(belowPlayer);
        ScaffoldWalkHack.MC.field_1724.method_31548().method_61496(oldSlot);
    }

    private void scaffoldTo(class_2338 belowPlayer) {
        class_2350[] sides;
        if (this.placeBlock(belowPlayer)) {
            return;
        }
        for (class_2350 side : sides = class_2350.values()) {
            class_2338 neighbor = belowPlayer.method_10093(side);
            if (!this.placeBlock(neighbor)) continue;
            return;
        }
        for (class_2350 side : sides) {
            for (class_2350 side2 : Arrays.copyOfRange(sides, side.ordinal(), 6)) {
                class_2338 neighbor;
                if (side.method_10153().equals((Object)side2) || !this.placeBlock(neighbor = belowPlayer.method_10093(side).method_10093(side2))) continue;
                return;
            }
        }
    }

    private boolean placeBlock(class_2338 pos) {
        class_243 eyesPos = RotationUtils.getEyesPos();
        for (class_2350 side : class_2350.values()) {
            class_243 hitVec;
            class_2338 neighbor = pos.method_10093(side);
            class_2350 side2 = side.method_10153();
            if (eyesPos.method_1025(class_243.method_24953((class_2382)pos)) >= eyesPos.method_1025(class_243.method_24953((class_2382)neighbor)) || !BlockUtils.canBeClicked(neighbor) || eyesPos.method_1025(hitVec = class_243.method_24953((class_2382)neighbor).method_1019(class_243.method_24954((class_2382)side2.method_62675()).method_1021(0.5))) > 18.0625) continue;
            RotationUtils.getNeededRotations(hitVec).sendPlayerLookPacket();
            IMC.getInteractionManager().rightClickBlock(neighbor, side2, hitVec);
            ScaffoldWalkHack.MC.field_1724.method_6104(class_1268.field_5808);
            ScaffoldWalkHack.MC.field_1752 = 4;
            return true;
        }
        return false;
    }
}
