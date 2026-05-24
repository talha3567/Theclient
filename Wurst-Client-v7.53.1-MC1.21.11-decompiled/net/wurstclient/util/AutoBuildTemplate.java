package net.wurstclient.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import net.minecraft.class_1792;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.wurstclient.settings.FileSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonArray;
import net.wurstclient.util.json.WsonObject;

public final class AutoBuildTemplate {
    private final Path path;
    private final String name;
    private final LinkedHashSet<BlockData> blocks;

    private AutoBuildTemplate(Path path, LinkedHashSet<BlockData> blocks) {
        this.path = path;
        String fileName = path.getFileName().toString();
        this.name = fileName.substring(0, fileName.lastIndexOf("."));
        this.blocks = blocks;
    }

    public static AutoBuildTemplate load(Path path) throws IOException, JsonException {
        WsonObject json = JsonUtils.parseFileToObject(path);
        int version = json.getInt("version", 1);
        WsonArray jsonBlocks = json.getArray("blocks");
        LinkedHashSet<BlockData> loadedBlocks = new LinkedHashSet<BlockData>();
        if (jsonBlocks.isEmpty()) {
            throw new JsonException("Template has no blocks!");
        }
        switch (version) {
            case 1: {
                AutoBuildTemplate.loadV1(jsonBlocks, loadedBlocks);
                break;
            }
            case 2: {
                AutoBuildTemplate.loadV2(jsonBlocks, loadedBlocks);
                break;
            }
            default: {
                throw new JsonException("Unknown template version: " + version);
            }
        }
        return new AutoBuildTemplate(path, loadedBlocks);
    }

    private static void loadV2(WsonArray jsonBlocks, LinkedHashSet<BlockData> loadedBlocks) throws JsonException {
        for (int i = 0; i < jsonBlocks.size(); ++i) {
            WsonObject jsonBlock = jsonBlocks.getObject(i);
            try {
                WsonArray jsonPos = jsonBlock.getArray("pos");
                int[] pos = new int[]{jsonPos.getInt(0), jsonPos.getInt(1), jsonPos.getInt(2)};
                String name = jsonBlock.getString("block", "");
                loadedBlocks.add(new BlockData(pos, name));
                continue;
            }
            catch (JsonException e) {
                throw new JsonException("Entry blocks[" + i + "] is not valid", e);
            }
        }
    }

    private static void loadV1(WsonArray jsonBlocks, LinkedHashSet<BlockData> loadedBlocks) throws JsonException {
        for (int i = 0; i < jsonBlocks.size(); ++i) {
            WsonArray jsonBlock = jsonBlocks.getArray(i);
            try {
                int[] pos = new int[]{jsonBlock.getInt(0), jsonBlock.getInt(1), jsonBlock.getInt(2)};
                loadedBlocks.add(new BlockData(pos, ""));
                continue;
            }
            catch (JsonException e) {
                throw new JsonException("Entry blocks[" + i + "] is not valid", e);
            }
        }
    }

    public LinkedHashMap<class_2338, class_1792> getBlocksToPlace(class_2338 origin, class_2350 direction) {
        class_2350 front = direction;
        class_2350 left = front.method_10160();
        LinkedHashMap<class_2338, class_1792> blocksToPlace = new LinkedHashMap<class_2338, class_1792>();
        for (BlockData block : this.blocks) {
            class_2338 pos = block.toBlockPos(origin, front, left);
            class_1792 item = block.toItem();
            blocksToPlace.put(pos, item);
        }
        return blocksToPlace;
    }

    public int size() {
        return this.blocks.size();
    }

    public boolean isSelected(FileSetting setting) {
        return this.path.equals(setting.getSelectedFile());
    }

    public String getName() {
        return this.name;
    }

    private record BlockData(int[] pos, String name) {
        public class_2338 toBlockPos(class_2338 origin, class_2350 front, class_2350 left) {
            return origin.method_10079(left, this.pos[0]).method_10086(this.pos[1]).method_10079(front, this.pos[2]);
        }

        public class_1792 toItem() {
            return BlockUtils.getBlockFromName(this.name).method_8389();
        }
    }
}
