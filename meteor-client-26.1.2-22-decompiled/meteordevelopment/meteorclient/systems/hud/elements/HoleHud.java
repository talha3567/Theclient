package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.LevelRendererAccessor;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.DisplayItemUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class HoleHud
extends HudElement {
    public static final HudElementInfo<HoleHud> INFO = new HudElementInfo<HoleHud>(Hud.GROUP, "hole", "Displays information about the hole you are standing in.", HoleHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    public final Setting<List<Block>> safe;
    public final Setting<Boolean> customScale;
    public final Setting<Double> scale;
    public final Setting<Boolean> background;
    public final Setting<SettingColor> backgroundColor;
    private final Color BG_COLOR;
    private final Color OL_COLOR;

    public HoleHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.safe = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("safe-blocks")).description("Which blocks to consider safe.")).defaultValue(Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies a custom scale to this hud element.")).defaultValue(false)).onChanged(bl -> this.calculateSize())).build());
        this.scale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(2.0).onChanged(d -> this.calculateSize())).min(0.5).sliderRange(0.5, 3.0).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.BG_COLOR = new Color(255, 25, 25, 100);
        this.OL_COLOR = new Color(255, 25, 25, 255);
        this.calculateSize();
    }

    private void calculateSize() {
        this.setSize(48.0f * this.getScale(), 48.0f * this.getScale());
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            this.drawBlock(renderer, this.get(Facing.Left), this.x, (float)this.y + 16.0f * this.getScale());
            this.drawBlock(renderer, this.get(Facing.Front), (float)this.x + 16.0f * this.getScale(), this.y);
            this.drawBlock(renderer, this.get(Facing.Right), (float)this.x + 32.0f * this.getScale(), (float)this.y + 16.0f * this.getScale());
            this.drawBlock(renderer, this.get(Facing.Back), (float)this.x + 16.0f * this.getScale(), (float)this.y + 32.0f * this.getScale());
        });
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
    }

    private Direction get(Facing dir) {
        if (this.isInEditor()) {
            return Direction.DOWN;
        }
        return Direction.fromYRot((double)Mth.wrapDegrees((float)(MeteorClient.mc.player.getYRot() + (float)dir.offset)));
    }

    private void drawBlock(HudRenderer renderer, Direction dir, double x, double y) {
        Block block;
        Block block2 = block = dir == Direction.DOWN ? Blocks.OBSIDIAN : MeteorClient.mc.level.getBlockState(MeteorClient.mc.player.blockPosition().relative(dir)).getBlock();
        if (!this.safe.get().contains(block)) {
            return;
        }
        renderer.item(DisplayItemUtils.toStack(block.asItem()), (int)x, (int)y, this.getScale(), false);
        if (dir == Direction.DOWN) {
            return;
        }
        ((LevelRendererAccessor)MeteorClient.mc.levelRenderer).meteor$getDestroyingBlocks().values().forEach(info -> {
            if (info.getPos().equals((Object)MeteorClient.mc.player.blockPosition().relative(dir))) {
                this.renderBreaking(renderer, x, y, (float)info.getProgress() / 9.0f);
            }
        });
    }

    private void renderBreaking(HudRenderer renderer, double x, double y, double percent) {
        renderer.quad(x, y, 16.0 * percent * (double)this.getScale(), 16.0f * this.getScale(), this.BG_COLOR);
        renderer.quad(x, y, 16.0f * this.getScale(), 1.0f * this.getScale(), this.OL_COLOR);
        renderer.quad(x, y + (double)(15.0f * this.getScale()), 16.0f * this.getScale(), 1.0f * this.getScale(), this.OL_COLOR);
        renderer.quad(x, y, 1.0f * this.getScale(), 16.0f * this.getScale(), this.OL_COLOR);
        renderer.quad(x + (double)(15.0f * this.getScale()), y, 1.0f * this.getScale(), 16.0f * this.getScale(), this.OL_COLOR);
    }

    private float getScale() {
        return this.customScale.get() != false ? this.scale.get().floatValue() : this.scale.getDefaultValue().floatValue();
    }

    private static enum Facing {
        Left(-90),
        Right(90),
        Front(0),
        Back(180);

        public final int offset;

        private Facing(int offset) {
            this.offset = offset;
        }
    }
}
