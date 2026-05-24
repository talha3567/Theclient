package net.wurstclient.hacks;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_2382;
import net.minecraft.class_290;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.ChunkAreaSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BlockVertexCompiler;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.EasyVertexBuffer;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;
import net.wurstclient.util.chunk.ChunkSearcher;
import net.wurstclient.util.chunk.ChunkSearcherCoordinator;

@SearchTags(value={"cave finder"})
public final class CaveFinderHack
extends Hack
implements UpdateListener,
RenderListener {
    private final ChunkAreaSetting area = new ChunkAreaSetting("Area", "The area around the player to search in.\nHigher values require a faster computer.");
    private final SliderSetting limit = new SliderSetting("Limit", "The maximum number of blocks to display.\nHigher values require a faster computer.", 5.0, 3.0, 6.0, 1.0, SliderSetting.ValueDisplay.LOGARITHMIC);
    private final ColorSetting color = new ColorSetting("Color", "Caves will be highlighted in this color.", Color.RED);
    private final SliderSetting opacity = new SliderSetting("Opacity", "How opaque the highlights should be.\n0 = breathing animation", 0.0, 0.0, 1.0, 0.01, SliderSetting.ValueDisplay.PERCENTAGE.withLabel(0.0, "breathing"));
    private int prevLimit;
    private boolean notify;
    private final ChunkSearcherCoordinator coordinator = new ChunkSearcherCoordinator((pos, state) -> state.method_26204() == class_2246.field_10543, this.area);
    private ForkJoinPool forkJoinPool;
    private ForkJoinTask<HashSet<class_2338>> getMatchingBlocksTask;
    private ForkJoinTask<ArrayList<int[]>> compileVerticesTask;
    private EasyVertexBuffer vertexBuffer;
    private RegionPos bufferRegion;
    private boolean bufferUpToDate;

    public CaveFinderHack() {
        super("CaveFinder");
        this.setCategory(Category.RENDER);
        this.addSetting(this.area);
        this.addSetting(this.limit);
        this.addSetting(this.color);
        this.addSetting(this.opacity);
    }

    @Override
    protected void onEnable() {
        this.prevLimit = this.limit.getValueI();
        this.notify = true;
        this.forkJoinPool = new ForkJoinPool();
        this.bufferUpToDate = false;
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PacketInputListener.class, this.coordinator);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PacketInputListener.class, this.coordinator);
        EVENTS.remove(RenderListener.class, this);
        this.stopBuildingBuffer();
        this.coordinator.reset();
        this.forkJoinPool.shutdownNow();
        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
        }
        this.vertexBuffer = null;
        this.bufferRegion = null;
    }

    @Override
    public void onUpdate() {
        boolean searchersChanged = this.coordinator.update();
        if (searchersChanged) {
            this.stopBuildingBuffer();
        }
        if (!this.coordinator.isDone()) {
            return;
        }
        if (this.limit.getValueI() != this.prevLimit) {
            this.stopBuildingBuffer();
            this.prevLimit = this.limit.getValueI();
            this.notify = true;
        }
        if (this.getMatchingBlocksTask == null) {
            this.startGetMatchingBlocksTask();
        }
        if (!this.getMatchingBlocksTask.isDone()) {
            return;
        }
        if (this.compileVerticesTask == null) {
            this.startCompileVerticesTask();
        }
        if (!this.compileVerticesTask.isDone()) {
            return;
        }
        if (!this.bufferUpToDate) {
            this.setBufferFromTask();
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (this.vertexBuffer == null || this.bufferRegion == null) {
            return;
        }
        float x = (float)(System.currentTimeMillis() % 2000L) / 1000.0f;
        float alpha = 0.25f + 0.25f * class_3532.method_15374((double)(x * (float)Math.PI));
        if (this.opacity.getValue() > 0.0) {
            alpha = this.opacity.getValueF();
        }
        matrixStack.method_22903();
        RenderUtils.applyRegionalRenderOffset(matrixStack, this.bufferRegion);
        this.vertexBuffer.draw(matrixStack, WurstRenderLayers.ESP_QUADS, this.color.getColorF(), alpha);
        matrixStack.method_22909();
    }

    private void stopBuildingBuffer() {
        if (this.getMatchingBlocksTask != null) {
            this.getMatchingBlocksTask.cancel(true);
            this.getMatchingBlocksTask = null;
        }
        if (this.compileVerticesTask != null) {
            this.compileVerticesTask.cancel(true);
            this.compileVerticesTask = null;
        }
        this.bufferUpToDate = false;
    }

    private void startGetMatchingBlocksTask() {
        class_2338 eyesPos = class_2338.method_49638((class_2374)RotationUtils.getEyesPos());
        Comparator<class_2338> comparator = Comparator.comparingInt(pos -> eyesPos.method_19455((class_2382)pos));
        this.getMatchingBlocksTask = this.forkJoinPool.submit(() -> ((Stream)this.coordinator.getMatches().parallel()).map(ChunkSearcher.Result::pos).sorted(comparator).limit(this.limit.getValueLog()).collect(Collectors.toCollection(HashSet::new)));
    }

    private void startCompileVerticesTask() {
        HashSet<class_2338> matchingBlocks = this.getMatchingBlocksTask.join();
        if (matchingBlocks.size() < this.limit.getValueLog()) {
            this.notify = true;
        } else if (this.notify) {
            ChatUtils.warning("CaveFinder found \u00a7lA LOT\u00a7r of blocks! To prevent lag, it will only show the closest \u00a76" + this.limit.getValueString() + "\u00a7r results.");
            this.notify = false;
        }
        this.compileVerticesTask = this.forkJoinPool.submit(() -> BlockVertexCompiler.compile(matchingBlocks));
    }

    private void setBufferFromTask() {
        ArrayList<int[]> vertices = this.compileVerticesTask.join();
        RegionPos region = RenderUtils.getCameraRegion();
        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
        }
        this.vertexBuffer = EasyVertexBuffer.createAndUpload(VertexFormat.class_5596.field_27382, class_290.field_1576, buffer -> {
            for (int[] vertex : vertices) {
                buffer.method_22912((float)(vertex[0] - region.x()), (float)vertex[1], (float)(vertex[2] - region.z())).method_39415(-1);
            }
        });
        this.bufferUpToDate = true;
        this.bufferRegion = region;
    }
}
