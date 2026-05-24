package net.wurstclient.ai;

import java.util.ArrayList;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.wurstclient.ai.PathPos;
import net.wurstclient.ai.PathProcessor;
import net.wurstclient.util.RotationUtils;

public class FlyPathProcessor
extends PathProcessor {
    private final boolean creativeFlying;

    public FlyPathProcessor(ArrayList<PathPos> path, boolean creativeFlying) {
        super(path);
        this.creativeFlying = creativeFlying;
    }

    @Override
    public void process() {
        boolean horizontal;
        class_2338 pos = class_2338.method_49638((class_2374)FlyPathProcessor.MC.field_1724.method_73189());
        class_243 posVec = FlyPathProcessor.MC.field_1724.method_73189();
        class_2338 nextPos = (class_2338)this.path.get(this.index);
        int posIndex = this.path.indexOf(pos);
        class_238 nextBox = new class_238((double)nextPos.method_10263() + 0.3, (double)nextPos.method_10264(), (double)nextPos.method_10260() + 0.3, (double)nextPos.method_10263() + 0.7, (double)nextPos.method_10264() + 0.2, (double)nextPos.method_10260() + 0.7);
        this.ticksOffPath = posIndex == -1 ? ++this.ticksOffPath : 0;
        if (posIndex > this.index || posVec.field_1352 >= nextBox.field_1323 && posVec.field_1352 <= nextBox.field_1320 && posVec.field_1351 >= nextBox.field_1322 && posVec.field_1351 <= nextBox.field_1325 && posVec.field_1350 >= nextBox.field_1321 && posVec.field_1350 <= nextBox.field_1324) {
            this.index = posIndex > this.index ? posIndex + 1 : ++this.index;
            if (this.creativeFlying) {
                class_243 v = FlyPathProcessor.MC.field_1724.method_18798();
                FlyPathProcessor.MC.field_1724.method_18800(v.field_1352 / Math.max(Math.abs(v.field_1352) * 50.0, 1.0), v.field_1351 / Math.max(Math.abs(v.field_1351) * 50.0, 1.0), v.field_1350 / Math.max(Math.abs(v.field_1350) * 50.0, 1.0));
            }
            if (this.index >= this.path.size()) {
                this.done = true;
            }
            return;
        }
        FlyPathProcessor.lockControls();
        FlyPathProcessor.MC.field_1724.method_31549().field_7479 = this.creativeFlying;
        boolean x = posVec.field_1352 < nextBox.field_1323 || posVec.field_1352 > nextBox.field_1320;
        boolean y = posVec.field_1351 < nextBox.field_1322 || posVec.field_1351 > nextBox.field_1325;
        boolean z = posVec.field_1350 < nextBox.field_1321 || posVec.field_1350 > nextBox.field_1324;
        boolean bl = horizontal = x || z;
        if (horizontal) {
            this.facePosition(nextPos);
            if (Math.abs(class_3532.method_15393((float)RotationUtils.getHorizontalAngleToLookVec(class_243.method_24953((class_2382)nextPos)))) > 1.0f) {
                return;
            }
        }
        class_2338 offset = nextPos.method_10059((class_2382)pos);
        while (this.index < this.path.size() - 1 && ((PathPos)((Object)this.path.get(this.index))).method_10081((class_2382)offset).equals(this.path.get(this.index + 1))) {
            ++this.index;
        }
        if (this.creativeFlying) {
            class_243 v = FlyPathProcessor.MC.field_1724.method_18798();
            if (!x) {
                FlyPathProcessor.MC.field_1724.method_18800(v.field_1352 / Math.max(Math.abs(v.field_1352) * 50.0, 1.0), v.field_1351, v.field_1350);
            }
            if (!y) {
                FlyPathProcessor.MC.field_1724.method_18800(v.field_1352, v.field_1351 / Math.max(Math.abs(v.field_1351) * 50.0, 1.0), v.field_1350);
            }
            if (!z) {
                FlyPathProcessor.MC.field_1724.method_18800(v.field_1352, v.field_1351, v.field_1350 / Math.max(Math.abs(v.field_1350) * 50.0, 1.0));
            }
        }
        class_243 vecInPos = new class_243((double)nextPos.method_10263() + 0.5, (double)nextPos.method_10264() + 0.1, (double)nextPos.method_10260() + 0.5);
        if (horizontal) {
            if (!this.creativeFlying && FlyPathProcessor.MC.field_1724.method_73189().method_1022(vecInPos) <= FlyPathProcessor.WURST.getHax().flightHack.getHorizontalSpeed()) {
                FlyPathProcessor.MC.field_1724.method_5814(vecInPos.field_1352, vecInPos.field_1351, vecInPos.field_1350);
                return;
            }
            FlyPathProcessor.MC.field_1690.field_1894.method_23481(true);
            if (FlyPathProcessor.MC.field_1724.field_5976) {
                if (posVec.field_1351 > nextBox.field_1325) {
                    FlyPathProcessor.MC.field_1690.field_1832.method_23481(true);
                } else if (posVec.field_1351 < nextBox.field_1322) {
                    FlyPathProcessor.MC.field_1690.field_1903.method_23481(true);
                }
            }
        } else if (y) {
            if (!this.creativeFlying && FlyPathProcessor.MC.field_1724.method_73189().method_1022(vecInPos) <= FlyPathProcessor.WURST.getHax().flightHack.getActualVerticalSpeed()) {
                FlyPathProcessor.MC.field_1724.method_5814(vecInPos.field_1352, vecInPos.field_1351, vecInPos.field_1350);
                return;
            }
            if (posVec.field_1351 < nextBox.field_1322) {
                FlyPathProcessor.MC.field_1690.field_1903.method_23481(true);
            } else {
                FlyPathProcessor.MC.field_1690.field_1832.method_23481(true);
            }
            if (FlyPathProcessor.MC.field_1724.field_5992) {
                FlyPathProcessor.MC.field_1690.field_1832.method_23481(false);
                FlyPathProcessor.MC.field_1690.field_1894.method_23481(true);
            }
        }
    }

    @Override
    public boolean canBreakBlocks() {
        return true;
    }
}
