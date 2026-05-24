package meteordevelopment.meteorclient.systems.modules.world;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StorageBlockListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.text.RunnableClickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;

public class StashFinder
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private static final List<Block> DEFAULT_SUPPORT_BLOCK_BLACKLIST = List.of(Blocks.OXIDIZED_COPPER, Blocks.OXIDIZED_CUT_COPPER, Blocks.TUFF_BRICKS, Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER, Blocks.BARREL, Blocks.WAXED_COPPER_BULB);
    private final Setting<List<BlockEntityType<?>>> storageBlocks;
    private final Setting<Integer> minimumStorageCount;
    private final Setting<List<Block>> blacklistedBlocks;
    private final Setting<Integer> minimumDistance;
    private final Setting<Boolean> sendNotifications;
    private final Setting<Mode> notificationMode;
    private final Setting<Boolean> renderTracer;
    private final Setting<SettingColor> traceColor;
    private final Setting<Integer> traceArrivalDistance;
    private final Setting<Integer> traceMaxDistance;
    private final Setting<Boolean> renderChunkColumn;
    private final Setting<SettingColor> traceColumnColor;
    private final Setting<Keybind> clearTracesBind;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<ChunkPos, Vec3> tracerPositions;
    public List<Chunk> chunks;

    public StashFinder() {
        super(Categories.World, "stash-finder", "Searches loaded chunks for storage blocks. Saves to <your minecraft folder>/meteor-client");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.storageBlocks = this.sgGeneral.add(((StorageBlockListSetting.Builder)((StorageBlockListSetting.Builder)new StorageBlockListSetting.Builder().name("storage-blocks")).description("Select the storage blocks to search for.")).defaultValue(StorageBlockListSetting.STORAGE_BLOCKS).build());
        this.minimumStorageCount = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("minimum-storage-count")).description("The minimum amount of storage blocks in a chunk to record the chunk.")).defaultValue(4)).min(1).sliderMin(1).build());
        this.blacklistedBlocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blacklisted-support-blocks")).description("Blocks that prevent counting a storage block entity when it sits on them.")).defaultValue(DEFAULT_SUPPORT_BLOCK_BLACKLIST)).build());
        this.minimumDistance = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("minimum-distance")).description("The minimum distance you must be from spawn to record a certain chunk.")).defaultValue(0)).min(0).sliderMax(10000).build());
        this.sendNotifications = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("notifications")).description("Sends Minecraft notifications when new stashes are found.")).defaultValue(true)).build());
        this.notificationMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("notification-mode")).description("The mode to use for notifications.")).defaultValue(Mode.Both)).visible(this.sendNotifications::get)).build());
        this.renderTracer = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-tracer")).description("Renders a tracer to the last found stash.")).defaultValue(true)).build());
        this.traceColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("tracer-color")).description("Color of the stash tracer.")).defaultValue(new SettingColor(255, 215, 0, 255)).visible(this.renderTracer::get)).build());
        this.traceArrivalDistance = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("tracer-hide-at-distance")).description("Hide the trace when you are this close to the stash.")).defaultValue(16)).min(1).sliderMin(1).sliderMax(50).visible(this.renderTracer::get)).build());
        this.traceMaxDistance = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("tracer-max-distance")).description("Hide the trace when you are farther than this distance from the stash.")).defaultValue(2000)).min(10).sliderMin(50).sliderMax(10000).visible(this.renderTracer::get)).build());
        this.renderChunkColumn = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-chunk-column")).description("Renders a vertical column at the center of traced chunks.")).defaultValue(false)).build());
        this.traceColumnColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("chunk-column-color")).description("Color of the stash tracer column.")).defaultValue(new SettingColor(255, 215, 0, 100)).visible(this.renderChunkColumn::get)).build());
        this.clearTracesBind = this.sgRender.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("clear-traces-bind")).description("Keybind to clear all stash traces.")).defaultValue(Keybind.none())).build());
        this.tracerPositions = new HashMap<ChunkPos, Vec3>();
        this.chunks = new ArrayList<Chunk>();
    }

    @Override
    public void onActivate() {
        this.load();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.clearTracesBind.get().isPressed()) {
            return;
        }
        this.tracerPositions.clear();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        double chunkZAbs;
        double chunkXAbs = Math.abs(event.chunk().getPos().x() * 16);
        if (Math.sqrt(chunkXAbs * chunkXAbs + (chunkZAbs = (double)Math.abs(event.chunk().getPos().z() * 16)) * chunkZAbs) < (double)this.minimumDistance.get().intValue()) {
            return;
        }
        Chunk chunk = new Chunk(event.chunk().getPos());
        List<Block> blockBlacklist = this.blacklistedBlocks.get();
        for (BlockEntity blockEntity : event.chunk().getBlockEntities().values()) {
            BlockPos below;
            if (!this.storageBlocks.get().contains(blockEntity.getType())) continue;
            if (!blockBlacklist.isEmpty()) {
                below = blockEntity.getBlockPos().below();
                if (blockBlacklist.contains(event.chunk().getBlockState(below).getBlock())) continue;
            }
            Objects.requireNonNull(blockEntity);
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ChestBlockEntity.class, BarrelBlockEntity.class, ShulkerBoxBlockEntity.class, EnderChestBlockEntity.class, AbstractFurnaceBlockEntity.class, DispenserBlockEntity.class, HopperBlockEntity.class}, (BlockEntity)below, n)) {
                case 0: {
                    ++chunk.chests;
                    break;
                }
                case 1: {
                    ++chunk.barrels;
                    break;
                }
                case 2: {
                    ++chunk.shulkers;
                    break;
                }
                case 3: {
                    ++chunk.enderChests;
                    break;
                }
                case 4: {
                    ++chunk.furnaces;
                    break;
                }
                case 5: {
                    ++chunk.dispensersDroppers;
                    break;
                }
                case 6: {
                    ++chunk.hoppers;
                    break;
                }
            }
        }
        if (chunk.getTotal() >= this.minimumStorageCount.get()) {
            Chunk prevChunk = null;
            int i = this.chunks.indexOf(chunk);
            if (i < 0) {
                this.chunks.add(chunk);
            } else {
                prevChunk = this.chunks.set(i, chunk);
            }
            if (this.renderTracer.get().booleanValue()) {
                double y = this.mc.player != null ? this.mc.player.getEyeY() : 0.0;
                this.tracerPositions.put(chunk.chunkPos, new Vec3((double)chunk.x, y, (double)chunk.z));
            }
            this.saveJson();
            this.saveCsv();
            if (!(!this.sendNotifications.get().booleanValue() || chunk.equals(prevChunk) && chunk.countsEqual(prevChunk))) {
                switch (this.notificationMode.get().ordinal()) {
                    case 0: {
                        this.sendChatNotification(chunk);
                        break;
                    }
                    case 1: {
                        MeteorToast toast = new MeteorToast.Builder(this.title).icon(Items.CHEST).text("Found Stash!").build();
                        this.mc.getToastManager().addToast((Toast)toast);
                        break;
                    }
                    case 2: {
                        this.sendChatNotification(chunk);
                        MeteorToast toast = new MeteorToast.Builder(this.title).icon(Items.CHEST).text("Found Stash!").build();
                        this.mc.getToastManager().addToast((Toast)toast);
                    }
                }
            }
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        this.chunks.sort(Comparator.comparingInt(value -> -value.getTotal()));
        WVerticalList list = theme.verticalList();
        WHorizontalList hl = theme.horizontalList();
        WButton clear = hl.add(theme.button("Clear Chunks")).widget();
        WButton resetTracers = hl.add(theme.button("Reset Tracers")).widget();
        list.add(hl);
        WTable table = new WTable();
        if (!this.chunks.isEmpty()) {
            list.add(table);
        }
        clear.action = () -> {
            this.chunks.clear();
            table.clear();
            this.tracerPositions.clear();
        };
        resetTracers.action = () -> {
            table.clear();
            this.tracerPositions.clear();
            this.fillTable(theme, table);
        };
        this.fillTable(theme, table);
        return list;
    }

    private void fillTable(GuiTheme theme, WTable table) {
        for (Chunk chunk : this.chunks) {
            table.add(theme.label("Pos: " + chunk.x + ", " + chunk.z)).padRight(10.0);
            table.add(theme.label("Total: " + chunk.getTotal())).padRight(10.0);
            WCheckbox visible = table.add(theme.checkbox(this.tracerPositions.containsKey(chunk.chunkPos))).widget();
            visible.action = () -> {
                if (visible.checked) {
                    double y = this.mc.player != null ? this.mc.player.getEyeY() : 0.0;
                    this.tracerPositions.put(chunk.chunkPos, new Vec3((double)chunk.x, y, (double)chunk.z));
                } else {
                    this.tracerPositions.remove(chunk.chunkPos);
                }
            };
            WButton open = table.add(theme.button("Open")).widget();
            open.action = () -> this.mc.setScreen((Screen)new ChunkScreen(theme, chunk));
            WButton gotoBtn = table.add(theme.button("Goto")).widget();
            gotoBtn.action = () -> PathManagers.get().moveTo(new BlockPos(chunk.x, 0, chunk.z), true);
            WMinus delete = table.add(theme.minus()).widget();
            delete.action = () -> {
                if (this.chunks.remove(chunk)) {
                    this.tracerPositions.remove(chunk.chunkPos);
                    table.clear();
                    this.fillTable(theme, table);
                    this.saveJson();
                    this.saveCsv();
                }
            };
            table.row();
        }
    }

    private void load() {
        block9: {
            Reader reader2;
            File file;
            boolean loaded;
            block8: {
                loaded = false;
                file = this.getJsonFile();
                if (file.exists()) {
                    try {
                        reader2 = new FileReader(file);
                        this.chunks = (List)GSON.fromJson(reader2, new TypeToken<List<Chunk>>(this){
                            final /* synthetic */ StashFinder this$0;
                            {
                                StashFinder stashFinder = this$0;
                                Objects.requireNonNull(stashFinder);
                                this.this$0 = stashFinder;
                            }
                        }.getType());
                        ((InputStreamReader)reader2).close();
                        for (Chunk chunk : this.chunks) {
                            chunk.calculatePos();
                        }
                        loaded = true;
                    }
                    catch (Exception reader2) {
                        if (this.chunks != null) break block8;
                        this.chunks = new ArrayList<Chunk>();
                    }
                }
            }
            file = this.getCsvFile();
            if (!loaded && file.exists()) {
                try {
                    String line;
                    reader2 = new BufferedReader(new FileReader(file));
                    ((BufferedReader)reader2).readLine();
                    while ((line = ((BufferedReader)reader2).readLine()) != null) {
                        String[] values = line.split(" ");
                        Chunk chunk = new Chunk(new ChunkPos(Integer.parseInt(values[0]), Integer.parseInt(values[1])));
                        chunk.chests = Integer.parseInt(values[2]);
                        chunk.shulkers = Integer.parseInt(values[3]);
                        chunk.enderChests = Integer.parseInt(values[4]);
                        chunk.furnaces = Integer.parseInt(values[5]);
                        chunk.dispensersDroppers = Integer.parseInt(values[6]);
                        chunk.hoppers = Integer.parseInt(values[7]);
                        this.chunks.add(chunk);
                    }
                    ((BufferedReader)reader2).close();
                }
                catch (Exception exception) {
                    if (this.chunks != null) break block9;
                    this.chunks = new ArrayList<Chunk>();
                }
            }
        }
    }

    private void saveCsv() {
        try {
            File file = this.getCsvFile();
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write("X,Z,Chests,Barrels,Shulkers,EnderChests,Furnaces,DispensersDroppers,Hoppers\n");
            for (Chunk chunk : this.chunks) {
                chunk.write(writer);
            }
            ((Writer)writer).close();
        }
        catch (IOException e) {
            MeteorClient.LOG.error("Error while writing the stash list to csv", e);
        }
    }

    private void saveJson() {
        try {
            File file = this.getJsonFile();
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            GSON.toJson(this.chunks, (Appendable)writer);
            ((Writer)writer).close();
        }
        catch (IOException e) {
            MeteorClient.LOG.error("Error while writing the stash list to json", e);
        }
    }

    private File getJsonFile() {
        return new File(new File(new File(MeteorClient.FOLDER, "stashes"), Utils.getFileWorldName()), "stashes.json");
    }

    private File getCsvFile() {
        return new File(new File(new File(MeteorClient.FOLDER, "stashes"), Utils.getFileWorldName()), "stashes.csv");
    }

    @Override
    public String getInfoString() {
        return String.valueOf(this.chunks.size());
    }

    private void sendChatNotification(Chunk chunk) {
        MutableComponent coords = Component.literal((String)(chunk.x + ", " + chunk.z)).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).applyFormat(ChatFormatting.UNDERLINE).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)"Path to stash"))).withClickEvent((ClickEvent)new RunnableClickEvent(() -> PathManagers.get().moveTo(new BlockPos(chunk.x, 0, chunk.z), true))));
        MutableComponent message = Component.literal((String)"Found stash at ").withStyle(ChatFormatting.GRAY).append((Component)Component.literal((String)"[").withStyle(ChatFormatting.GRAY)).append((Component)coords).append((Component)Component.literal((String)"]").withStyle(ChatFormatting.GRAY)).append((Component)Component.literal((String)".").withStyle(ChatFormatting.GRAY));
        ChatUtils.sendMsg((Component)message);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.tracerPositions.isEmpty() || this.mc.player == null) {
            return;
        }
        double playerX = this.mc.player.getX();
        double playerZ = this.mc.player.getZ();
        this.tracerPositions.entrySet().removeIf(entry -> {
            Vec3 pos = (Vec3)entry.getValue();
            double horizontalDist = Math.hypot(pos.x - playerX, pos.z - playerZ);
            return horizontalDist <= (double)this.traceArrivalDistance.get().intValue();
        });
        if (!this.renderTracer.get().booleanValue() && !this.renderChunkColumn.get().booleanValue()) {
            return;
        }
        for (Vec3 pos : this.tracerPositions.values()) {
            double horizontalDist = Math.hypot(pos.x - playerX, pos.z - playerZ);
            if (horizontalDist > (double)this.traceMaxDistance.get().intValue()) continue;
            if (this.renderTracer.get().booleanValue()) {
                event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, pos.x, this.mc.player.getEyeY(), pos.z, this.traceColor.get());
            }
            if (!this.renderChunkColumn.get().booleanValue()) continue;
            double x1 = pos.x - 0.5;
            double x2 = pos.x + 0.5;
            double z1 = pos.z - 0.5;
            double z2 = pos.z + 0.5;
            int bottomY = this.mc.level.getMinY();
            int topY = bottomY + this.mc.level.dimensionType().height();
            event.renderer.line(x1, bottomY, z1, x1, topY, z1, this.traceColumnColor.get());
            event.renderer.line(x1, bottomY, z2, x1, topY, z2, this.traceColumnColor.get());
            event.renderer.line(x2, bottomY, z1, x2, topY, z1, this.traceColumnColor.get());
            event.renderer.line(x2, bottomY, z2, x2, topY, z2, this.traceColumnColor.get());
        }
    }

    public static enum Mode {
        Chat,
        Toast,
        Both;

    }

    public static class Chunk {
        private static final StringBuilder sb = new StringBuilder();
        public ChunkPos chunkPos;
        public transient int x;
        public transient int z;
        public int chests;
        public int barrels;
        public int shulkers;
        public int enderChests;
        public int furnaces;
        public int dispensersDroppers;
        public int hoppers;

        public Chunk(ChunkPos chunkPos) {
            this.chunkPos = chunkPos;
            this.calculatePos();
        }

        public void calculatePos() {
            this.x = this.chunkPos.x() * 16 + 8;
            this.z = this.chunkPos.z() * 16 + 8;
        }

        public int getTotal() {
            return this.chests + this.barrels + this.shulkers + this.enderChests + this.furnaces + this.dispensersDroppers + this.hoppers;
        }

        public void write(Writer writer) throws IOException {
            sb.setLength(0);
            sb.append(this.x).append(',').append(this.z).append(',');
            sb.append(this.chests).append(',').append(this.barrels).append(',').append(this.shulkers).append(',').append(this.enderChests).append(',').append(this.furnaces).append(',').append(this.dispensersDroppers).append(',').append(this.hoppers).append('\n');
            writer.write(sb.toString());
        }

        public boolean countsEqual(Chunk c) {
            if (c == null) {
                return false;
            }
            return this.chests != c.chests || this.barrels != c.barrels || this.shulkers != c.shulkers || this.enderChests != c.enderChests || this.furnaces != c.furnaces || this.dispensersDroppers != c.dispensersDroppers || this.hoppers != c.hoppers;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Chunk chunk = (Chunk)o;
            return Objects.equals(this.chunkPos, chunk.chunkPos);
        }

        public int hashCode() {
            return Objects.hash(this.chunkPos);
        }
    }

    private static class ChunkScreen
    extends WindowScreen {
        private final Chunk chunk;

        public ChunkScreen(GuiTheme theme, Chunk chunk) {
            super(theme, "Chunk at " + chunk.x + ", " + chunk.z);
            this.chunk = chunk;
        }

        @Override
        public void initWidgets() {
            WTable t = this.add(this.theme.table()).expandX().widget();
            t.add(this.theme.label("Total:"));
            t.add(this.theme.label("" + this.chunk.getTotal()));
            t.row();
            t.add(this.theme.horizontalSeparator()).expandX();
            t.row();
            t.add(this.theme.label("Chests:"));
            t.add(this.theme.label("" + this.chunk.chests));
            t.row();
            t.add(this.theme.label("Barrels:"));
            t.add(this.theme.label("" + this.chunk.barrels));
            t.row();
            t.add(this.theme.label("Shulkers:"));
            t.add(this.theme.label("" + this.chunk.shulkers));
            t.row();
            t.add(this.theme.label("Ender Chests:"));
            t.add(this.theme.label("" + this.chunk.enderChests));
            t.row();
            t.add(this.theme.label("Furnaces:"));
            t.add(this.theme.label("" + this.chunk.furnaces));
            t.row();
            t.add(this.theme.label("Dispensers and droppers:"));
            t.add(this.theme.label("" + this.chunk.dispensersDroppers));
            t.row();
            t.add(this.theme.label("Hoppers:"));
            t.add(this.theme.label("" + this.chunk.hoppers));
        }
    }
}
