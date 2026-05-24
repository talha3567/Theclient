package net.wurstclient.hacks;

import java.util.Random;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.BlockBreakingProgressListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BlockUtils;

@SearchTags(value={"FastMine", "SpeedMine", "SpeedyGonzales", "fast break", "fast mine", "speed mine", "speedy gonzales", "NoBreakDelay", "no break delay"})
public final class FastBreakHack
extends Hack
implements UpdateListener,
BlockBreakingProgressListener {
    private final SliderSetting activationChance = new SliderSetting("Activation chance", "Only FastBreaks some of the blocks you break with the given chance, which makes it harder for anti-cheat plugins to detect.\n\nThis setting does nothing if Legit mode is enabled.", 1.0, 0.0, 1.0, 0.01, SliderSetting.ValueDisplay.PERCENTAGE);
    private final CheckboxSetting legitMode = new CheckboxSetting("Legit mode", "Only removes the delay between breaking blocks, without speeding up the breaking process itself.\n\nThis is much slower, but great at bypassing anti-cheat plugins. Use this if regular FastBreak is not working and the Activation chance slider doesn't help.", false);
    private final Random random = new Random();
    private class_2338 lastBlockPos;
    private boolean fastBreakBlock;

    public FastBreakHack() {
        super("FastBreak");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.activationChance);
        this.addSetting(this.legitMode);
    }

    @Override
    public String getRenderName() {
        if (this.legitMode.isChecked()) {
            return this.getName() + "Legit";
        }
        return this.getName();
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(BlockBreakingProgressListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(BlockBreakingProgressListener.class, this);
        this.lastBlockPos = null;
    }

    @Override
    public void onUpdate() {
        FastBreakHack.MC.field_1761.field_3716 = 0;
    }

    @Override
    public void onBlockBreakingProgress(BlockBreakingProgressListener.BlockBreakingProgressEvent event) {
        if (this.legitMode.isChecked()) {
            return;
        }
        if (FastBreakHack.MC.field_1761.field_3715 >= 1.0f) {
            return;
        }
        class_2338 blockPos = event.getBlockPos();
        if (!blockPos.equals((Object)this.lastBlockPos)) {
            this.lastBlockPos = blockPos;
            boolean bl = this.fastBreakBlock = this.random.nextDouble() <= this.activationChance.getValue();
        }
        if (BlockUtils.isUnbreakable(blockPos)) {
            return;
        }
        if (!this.fastBreakBlock) {
            return;
        }
        class_2846.class_2847 action = class_2846.class_2847.field_12973;
        class_2350 direction = event.getDirection();
        IMC.getInteractionManager().sendPlayerActionC2SPacket(action, blockPos, direction);
    }
}
