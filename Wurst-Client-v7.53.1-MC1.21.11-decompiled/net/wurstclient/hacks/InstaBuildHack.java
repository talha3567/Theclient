package net.wurstclient.hacks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_239;
import net.minecraft.class_3965;
import net.wurstclient.Category;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.FileSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.AutoBuildTemplate;
import net.wurstclient.util.BlockPlacer;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.InteractionSimulator;
import net.wurstclient.util.InventoryUtils;
import net.wurstclient.util.json.JsonException;

public final class InstaBuildHack
extends Hack
implements UpdateListener,
RightClickListener {
    private final FileSetting templateSetting = new FileSetting("Template", "Determines what to build.\n\nTemplates are just JSON files. Feel free to add your own or to edit / delete the default templates.\n\nIf you mess up, simply press the 'Reset to Defaults' button or delete the folder.", "autobuild", path -> {});
    private final SliderSetting range = new SliderSetting("Range", "How far to reach when placing blocks.\nRecommended values:\n6.0 for vanilla\n4.25 for NoCheat+", 6.0, 1.0, 10.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting useSavedBlocks = new CheckboxSetting("Use saved blocks", "Tries to place the same blocks that were saved in the template.\n\nIf the template does not specify block types, it will be built from whatever block you are holding.", false);
    private Status status = Status.NO_TEMPLATE;
    private AutoBuildTemplate template;
    private LinkedHashMap<class_2338, class_1792> remainingBlocks = new LinkedHashMap();

    public InstaBuildHack() {
        super("InstaBuild");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.templateSetting);
        this.addSetting(this.range);
        this.addSetting(this.useSavedBlocks);
    }

    @Override
    public String getRenderName() {
        Object name = this.getName();
        switch (this.status.ordinal()) {
            case 0: {
                break;
            }
            case 1: {
                name = (String)name + " [Loading...]";
                break;
            }
            case 2: {
                name = (String)name + " [" + this.template.getName() + "]";
            }
        }
        return name;
    }

    @Override
    protected void onEnable() {
        InstaBuildHack.WURST.getHax().autoBuildHack.setEnabled(false);
        InstaBuildHack.WURST.getHax().templateToolHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RightClickListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RightClickListener.class, this);
        this.remainingBlocks.clear();
        this.status = this.template == null ? Status.NO_TEMPLATE : Status.IDLE;
    }

    @Override
    public void onRightClick(RightClickListener.RightClickEvent event) {
        if (this.status != Status.IDLE) {
            return;
        }
        class_239 hitResult = InstaBuildHack.MC.field_1765;
        if (hitResult == null || hitResult.method_17783() != class_239.class_240.field_1332 || !(hitResult instanceof class_3965)) {
            return;
        }
        class_3965 blockHitResult = (class_3965)hitResult;
        class_2338 hitResultPos = blockHitResult.method_17777();
        if (!BlockUtils.canBeClicked(hitResultPos)) {
            return;
        }
        class_2338 startPos = hitResultPos.method_10093(blockHitResult.method_17780());
        class_2350 direction = InstaBuildHack.MC.field_1724.method_5735();
        this.remainingBlocks = this.template.getBlocksToPlace(startPos, direction);
        this.buildInstantly();
    }

    @Override
    public void onUpdate() {
        switch (this.status.ordinal()) {
            case 0: {
                this.loadSelectedTemplate();
                break;
            }
            default: {
                break;
            }
            case 2: {
                if (this.template.isSelected(this.templateSetting)) break;
                this.loadSelectedTemplate();
            }
        }
    }

    private void buildInstantly() {
        class_1661 inventory = InstaBuildHack.MC.field_1724.method_31548();
        int oldSlot = inventory.method_67532();
        for (Map.Entry<class_2338, class_1792> entry : this.remainingBlocks.entrySet()) {
            BlockPlacer.BlockPlacingParams params;
            class_2338 pos = entry.getKey();
            class_1792 item = entry.getValue();
            if (!BlockUtils.getState(pos).method_45474() || (params = BlockPlacer.getBlockPlacingParams(pos)) == null || params.distanceSq() > this.range.getValueSq() || params.requiresSneaking()) continue;
            if (this.useSavedBlocks.isChecked() && item != class_1802.field_8162 && !InstaBuildHack.MC.field_1724.method_6047().method_31574(item)) {
                this.giveOrSelectItem(item);
            }
            InteractionSimulator.rightClickBlock(params.toHitResult(), SwingHandSetting.SwingHand.OFF);
        }
        inventory.method_61496(oldSlot);
        this.remainingBlocks.clear();
    }

    private void giveOrSelectItem(class_1792 item) {
        if (InventoryUtils.selectItem(item, 9)) {
            return;
        }
        if (!InstaBuildHack.MC.field_1724.method_56992()) {
            return;
        }
        class_1661 inventory = InstaBuildHack.MC.field_1724.method_31548();
        int slot = inventory.method_7376();
        if (!class_1661.method_7380((int)slot)) {
            slot = inventory.method_67532();
        }
        class_1799 stack = new class_1799((class_1935)item);
        InventoryUtils.setCreativeStack(slot, stack);
    }

    private void loadSelectedTemplate() {
        this.status = Status.LOADING;
        Path path = this.templateSetting.getSelectedFile();
        try {
            this.template = AutoBuildTemplate.load(path);
            this.status = Status.IDLE;
        }
        catch (IOException | JsonException e) {
            Path fileName = path.getFileName();
            ChatUtils.error("Couldn't load template '" + String.valueOf(fileName) + "'.");
            String simpleClassName = e.getClass().getSimpleName();
            String message = e.getMessage();
            ChatUtils.message(simpleClassName + ": " + message);
            e.printStackTrace();
            this.setEnabled(false);
        }
    }

    private static enum Status {
        NO_TEMPLATE,
        LOADING,
        IDLE;

    }
}
