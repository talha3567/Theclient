package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_243;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.nukers.CommonNukerSettings;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"speed nuker", "FastNuker", "fast nuker"})
@DontSaveState
public final class SpeedNukerHack
extends Hack
implements UpdateListener {
    private final SliderSetting range = new SliderSetting("Range", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CommonNukerSettings commonSettings = new CommonNukerSettings();
    private final SwingHandSetting swingHand = new SwingHandSetting(SwingHandSetting.genericMiningDescription(this), SwingHandSetting.SwingHand.OFF);

    public SpeedNukerHack() {
        super("SpeedNuker");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.range);
        this.commonSettings.getSettings().forEach(x$0 -> this.addSetting((Setting)x$0));
        this.addSetting(this.swingHand);
    }

    @Override
    public String getRenderName() {
        return this.getName() + this.commonSettings.getRenderNameSuffix();
    }

    @Override
    protected void onEnable() {
        SpeedNukerHack.WURST.getHax().autoMineHack.setEnabled(false);
        SpeedNukerHack.WURST.getHax().excavatorHack.setEnabled(false);
        SpeedNukerHack.WURST.getHax().nukerHack.setEnabled(false);
        SpeedNukerHack.WURST.getHax().nukerLegitHack.setEnabled(false);
        SpeedNukerHack.WURST.getHax().tunnellerHack.setEnabled(false);
        SpeedNukerHack.WURST.getHax().veinMinerHack.setEnabled(false);
        EVENTS.add(LeftClickListener.class, this.commonSettings);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(LeftClickListener.class, this.commonSettings);
        EVENTS.remove(UpdateListener.class, this);
        this.commonSettings.reset();
    }

    @Override
    public void onUpdate() {
        ArrayList blocks;
        if (this.commonSettings.isIdModeWithAir()) {
            return;
        }
        class_243 eyesVec = RotationUtils.getEyesPos();
        class_2338 eyesBlock = class_2338.method_49638((class_2374)eyesVec);
        double rangeSq = this.range.getValueSq();
        int blockRange = this.range.getValueCeil();
        Stream<class_2338> stream = BlockUtils.getAllInBoxStream(eyesBlock, blockRange).filter(BlockUtils::canBeClicked).filter(this.commonSettings::shouldBreakBlock);
        if (this.commonSettings.isSphereShape()) {
            stream = stream.filter(pos -> pos.method_19770((class_2374)eyesVec) <= rangeSq);
        }
        if ((blocks = stream.sorted(Comparator.comparingDouble(pos -> pos.method_19770((class_2374)eyesVec))).collect(Collectors.toCollection(ArrayList::new))).isEmpty()) {
            return;
        }
        SpeedNukerHack.WURST.getHax().autoToolHack.equipIfEnabled((class_2338)blocks.get(0));
        BlockBreaker.breakBlocksWithPacketSpam(blocks);
        this.swingHand.swing(class_1268.field_5808);
    }
}
