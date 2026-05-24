package net.wurstclient.hacks;

import java.util.ArrayList;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"instant bunker"})
public final class InstantBunkerHack
extends Hack
implements UpdateListener {
    private final int[][] template = new int[][]{{2, 0, 2}, {-2, 0, 2}, {2, 0, -2}, {-2, 0, -2}, {2, 1, 2}, {-2, 1, 2}, {2, 1, -2}, {-2, 1, -2}, {2, 2, 2}, {-2, 2, 2}, {2, 2, -2}, {-2, 2, -2}, {1, 2, 2}, {0, 2, 2}, {-1, 2, 2}, {2, 2, 1}, {2, 2, 0}, {2, 2, -1}, {-2, 2, 1}, {-2, 2, 0}, {-2, 2, -1}, {1, 2, -2}, {0, 2, -2}, {-1, 2, -2}, {1, 0, 2}, {0, 0, 2}, {-1, 0, 2}, {2, 0, 1}, {2, 0, 0}, {2, 0, -1}, {-2, 0, 1}, {-2, 0, 0}, {-2, 0, -1}, {1, 0, -2}, {0, 0, -2}, {-1, 0, -2}, {1, 1, 2}, {0, 1, 2}, {-1, 1, 2}, {2, 1, 1}, {2, 1, 0}, {2, 1, -1}, {-2, 1, 1}, {-2, 1, 0}, {-2, 1, -1}, {1, 1, -2}, {0, 1, -2}, {-1, 1, -2}, {1, 2, 1}, {-1, 2, 1}, {1, 2, -1}, {-1, 2, -1}, {0, 2, 1}, {1, 2, 0}, {-1, 2, 0}, {0, 2, -1}, {0, 2, 0}};
    private final ArrayList<class_2338> positions = new ArrayList();
    private int startTimer;

    public InstantBunkerHack() {
        super("InstantBunker");
        this.setCategory(Category.BLOCKS);
    }

    @Override
    protected void onEnable() {
        InstantBunkerHack.WURST.getHax().tunnellerHack.setEnabled(false);
        if (!InstantBunkerHack.MC.field_1724.method_24828()) {
            ChatUtils.error("Can't build this in mid-air.");
            this.setEnabled(false);
            return;
        }
        class_1799 stack = InstantBunkerHack.MC.field_1724.method_31548().method_7391();
        if (!(stack.method_7909() instanceof class_1747)) {
            ChatUtils.error("You must have blocks in the main hand.");
            this.setEnabled(false);
            return;
        }
        if (stack.method_7947() < 57 && !InstantBunkerHack.MC.field_1724.method_31549().field_7477) {
            ChatUtils.warning("Not enough blocks. Bunker may be incomplete.");
        }
        class_2338 startPos = class_2338.method_49638((class_2374)InstantBunkerHack.MC.field_1724.method_73189());
        class_2350 facing = InstantBunkerHack.MC.field_1724.method_5735();
        class_2350 facing2 = facing.method_10160();
        this.positions.clear();
        for (int[] pos : this.template) {
            this.positions.add(startPos.method_10086(pos[1]).method_10079(facing, pos[2]).method_10079(facing2, pos[0]));
        }
        this.startTimer = 2;
        InstantBunkerHack.MC.field_1724.method_6043();
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (this.startTimer > 0) {
            --this.startTimer;
            return;
        }
        if (this.startTimer <= 0) {
            for (class_2338 pos : this.positions) {
                if (!BlockUtils.getState(pos).method_45474() || InstantBunkerHack.MC.field_1724.method_5829().method_994(new class_238(pos))) continue;
                this.placeBlockSimple(pos);
            }
            InstantBunkerHack.MC.field_1724.method_6104(class_1268.field_5808);
            if (InstantBunkerHack.MC.field_1724.method_24828()) {
                this.setEnabled(false);
            }
        }
    }

    private void placeBlockSimple(class_2338 pos) {
        int i;
        class_2350 side = null;
        class_2350[] sides = class_2350.values();
        class_243 eyesPos = RotationUtils.getEyesPos();
        class_243 posVec = class_243.method_24953((class_2382)pos);
        double distanceSqPosVec = eyesPos.method_1025(posVec);
        class_243[] hitVecs = new class_243[sides.length];
        for (i = 0; i < sides.length; ++i) {
            hitVecs[i] = posVec.method_1019(class_243.method_24954((class_2382)sides[i].method_62675()).method_1021(0.5));
        }
        for (i = 0; i < sides.length; ++i) {
            class_2680 neighborState;
            class_265 neighborShape;
            class_2338 neighbor = pos.method_10093(sides[i]);
            if (!BlockUtils.canBeClicked(neighbor) || InstantBunkerHack.MC.field_1687.method_17745(eyesPos, hitVecs[i], neighbor, neighborShape = (neighborState = BlockUtils.getState(neighbor)).method_26218((class_1922)InstantBunkerHack.MC.field_1687, neighbor), neighborState) != null) continue;
            side = sides[i];
            break;
        }
        if (side == null) {
            for (i = 0; i < sides.length; ++i) {
                if (!BlockUtils.canBeClicked(pos.method_10093(sides[i])) || distanceSqPosVec > eyesPos.method_1025(hitVecs[i])) continue;
                side = sides[i];
                break;
            }
        }
        if (side == null) {
            return;
        }
        class_243 hitVec = hitVecs[side.ordinal()];
        IMC.getInteractionManager().rightClickBlock(pos.method_10093(side), side.method_10153(), hitVec);
        SwingHandSetting.SwingHand.SERVER.swing(class_1268.field_5808);
        InstantBunkerHack.MC.field_1752 = 4;
    }
}
