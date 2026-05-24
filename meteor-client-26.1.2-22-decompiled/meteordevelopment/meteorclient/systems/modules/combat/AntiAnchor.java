package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;

public class AntiAnchor
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> swing;

    public AntiAnchor() {
        super(Categories.Combat, "anti-anchor", "Automatically prevents Anchor Aura by placing a slab on your head.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Makes you rotate when placing.")).defaultValue(true)).build());
        this.swing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Swings your hand when placing.")).defaultValue(true)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.level.getBlockState(this.mc.player.blockPosition().above(2)).getBlock() == Blocks.RESPAWN_ANCHOR && this.mc.level.getBlockState(this.mc.player.blockPosition().above()).getBlock() == Blocks.AIR) {
            BlockUtils.place(this.mc.player.blockPosition().offset(0, 1, 0), InvUtils.findInHotbar(itemStack -> Block.byItem((Item)itemStack.getItem()) instanceof SlabBlock), this.rotate.get(), 15, this.swing.get(), false, true);
        }
    }
}
