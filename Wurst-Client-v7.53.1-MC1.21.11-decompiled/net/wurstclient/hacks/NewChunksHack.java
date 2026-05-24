package net.wurstclient.hacks;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2791;
import net.minecraft.class_2818;
import net.minecraft.class_2874;
import net.minecraft.class_3610;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.wurstclient.Category;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.newchunks.NewChunksChunkRenderer;
import net.wurstclient.hacks.newchunks.NewChunksReasonsRenderer;
import net.wurstclient.hacks.newchunks.NewChunksRenderer;
import net.wurstclient.hacks.newchunks.NewChunksShowSetting;
import net.wurstclient.hacks.newchunks.NewChunksStyleSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.chunk.ChunkUtils;

public final class NewChunksHack
extends Hack
implements UpdateListener,
RenderListener {
    private final NewChunksStyleSetting style = new NewChunksStyleSetting();
    private final NewChunksShowSetting show = new NewChunksShowSetting();
    private final CheckboxSetting showReasons = new CheckboxSetting("Show reasons", "Highlights the block that caused each chunk to be marked as new/old.", false);
    private final CheckboxSetting showCounter = new CheckboxSetting("Show counter", "Shows the number of new/old chunks found so far.", false);
    private final SliderSetting altitude = new SliderSetting("Altitude", 0.0, -64.0, 320.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final SliderSetting drawDistance = new SliderSetting("Draw distance", 32.0, 8.0, 64.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final SliderSetting opacity = new SliderSetting("Opacity", 0.75, 0.1, 1.0, 0.01, SliderSetting.ValueDisplay.PERCENTAGE);
    private final ColorSetting newChunksColor = new ColorSetting("New chunks color", Color.RED);
    private final ColorSetting oldChunksColor = new ColorSetting("Old chunks color", Color.BLUE);
    private final CheckboxSetting logChunks = new CheckboxSetting("Log chunks", "Writes to the log file when a new/old chunk is found.", false);
    private final Set<class_1923> newChunks = ConcurrentHashMap.newKeySet();
    private final Set<class_1923> oldChunks = ConcurrentHashMap.newKeySet();
    private final Set<class_1923> dontCheckAgain = ConcurrentHashMap.newKeySet();
    private final Set<class_2338> newChunkReasons = ConcurrentHashMap.newKeySet();
    private final Set<class_2338> oldChunkReasons = ConcurrentHashMap.newKeySet();
    private final NewChunksRenderer renderer = new NewChunksRenderer(this.altitude, this.opacity, this.newChunksColor, this.oldChunksColor);
    private final NewChunksReasonsRenderer reasonsRenderer = new NewChunksReasonsRenderer(this.drawDistance);
    private RegionPos lastRegion;
    private class_2874 lastDimension;

    public NewChunksHack() {
        super("NewChunks");
        this.setCategory(Category.RENDER);
        this.addSetting(this.style);
        this.addSetting(this.show);
        this.addSetting(this.showReasons);
        this.addSetting(this.showCounter);
        this.addSetting(this.altitude);
        this.addSetting(this.drawDistance);
        this.addSetting(this.opacity);
        this.addSetting(this.newChunksColor);
        this.addSetting(this.oldChunksColor);
        this.addSetting(this.logChunks);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
        this.reset();
    }

    private void reset() {
        this.oldChunks.clear();
        this.newChunks.clear();
        this.dontCheckAgain.clear();
        this.oldChunkReasons.clear();
        this.newChunkReasons.clear();
        this.lastRegion = null;
        this.lastDimension = NewChunksHack.MC.field_1687.method_8597();
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.renderer.closeBuffers();
    }

    @Override
    public String getRenderName() {
        if (!this.showCounter.isChecked()) {
            return this.getName();
        }
        return String.format("%s [%d/%d]", this.getName(), this.newChunks.size(), this.oldChunks.size());
    }

    @Override
    public void onUpdate() {
        this.renderer.closeBuffers();
        NewChunksShowSetting.Show showSetting = (NewChunksShowSetting.Show)((Object)this.show.getSelected());
        int dd = this.drawDistance.getValueI();
        NewChunksChunkRenderer chunkRenderer = ((NewChunksStyleSetting.Style)((Object)this.style.getSelected())).getChunkRenderer();
        if (showSetting.includesNew()) {
            this.renderer.updateBuffer(0, chunkRenderer.getLayer(), buffer -> chunkRenderer.buildBuffer((class_4588)buffer, this.newChunks, dd));
            if (this.showReasons.isChecked()) {
                this.renderer.updateBuffer(1, this.reasonsRenderer.getLayer(), buffer -> this.reasonsRenderer.buildBuffer((class_4588)buffer, List.copyOf(this.newChunkReasons)));
            }
        }
        if (showSetting.includesOld()) {
            this.renderer.updateBuffer(2, chunkRenderer.getLayer(), buffer -> chunkRenderer.buildBuffer((class_4588)buffer, this.oldChunks, dd));
            if (this.showReasons.isChecked()) {
                this.renderer.updateBuffer(3, this.reasonsRenderer.getLayer(), buffer -> this.reasonsRenderer.buildBuffer((class_4588)buffer, List.copyOf(this.oldChunkReasons)));
            }
        }
    }

    public void afterLoadChunk(int x, int z) {
        if (!this.isEnabled()) {
            return;
        }
        class_2818 chunk = NewChunksHack.MC.field_1687.method_8497(x, z);
        new Thread(() -> this.checkLoadedChunk(chunk), "NewChunks " + String.valueOf(chunk.method_12004())).start();
    }

    private void checkLoadedChunk(class_2818 chunk) {
        class_1923 chunkPos = chunk.method_12004();
        if (this.newChunks.contains(chunkPos) || this.oldChunks.contains(chunkPos) || this.dontCheckAgain.contains(chunkPos)) {
            return;
        }
        int minX = chunkPos.method_8326();
        int minY = chunk.method_31607();
        int minZ = chunkPos.method_8328();
        int maxX = chunkPos.method_8327();
        int maxY = ChunkUtils.getHighestNonEmptySectionYOffset((class_2791)chunk) + 16;
        int maxZ = chunkPos.method_8329();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    class_2338 pos = new class_2338(x, y, z);
                    class_3610 fluidState = chunk.method_8316(pos);
                    if (fluidState.method_15769() || fluidState.method_15771()) continue;
                    this.oldChunks.add(chunkPos);
                    this.oldChunkReasons.add(pos);
                    if (this.logChunks.isChecked()) {
                        System.out.println("old chunk at " + String.valueOf(chunkPos));
                    }
                    return;
                }
            }
        }
        this.dontCheckAgain.add(chunkPos);
    }

    public void afterUpdateBlock(class_2338 pos) {
        if (!this.isEnabled()) {
            return;
        }
        class_3610 fluidState = BlockUtils.getState(pos).method_26227();
        if (fluidState.method_15769() || fluidState.method_15771()) {
            return;
        }
        class_1923 chunkPos = new class_1923(pos);
        if (this.newChunks.contains(chunkPos) || this.oldChunks.contains(chunkPos)) {
            return;
        }
        this.newChunks.add(chunkPos);
        this.newChunkReasons.add(pos);
        if (this.logChunks.isChecked()) {
            System.out.println("new chunk at " + String.valueOf(chunkPos));
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        RegionPos region;
        if (NewChunksHack.MC.field_1687.method_8597() != this.lastDimension) {
            this.reset();
        }
        if (!(region = RenderUtils.getCameraRegion()).equals(this.lastRegion)) {
            this.onUpdate();
            this.lastRegion = region;
        }
        this.renderer.render(matrixStack, partialTicks);
    }
}
