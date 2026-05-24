package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.systems.modules.world.InfinityMiner;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class AutoTool
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWhitelist;
    private final Setting<EnchantPreference> prefer;
    private final Setting<Boolean> silkTouchForEnderChest;
    private final Setting<Boolean> fortuneForOresCrops;
    private final Setting<Boolean> antiBreak;
    private final Setting<Integer> breakDurability;
    private final Setting<Boolean> switchBack;
    private final Setting<Integer> switchDelay;
    private final Setting<ListMode> listMode;
    private final Setting<List<Item>> whitelist;
    private final Setting<List<Item>> blacklist;
    private boolean wasPressed;
    private boolean shouldSwitch;
    private int ticks;
    private int bestSlot;

    public AutoTool() {
        super(Categories.Player, "auto-tool", "Automatically switches to the most effective tool when performing an action.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgWhitelist = this.settings.createGroup("Whitelist");
        this.prefer = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("prefer")).description("Either to prefer Silk Touch, Fortune, or none.")).defaultValue(EnchantPreference.Fortune)).build());
        this.silkTouchForEnderChest = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("silk-touch-for-ender-chest")).description("Mines Ender Chests only with the Silk Touch enchantment.")).defaultValue(true)).build());
        this.fortuneForOresCrops = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fortune-for-ores-and-crops")).description("Mines Ores and crops only with the Fortune enchantment.")).defaultValue(false)).build());
        this.antiBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-break")).description("Stops you from breaking your tool.")).defaultValue(false)).build());
        this.breakDurability = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("anti-break-percentage")).description("The durability percentage to stop using a tool.")).defaultValue(10)).range(1, 100).sliderRange(1, 100).visible(this.antiBreak::get)).build());
        this.switchBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("switch-back")).description("Switches your hand to whatever was selected when releasing your attack key.")).defaultValue(false)).build());
        this.switchDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("switch-delay")).description("Delay in ticks before switching tools.")).defaultValue(0)).build());
        this.listMode = this.sgWhitelist.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("list-mode")).description("Selection mode.")).defaultValue(ListMode.Blacklist)).build());
        this.whitelist = this.sgWhitelist.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("whitelist")).description("The tools you want to use.")).visible(() -> this.listMode.get() == ListMode.Whitelist)).filter(AutoTool::isTool).build());
        this.blacklist = this.sgWhitelist.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("blacklist")).description("The tools you don't want to use.")).visible(() -> this.listMode.get() == ListMode.Blacklist)).filter(AutoTool::isTool).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (Modules.get().isActive(InfinityMiner.class)) {
            return;
        }
        if (this.switchBack.get().booleanValue() && !this.mc.options.keyAttack.isDown() && this.wasPressed && InvUtils.previousSlot != -1) {
            InvUtils.swapBack();
            this.wasPressed = false;
            return;
        }
        if (this.ticks <= 0 && this.shouldSwitch && this.bestSlot != -1) {
            InvUtils.swap(this.bestSlot, this.switchBack.get());
            this.shouldSwitch = false;
        } else {
            --this.ticks;
        }
        this.wasPressed = this.mc.options.keyAttack.isDown();
    }

    @EventHandler(priority=100)
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (Modules.get().isActive(InfinityMiner.class)) {
            return;
        }
        if (this.mc.player.isCreative()) {
            return;
        }
        BlockState blockState = this.mc.level.getBlockState(event.blockPos);
        if (!BlockUtils.canBreak(event.blockPos, blockState)) {
            return;
        }
        ItemStack currentStack = this.mc.player.getMainHandItem();
        double bestScore = -1.0;
        this.bestSlot = -1;
        for (int i = 0; i < 9; ++i) {
            double score;
            ItemStack itemStack3 = this.mc.player.getInventory().getItem(i);
            if (this.listMode.get() == ListMode.Whitelist && !this.whitelist.get().contains(itemStack3.getItem()) || this.listMode.get() == ListMode.Blacklist && this.blacklist.get().contains(itemStack3.getItem()) || (score = AutoTool.getScore(itemStack3, blockState, this.silkTouchForEnderChest.get(), this.fortuneForOresCrops.get(), this.prefer.get(), itemStack2 -> !this.shouldStopUsing((ItemStack)itemStack2))) < 0.0 || !(score > bestScore)) continue;
            bestScore = score;
            this.bestSlot = i;
        }
        if (this.bestSlot != -1 && bestScore > AutoTool.getScore(currentStack, blockState, this.silkTouchForEnderChest.get(), this.fortuneForOresCrops.get(), this.prefer.get(), itemStack -> !this.shouldStopUsing((ItemStack)itemStack)) || this.shouldStopUsing(currentStack) || !AutoTool.isTool(currentStack)) {
            this.ticks = this.switchDelay.get();
            if (this.ticks == 0) {
                InvUtils.swap(this.bestSlot, true);
            } else {
                this.shouldSwitch = true;
            }
        }
        if (this.shouldStopUsing(currentStack = this.mc.player.getMainHandItem()) && AutoTool.isTool(currentStack)) {
            this.mc.options.keyAttack.setDown(false);
            event.cancel();
        }
    }

    private boolean shouldStopUsing(ItemStack itemStack) {
        return this.antiBreak.get() != false && itemStack.getMaxDamage() - itemStack.getDamageValue() < itemStack.getMaxDamage() * this.breakDurability.get() / 100;
    }

    public static double getScore(ItemStack itemStack, BlockState state, boolean silkTouchEnderChest, boolean fortuneOre, EnchantPreference enchantPreference, Predicate<ItemStack> good) {
        if (!good.test(itemStack) || !AutoTool.isTool(itemStack)) {
            return -1.0;
        }
        if (!(itemStack.isCorrectToolForDrops(state) || itemStack.is(ItemTags.SWORDS) && (state.getBlock() instanceof BambooStalkBlock || state.getBlock() instanceof BambooSaplingBlock) || itemStack.getItem() instanceof ShearsItem && state.getBlock() instanceof LeavesBlock || state.is(BlockTags.WOOL))) {
            return -1.0;
        }
        if (silkTouchEnderChest && state.getBlock() == Blocks.ENDER_CHEST && !Utils.hasEnchantments(itemStack, Enchantments.SILK_TOUCH)) {
            return -1.0;
        }
        if (fortuneOre && AutoTool.isFortunable(state.getBlock()) && !Utils.hasEnchantments(itemStack, Enchantments.FORTUNE)) {
            return -1.0;
        }
        double score = 0.0;
        score += (double)(itemStack.getDestroySpeed(state) * 1000.0f);
        score += (double)Utils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.UNBREAKING);
        score += (double)Utils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.EFFICIENCY);
        score += (double)Utils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.MENDING);
        if (enchantPreference == EnchantPreference.Fortune) {
            score += (double)Utils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.FORTUNE);
        }
        if (enchantPreference == EnchantPreference.SilkTouch) {
            score += (double)Utils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.SILK_TOUCH);
        }
        if (itemStack.is(ItemTags.SWORDS) && (state.getBlock() instanceof BambooStalkBlock || state.getBlock() instanceof BambooSaplingBlock)) {
            score += (double)(9000.0f + ((Tool)itemStack.get(DataComponents.TOOL)).getMiningSpeed(state) * 1000.0f);
        }
        return score;
    }

    public static boolean isTool(Item item) {
        return AutoTool.isTool(item.getDefaultInstance());
    }

    public static boolean isTool(ItemStack itemStack) {
        return itemStack.is(ItemTags.AXES) || itemStack.is(ItemTags.HOES) || itemStack.is(ItemTags.PICKAXES) || itemStack.is(ItemTags.SHOVELS) || itemStack.getItem() instanceof ShearsItem;
    }

    private static boolean isFortunable(Block block) {
        if (block == Blocks.ANCIENT_DEBRIS) {
            return false;
        }
        return Xray.ORES.contains(block) || block instanceof CropBlock;
    }

    public static enum EnchantPreference {
        None,
        Fortune,
        SilkTouch;

    }

    public static enum ListMode {
        Whitelist,
        Blacklist;

    }
}
