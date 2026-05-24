package net.wurstclient.ai;

import java.util.ArrayList;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_2382;
import net.minecraft.class_2399;
import net.minecraft.class_243;
import net.minecraft.class_2541;
import net.minecraft.class_3532;
import net.wurstclient.WurstClient;
import net.wurstclient.ai.PathPos;
import net.wurstclient.ai.PathProcessor;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

public class WalkPathProcessor
extends PathProcessor {
    public WalkPathProcessor(ArrayList<PathPos> path) {
        super(path);
    }

    @Override
    public void process() {
        class_2338 pos = WurstClient.MC.field_1724.method_24828() ? class_2338.method_49637((double)WurstClient.MC.field_1724.method_23317(), (double)(WurstClient.MC.field_1724.method_23318() + 0.5), (double)WurstClient.MC.field_1724.method_23321()) : class_2338.method_49638((class_2374)WurstClient.MC.field_1724.method_73189());
        PathPos nextPos = (PathPos)((Object)this.path.get(this.index));
        int posIndex = this.path.indexOf(pos);
        this.ticksOffPath = posIndex == -1 ? ++this.ticksOffPath : 0;
        if (pos.equals((Object)nextPos)) {
            ++this.index;
            if (this.index >= this.path.size()) {
                this.done = true;
            }
            return;
        }
        if (posIndex > this.index) {
            this.index = posIndex + 1;
            if (this.index >= this.path.size()) {
                this.done = true;
            }
            return;
        }
        WalkPathProcessor.lockControls();
        WurstClient.MC.field_1724.method_31549().field_7479 = false;
        this.facePosition(nextPos);
        if (class_3532.method_15393((float)Math.abs(RotationUtils.getHorizontalAngleToLookVec(class_243.method_24953((class_2382)nextPos)))) > 90.0f) {
            return;
        }
        if (WalkPathProcessor.WURST.getHax().jesusHack.isEnabled()) {
            if (WurstClient.MC.field_1724.method_23318() < (double)nextPos.method_10264() && (WurstClient.MC.field_1724.method_5799() || WurstClient.MC.field_1724.method_5771())) {
                return;
            }
            if (WurstClient.MC.field_1724.method_23318() - (double)nextPos.method_10264() > 0.5 && (WurstClient.MC.field_1724.method_5799() || WurstClient.MC.field_1724.method_5771() || WalkPathProcessor.WURST.getHax().jesusHack.isOverLiquid())) {
                WalkPathProcessor.MC.field_1690.field_1832.method_23481(true);
            }
        }
        if (pos.method_10263() != nextPos.method_10263() || pos.method_10260() != nextPos.method_10260()) {
            WalkPathProcessor.MC.field_1690.field_1894.method_23481(true);
            if (this.index > 0 && ((PathPos)((Object)this.path.get(this.index - 1))).isJumping() || pos.method_10264() < nextPos.method_10264()) {
                WalkPathProcessor.MC.field_1690.field_1903.method_23481(true);
            }
        } else if (pos.method_10264() != nextPos.method_10264()) {
            if (pos.method_10264() < nextPos.method_10264()) {
                class_2248 block = BlockUtils.getBlock(pos);
                if (block instanceof class_2399 || block instanceof class_2541) {
                    WURST.getRotationFaker().faceVectorClientIgnorePitch(BlockUtils.getBoundingBox(pos).method_1005());
                    WalkPathProcessor.MC.field_1690.field_1894.method_23481(true);
                } else {
                    if (this.index < this.path.size() - 1 && !nextPos.method_10084().equals(this.path.get(this.index + 1))) {
                        ++this.index;
                    }
                    WalkPathProcessor.MC.field_1690.field_1903.method_23481(true);
                }
            } else {
                while (this.index < this.path.size() - 1 && ((PathPos)((Object)this.path.get(this.index))).method_10074().equals(this.path.get(this.index + 1))) {
                    ++this.index;
                }
                if (WurstClient.MC.field_1724.method_24828()) {
                    WalkPathProcessor.MC.field_1690.field_1894.method_23481(true);
                }
            }
        }
    }

    @Override
    public boolean canBreakBlocks() {
        return WalkPathProcessor.MC.field_1724.method_24828();
    }
}
