package net.wurstclient.hacks.autofarm;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_4587;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.RenderUtils;

public final class AutoFarmRenderer {
    private static final class_238 BLOCK_BOX = new class_238(class_2338.field_10980).method_1011(0.0625);
    private static final class_238 NODE_BOX = new class_238(class_2338.field_10980).method_1011(0.25);
    public final CheckboxSetting drawReplantingSpots = new CheckboxSetting("Draw replanting spots", true);
    public final CheckboxSetting drawBlocksToHarvest = new CheckboxSetting("Draw blocks to harvest", true);
    public final CheckboxSetting drawBlocksToReplant = new CheckboxSetting("Draw blocks to replant", true);
    private List<class_238> replantingSpots = List.of();
    private List<class_238> blocksToHarvest = List.of();
    private List<class_238> blocksToReplant = List.of();

    public void update(Collection<class_2338> replantingSpots, Collection<class_2338> blocksToHarvest, Collection<class_2338> blocksToReplant) {
        this.replantingSpots = replantingSpots.stream().map(arg_0 -> ((class_238)NODE_BOX).method_996(arg_0)).toList();
        this.blocksToHarvest = blocksToHarvest.stream().map(arg_0 -> ((class_238)BLOCK_BOX).method_996(arg_0)).toList();
        this.blocksToReplant = blocksToReplant.stream().map(arg_0 -> ((class_238)BLOCK_BOX).method_996(arg_0)).toList();
    }

    public void render(class_4587 matrixStack) {
        if (this.drawReplantingSpots.isChecked()) {
            RenderUtils.drawNodes(matrixStack, this.replantingSpots, -2147418113, false);
        }
        if (this.drawBlocksToHarvest.isChecked()) {
            RenderUtils.drawOutlinedBoxes(matrixStack, this.blocksToHarvest, -2147418368, false);
        }
        if (this.drawBlocksToReplant.isChecked()) {
            RenderUtils.drawOutlinedBoxes(matrixStack, this.blocksToReplant, -2130771968, false);
        }
    }

    public Stream<Setting> getSettings() {
        return Stream.of(this.drawReplantingSpots, this.drawBlocksToHarvest, this.drawBlocksToReplant);
    }
}
