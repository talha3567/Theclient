package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.PlaceBlockEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class NoGhostBlocks
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> breaking;
    public final Setting<Boolean> placing;

    public NoGhostBlocks() {
        super(Categories.World, "no-ghost-blocks", "Attempts to prevent ghost blocks arising.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.breaking = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("breaking")).description("Whether to apply for block breaking actions.")).defaultValue(true)).build());
        this.placing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("placing")).description("Whether to apply for block placement actions.")).defaultValue(true)).build());
    }

    @EventHandler
    private void onBreakBlock(BreakBlockEvent event) {
        if (this.mc.isLocalServer() || !this.breaking.get().booleanValue()) {
            return;
        }
        event.cancel();
        BlockState blockState = this.mc.level.getBlockState(event.blockPos);
        blockState.getBlock().playerWillDestroy((Level)this.mc.level, event.blockPos, blockState, (Player)this.mc.player);
    }

    @EventHandler
    private void onPlaceBlock(PlaceBlockEvent event) {
        if (!this.placing.get().booleanValue()) {
            return;
        }
        event.cancel();
    }
}
