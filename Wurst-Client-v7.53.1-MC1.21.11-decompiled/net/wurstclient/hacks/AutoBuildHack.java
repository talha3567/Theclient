package net.wurstclient.hacks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.FaceTargetSetting;
import net.wurstclient.settings.FileSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.AutoBuildTemplate;
import net.wurstclient.util.BlockPlacer;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.DefaultAutoBuildTemplates;
import net.wurstclient.util.InteractionSimulator;
import net.wurstclient.util.InventoryUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;
import net.wurstclient.util.json.JsonException;

public final class AutoBuildHack
extends Hack
implements UpdateListener,
RightClickListener,
RenderListener {
    private static final class_238 BLOCK_BOX = new class_238(0.0625, 0.0625, 0.0625, 0.9375, 0.9375, 0.9375);
    private final FileSetting templateSetting = new FileSetting("Template", "Determines what to build.\n\nTemplates are just JSON files. Feel free to add your own or to edit / delete the default templates.\n\nIf you mess up, simply press the 'Reset to Defaults' button or delete the folder.", "autobuild", DefaultAutoBuildTemplates::createFiles);
    private final SliderSetting range = new SliderSetting("Range", "How far to reach when placing blocks.\nRecommended values:\n6.0 for vanilla\n4.25 for NoCheat+", 6.0, 1.0, 10.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting checkLOS = new CheckboxSetting("Check line of sight", "Makes sure that you don't reach through walls when placing blocks. Can help with AntiCheat plugins but slows down building.", false);
    private final CheckboxSetting useSavedBlocks = new CheckboxSetting("Use saved blocks", "Tries to place the same blocks that were saved in the template.\n\nIf the template does not specify block types, it will be built from whatever block you are holding.", true);
    private final FaceTargetSetting faceTarget = FaceTargetSetting.withoutPacketSpam(this, FaceTargetSetting.FaceTarget.SERVER);
    private final SwingHandSetting swingHand = new SwingHandSetting(this, SwingHandSetting.SwingHand.SERVER);
    private final CheckboxSetting fastPlace = new CheckboxSetting("Always FastPlace", "Builds as if FastPlace was enabled, even if it's not.", true);
    private final CheckboxSetting strictBuildOrder = new CheckboxSetting("Strict build order", "Places blocks in exactly the same order that they appear in the template. This is slower, but provides more consistent results.", false);
    private Status status = Status.NO_TEMPLATE;
    private AutoBuildTemplate template;
    private LinkedHashMap<class_2338, class_1792> remainingBlocks = new LinkedHashMap();

    public AutoBuildHack() {
        super("AutoBuild");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.templateSetting);
        this.addSetting(this.range);
        this.addSetting(this.checkLOS);
        this.addSetting(this.useSavedBlocks);
        this.addSetting(this.faceTarget);
        this.addSetting(this.swingHand);
        this.addSetting(this.fastPlace);
        this.addSetting(this.strictBuildOrder);
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
                break;
            }
            case 3: {
                double total = this.template.size();
                double placed = total - (double)this.remainingBlocks.size();
                double progress = (double)Math.round(placed / total * 10000.0) / 100.0;
                name = (String)name + " [" + this.template.getName() + "] " + progress + "%";
            }
        }
        return name;
    }

    @Override
    protected void onEnable() {
        AutoBuildHack.WURST.getHax().instaBuildHack.setEnabled(false);
        AutoBuildHack.WURST.getHax().templateToolHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RightClickListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RightClickListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.remainingBlocks.clear();
        this.status = this.template == null ? Status.NO_TEMPLATE : Status.IDLE;
    }

    @Override
    public void onRightClick(RightClickListener.RightClickEvent event) {
        if (this.status != Status.IDLE) {
            return;
        }
        class_239 hitResult = AutoBuildHack.MC.field_1765;
        if (hitResult == null || hitResult.method_17783() != class_239.class_240.field_1332 || !(hitResult instanceof class_3965)) {
            return;
        }
        class_3965 blockHitResult = (class_3965)hitResult;
        class_2338 hitResultPos = blockHitResult.method_17777();
        if (!BlockUtils.canBeClicked(hitResultPos)) {
            return;
        }
        class_2338 startPos = hitResultPos.method_10093(blockHitResult.method_17780());
        class_2350 direction = AutoBuildHack.MC.field_1724.method_5735();
        this.remainingBlocks = this.template.getBlocksToPlace(startPos, direction);
        this.status = Status.BUILDING;
    }

    @Override
    public void onUpdate() {
        switch (this.status.ordinal()) {
            case 0: {
                this.loadSelectedTemplate();
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                if (this.template.isSelected(this.templateSetting)) break;
                this.loadSelectedTemplate();
                break;
            }
            case 3: {
                this.buildNormally();
            }
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (this.status != Status.BUILDING) {
            return;
        }
        List<class_2338> blocksToDraw = this.remainingBlocks.keySet().stream().filter(pos -> BlockUtils.getState(pos).method_45474()).limit(1024L).toList();
        int black = Integer.MIN_VALUE;
        List<class_238> outlineBoxes = blocksToDraw.stream().map(pos -> BLOCK_BOX.method_996(pos)).toList();
        RenderUtils.drawOutlinedBoxes(matrixStack, outlineBoxes, black, true);
        int green = 637599488;
        class_243 eyesPos = RotationUtils.getEyesPos();
        double rangeSq = this.range.getValueSq();
        List<class_238> greenBoxes = blocksToDraw.stream().filter(pos -> pos.method_19770((class_2374)eyesPos) <= rangeSq).map(pos -> BLOCK_BOX.method_996(pos)).toList();
        RenderUtils.drawSolidBoxes(matrixStack, greenBoxes, green, true);
    }

    private void buildNormally() {
        this.remainingBlocks.keySet().removeIf(pos -> !BlockUtils.getState(pos).method_45474());
        if (this.remainingBlocks.isEmpty()) {
            this.status = Status.IDLE;
            return;
        }
        if (!this.fastPlace.isChecked() && AutoBuildHack.MC.field_1752 > 0) {
            return;
        }
        double rangeSq = this.range.getValueSq();
        for (Map.Entry<class_2338, class_1792> entry : this.remainingBlocks.entrySet()) {
            class_2338 pos2 = entry.getKey();
            class_1792 item = entry.getValue();
            BlockPlacer.BlockPlacingParams params = BlockPlacer.getBlockPlacingParams(pos2);
            if (params == null || params.distanceSq() > rangeSq || params.requiresSneaking() || this.checkLOS.isChecked() && !params.lineOfSight()) {
                if (!this.strictBuildOrder.isChecked()) continue;
                return;
            }
            if (this.useSavedBlocks.isChecked() && item != class_1802.field_8162 && !AutoBuildHack.MC.field_1724.method_6047().method_31574(item)) {
                this.giveOrSelectItem(item);
                return;
            }
            AutoBuildHack.MC.field_1752 = 4;
            this.faceTarget.face(params.hitVec());
            InteractionSimulator.rightClickBlock(params.toHitResult(), (SwingHandSetting.SwingHand)((Object)this.swingHand.getSelected()));
            return;
        }
    }

    private void giveOrSelectItem(class_1792 item) {
        if (InventoryUtils.selectItem(item, 36, true)) {
            return;
        }
        if (!AutoBuildHack.MC.field_1724.method_56992()) {
            return;
        }
        class_1661 inventory = AutoBuildHack.MC.field_1724.method_31548();
        int slot = inventory.method_7376();
        if (slot < 0) {
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

    public Path getFolder() {
        return this.templateSetting.getFolder();
    }

    private static enum Status {
        NO_TEMPLATE,
        LOADING,
        IDLE,
        BUILDING;

    }
}
