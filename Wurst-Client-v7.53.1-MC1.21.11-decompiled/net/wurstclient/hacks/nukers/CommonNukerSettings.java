package net.wurstclient.hacks.nukers;

import java.util.stream.Stream;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.wurstclient.WurstClient;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.hacks.nukers.NukerModeSetting;
import net.wurstclient.hacks.nukers.NukerMultiIdListSetting;
import net.wurstclient.hacks.nukers.NukerShapeSetting;
import net.wurstclient.settings.BlockSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.BlockUtils;

public final class CommonNukerSettings
implements LeftClickListener {
    private static final class_310 MC = WurstClient.MC;
    private final NukerShapeSetting shape = new NukerShapeSetting();
    private final CheckboxSetting flat = new CheckboxSetting("Flat mode", "Won't break any blocks below your feet.", false);
    private final NukerModeSetting mode = new NukerModeSetting();
    private final BlockSetting id = new BlockSetting("ID", "The type of block to break in ID mode.\nair = won't break anything", "minecraft:air", true);
    private final CheckboxSetting lockId = new CheckboxSetting("Lock ID", "Prevents changing the ID by clicking on blocks or restarting the hack.", false);
    private final NukerMultiIdListSetting multiIdList = new NukerMultiIdListSetting();

    public Stream<Setting> getSettings() {
        return Stream.of(this.shape, this.flat, this.mode, this.id, this.lockId, this.multiIdList);
    }

    public void reset() {
        if (!this.lockId.isChecked()) {
            this.id.setBlock(class_2246.field_10124);
        }
    }

    public String getRenderNameSuffix() {
        return switch ((NukerModeSetting.NukerMode)((Object)this.mode.getSelected())) {
            case NukerModeSetting.NukerMode.ID -> " [ID:" + this.id.getShortBlockName() + "]";
            case NukerModeSetting.NukerMode.MULTI_ID -> " [MultiID:" + this.multiIdList.size() + "]";
            case NukerModeSetting.NukerMode.SMASH -> " [Smash]";
            default -> "";
        };
    }

    public boolean isIdModeWithAir() {
        return this.mode.getSelected() == NukerModeSetting.NukerMode.ID && this.id.getBlock() == class_2246.field_10124;
    }

    public boolean isSphereShape() {
        return this.shape.getSelected() == NukerShapeSetting.NukerShape.SPHERE;
    }

    public boolean shouldBreakBlock(class_2338 pos) {
        if (this.flat.isChecked() && (double)pos.method_10264() < CommonNukerSettings.MC.field_1724.method_23318()) {
            return false;
        }
        switch ((NukerModeSetting.NukerMode)((Object)this.mode.getSelected())) {
            default: {
                return true;
            }
            case ID: {
                return BlockUtils.getName(pos).equals(this.id.getBlockName());
            }
            case MULTI_ID: {
                return this.multiIdList.contains(BlockUtils.getBlock(pos));
            }
            case SMASH: 
        }
        return BlockUtils.getHardness(pos) >= 1.0f;
    }

    @Override
    public void onLeftClick(LeftClickListener.LeftClickEvent event) {
        class_3965 bHitResult;
        if (this.lockId.isChecked() || this.mode.getSelected() != NukerModeSetting.NukerMode.ID) {
            return;
        }
        class_239 class_2392 = CommonNukerSettings.MC.field_1765;
        if (!(class_2392 instanceof class_3965) || (bHitResult = (class_3965)class_2392).method_17783() != class_239.class_240.field_1332) {
            return;
        }
        this.id.setBlockName(BlockUtils.getName(bHitResult.method_17777()));
    }
}
