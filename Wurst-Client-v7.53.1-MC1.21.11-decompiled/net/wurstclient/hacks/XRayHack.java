package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.class_11954;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_2680;
import net.minecraft.class_437;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.clickgui.screens.EditBlockListScreen;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.events.VisGraphListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.FullbrightHack;
import net.wurstclient.mixinterface.ISimpleOption;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;

@SearchTags(value={"XRay", "x ray", "OreFinder", "ore finder"})
public final class XRayHack
extends Hack
implements UpdateListener,
VisGraphListener {
    private final BlockListSetting ores = new BlockListSetting("Ores", "A list of blocks that X-Ray will show. They don't have to be just ores - you can add any block you want.\n\nRemember to restart X-Ray when changing this setting.", "minecraft:amethyst_cluster", "minecraft:ancient_debris", "minecraft:anvil", "minecraft:beacon", "minecraft:bone_block", "minecraft:bookshelf", "minecraft:brewing_stand", "minecraft:budding_amethyst", "minecraft:chain_command_block", "minecraft:chest", "minecraft:coal_block", "minecraft:coal_ore", "minecraft:command_block", "minecraft:copper_ore", "minecraft:crafter", "minecraft:crafting_table", "minecraft:creaking_heart", "minecraft:decorated_pot", "minecraft:deepslate_coal_ore", "minecraft:deepslate_copper_ore", "minecraft:deepslate_diamond_ore", "minecraft:deepslate_emerald_ore", "minecraft:deepslate_gold_ore", "minecraft:deepslate_iron_ore", "minecraft:deepslate_lapis_ore", "minecraft:deepslate_redstone_ore", "minecraft:diamond_block", "minecraft:diamond_ore", "minecraft:dispenser", "minecraft:dropper", "minecraft:emerald_block", "minecraft:emerald_ore", "minecraft:enchanting_table", "minecraft:end_portal", "minecraft:end_portal_frame", "minecraft:ender_chest", "minecraft:furnace", "minecraft:glowstone", "minecraft:gold_block", "minecraft:gold_ore", "minecraft:hopper", "minecraft:iron_block", "minecraft:iron_ore", "minecraft:ladder", "minecraft:lapis_block", "minecraft:lapis_ore", "minecraft:lava", "minecraft:lodestone", "minecraft:mossy_cobblestone", "minecraft:nether_gold_ore", "minecraft:nether_portal", "minecraft:nether_quartz_ore", "minecraft:raw_copper_block", "minecraft:raw_gold_block", "minecraft:raw_iron_block", "minecraft:redstone_block", "minecraft:redstone_ore", "minecraft:repeating_command_block", "minecraft:sculk_catalyst", "minecraft:sculk_sensor", "minecraft:sculk_shrieker", "minecraft:spawner", "minecraft:suspicious_gravel", "minecraft:suspicious_sand", "minecraft:tnt", "minecraft:torch", "minecraft:trapped_chest", "minecraft:trial_spawner", "minecraft:vault", "minecraft:wall_torch", "minecraft:water");
    private final CheckboxSetting onlyExposed = new CheckboxSetting("Only show exposed", "Only shows ores that would be visible in caves. This can help against anti-X-Ray plugins.\n\nRemember to restart X-Ray when changing this setting.", false);
    private final SliderSetting opacity = new SliderSetting("Opacity", "Opacity of non-ore blocks when X-Ray is enabled.\n\nRemember to restart X-Ray when changing this setting.", 0.0, 0.0, 0.99, 0.01, SliderSetting.ValueDisplay.PERCENTAGE.withLabel(0.0, "off"));
    private final String optiFineWarning;
    private final String renderName = Math.random() < 0.01 && System.getProperty("fabric.client.gametest") == null ? "X-Wurst" : this.getName();
    private ArrayList<String> oreNamesCache;
    private final ThreadLocal<class_2338.class_2339> mutablePosForExposedCheck = ThreadLocal.withInitial(class_2338.class_2339::new);

    public XRayHack() {
        super("X-Ray");
        this.setCategory(Category.RENDER);
        this.addSetting(this.ores);
        this.addSetting(this.onlyExposed);
        this.addSetting(this.opacity);
        this.optiFineWarning = this.checkOptiFine();
    }

    @Override
    public String getRenderName() {
        return this.renderName;
    }

    @Override
    protected void onEnable() {
        this.oreNamesCache = new ArrayList<String>(this.ores.getBlockNames());
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(VisGraphListener.class, this);
        XRayHack.MC.field_1769.method_3279();
        if (this.optiFineWarning != null) {
            ChatUtils.warning(this.optiFineWarning);
        }
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(VisGraphListener.class, this);
        XRayHack.MC.field_1769.method_3279();
        FullbrightHack fullbright = XRayHack.WURST.getHax().fullbrightHack;
        if (!fullbright.isChangingGamma()) {
            ISimpleOption.get(XRayHack.MC.field_1690.method_42473()).forceSetValue(fullbright.getDefaultGamma());
        }
    }

    @Override
    public void onUpdate() {
        ISimpleOption.get(XRayHack.MC.field_1690.method_42473()).forceSetValue(16.0);
    }

    @Override
    public void onVisGraph(VisGraphListener.VisGraphEvent event) {
        event.cancel();
    }

    public Boolean shouldDrawSide(class_2680 state, class_2338 pos) {
        if (!this.isEnabled()) {
            return null;
        }
        boolean visible = this.isVisible(state.method_26204(), pos);
        if (!visible && this.opacity.getValue() > 0.0) {
            return null;
        }
        return visible;
    }

    public boolean shouldHideBlockEntity(class_11954 state) {
        if (!this.isEnabled()) {
            return false;
        }
        class_2338 pos = state.field_62673;
        class_2248 block = BlockUtils.getBlock(pos);
        return !this.isVisible(block, pos);
    }

    public boolean isVisible(class_2248 block, class_2338 pos) {
        boolean visible;
        String name = BlockUtils.getName(block);
        int index = Collections.binarySearch(this.oreNamesCache, name);
        boolean bl = visible = index >= 0;
        if (visible && this.onlyExposed.isChecked() && pos != null) {
            return this.isExposed(pos);
        }
        return visible;
    }

    private boolean isExposed(class_2338 pos) {
        class_2338.class_2339 mutablePos = this.mutablePosForExposedCheck.get();
        for (class_2350 direction : class_2350.values()) {
            if (BlockUtils.isOpaqueFullCube((class_2338)mutablePos.method_25505((class_2382)pos, direction))) continue;
            return true;
        }
        return false;
    }

    public boolean isOpacityMode() {
        return this.isEnabled() && this.opacity.getValue() > 0.0;
    }

    public int getOpacityColorMask() {
        return (int)(this.opacity.getValue() * 255.0) << 24 | 0xFFFFFF;
    }

    public float getOpacityFloat() {
        return this.opacity.getValueF();
    }

    public void openBlockListEditor(class_437 prevScreen) {
        MC.method_1507((class_437)new EditBlockListScreen(prevScreen, this.ores));
    }

    private String checkOptiFine() {
        Pattern optifine;
        Stream<String> mods = FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId);
        if (mods.anyMatch((optifine = Pattern.compile("opti(?:fine|fabric).*")).asPredicate())) {
            return "OptiFine is installed. X-Ray will not work properly!";
        }
        return null;
    }
}
