package net.wurstclient.hacks;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1728;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1890;
import net.minecraft.class_1914;
import net.minecraft.class_1916;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2863;
import net.minecraft.class_3852;
import net.minecraft.class_3966;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_492;
import net.minecraft.class_636;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.autolibrarian.BookOffer;
import net.wurstclient.hacks.autolibrarian.UpdateBooksSetting;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.BookOffersSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.FaceTargetSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockPlacer;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.InventoryUtils;
import net.wurstclient.util.OverlayRenderer;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"auto librarian", "AutoVillager", "auto villager", "VillagerTrainer", "villager trainer", "LibrarianTrainer", "librarian trainer", "AutoHmmm", "auto hmmm"})
public final class AutoLibrarianHack
extends Hack
implements UpdateListener,
RenderListener {
    private final BookOffersSetting wantedBooks = new BookOffersSetting("Wanted books", "A list of enchanted books that you want your villagers to sell.\n\nAutoLibrarian will stop training the current villager once it has learned to sell one of these books.\n\nYou can also set a maximum price for each book, in case you already have a villager selling it but you want it for a cheaper price.", "minecraft:depth_strider;3", "minecraft:efficiency;5", "minecraft:feather_falling;4", "minecraft:fortune;3", "minecraft:looting;3", "minecraft:mending;1", "minecraft:protection;4", "minecraft:respiration;3", "minecraft:sharpness;5", "minecraft:silk_touch;1", "minecraft:unbreaking;3");
    private final CheckboxSetting lockInTrade = new CheckboxSetting("Lock in trade", "Automatically buys something from the villager once it has learned to sell the book you want. This prevents the villager from changing its trade offers later.\n\nMake sure you have at least 24 paper and 9 emeralds in your inventory when using this feature. Alternatively, 1 book and 64 emeralds will also work.", false);
    private final UpdateBooksSetting updateBooks = new UpdateBooksSetting();
    private final SliderSetting range = new SliderSetting("Range", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final FaceTargetSetting faceTarget = FaceTargetSetting.withoutPacketSpam(this, FaceTargetSetting.FaceTarget.SERVER);
    private final SwingHandSetting swingHand = new SwingHandSetting(this, SwingHandSetting.SwingHand.SERVER);
    private final SliderSetting repairMode = new SliderSetting("Repair mode", "Prevents AutoLibrarian from using your axe when its durability reaches the given threshold, so you can repair it before it breaks.\nCan be adjusted from 0 (off) to 100 remaining uses.", 1.0, 0.0, 100.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withLabel(0.0, "off"));
    private final OverlayRenderer overlay = new OverlayRenderer();
    private final HashSet<class_1646> experiencedVillagers = new HashSet();
    private class_1646 villager;
    private class_2338 jobSite;
    private boolean placingJobSite;
    private boolean breakingJobSite;

    public AutoLibrarianHack() {
        super("AutoLibrarian");
        this.setCategory(Category.OTHER);
        this.addSetting(this.wantedBooks);
        this.addSetting(this.lockInTrade);
        this.addSetting(this.updateBooks);
        this.addSetting(this.range);
        this.addSetting(this.faceTarget);
        this.addSetting(this.swingHand);
        this.addSetting(this.repairMode);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        if (this.breakingJobSite) {
            AutoLibrarianHack.MC.field_1761.field_3717 = true;
            AutoLibrarianHack.MC.field_1761.method_2925();
            this.breakingJobSite = false;
        }
        this.overlay.resetProgress();
        this.villager = null;
        this.jobSite = null;
        this.placingJobSite = false;
        this.breakingJobSite = false;
        this.experiencedVillagers.clear();
    }

    @Override
    public void onUpdate() {
        if (this.villager == null) {
            this.setTargetVillager();
            return;
        }
        if (this.jobSite == null) {
            this.setTargetJobSite();
            return;
        }
        if (this.placingJobSite && this.breakingJobSite) {
            throw new IllegalStateException("Trying to place and break job site at the same time. Something is wrong.");
        }
        if (this.placingJobSite) {
            this.placeJobSite();
            return;
        }
        if (this.breakingJobSite) {
            this.breakJobSite();
            return;
        }
        class_437 class_4372 = AutoLibrarianHack.MC.field_1755;
        if (!(class_4372 instanceof class_492)) {
            this.openTradeScreen();
            return;
        }
        class_492 tradeScreen = (class_492)class_4372;
        int experience = ((class_1728)tradeScreen.method_17577()).method_19254();
        if (experience > 0) {
            ChatUtils.warning("Villager at " + this.villager.method_24515().method_23854() + " is already experienced, meaning it can't be trained anymore.");
            ChatUtils.message("Looking for another villager...");
            this.experiencedVillagers.add(this.villager);
            this.villager = null;
            this.jobSite = null;
            this.closeTradeScreen();
            return;
        }
        BookOffer bookOffer = this.findEnchantedBookOffer(((class_1728)tradeScreen.method_17577()).method_17438());
        if (bookOffer == null) {
            ChatUtils.message("Villager is not selling an enchanted book.");
            this.closeTradeScreen();
            this.breakingJobSite = true;
            System.out.println("Breaking job site...");
            return;
        }
        ChatUtils.message("Villager is selling " + bookOffer.getEnchantmentNameWithLevel() + " for " + bookOffer.getFormattedPrice() + ".");
        if (!this.wantedBooks.isWanted(bookOffer)) {
            this.breakingJobSite = true;
            System.out.println("Breaking job site...");
            this.closeTradeScreen();
            return;
        }
        if (this.lockInTrade.isChecked()) {
            ((class_1728)tradeScreen.method_17577()).method_7650(0);
            ((class_1728)tradeScreen.method_17577()).method_20215(0);
            MC.method_1562().method_52787((class_2596)new class_2863(0));
            AutoLibrarianHack.MC.field_1761.method_2906(((class_1728)tradeScreen.method_17577()).field_7763, 2, 0, class_1713.field_7790, (class_1657)AutoLibrarianHack.MC.field_1724);
            this.closeTradeScreen();
        }
        ((UpdateBooksSetting.UpdateBooks)((Object)this.updateBooks.getSelected())).update(this.wantedBooks, bookOffer);
        ChatUtils.message("Done!");
        this.setEnabled(false);
    }

    private void breakJobSite() {
        if (this.jobSite == null) {
            throw new IllegalStateException("Job site is null.");
        }
        BlockBreaker.BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(this.jobSite);
        if (params == null || BlockUtils.getState(this.jobSite).method_45474()) {
            System.out.println("Job site has been broken. Replacing...");
            this.breakingJobSite = false;
            this.placingJobSite = true;
            return;
        }
        AutoLibrarianHack.WURST.getHax().autoToolHack.equipBestTool(this.jobSite, false, true, this.repairMode.getValueI());
        this.faceTarget.face(params.hitVec());
        if (AutoLibrarianHack.MC.field_1761.method_2902(this.jobSite, params.side())) {
            this.swingHand.swing(class_1268.field_5808);
        }
        this.overlay.updateProgress();
    }

    private void placeJobSite() {
        class_1269.class_9860 success;
        if (this.jobSite == null) {
            throw new IllegalStateException("Job site is null.");
        }
        if (!BlockUtils.getState(this.jobSite).method_45474()) {
            if (BlockUtils.getBlock(this.jobSite) == class_2246.field_16330) {
                System.out.println("Job site has been placed.");
                this.placingJobSite = false;
            } else {
                System.out.println("Found wrong block at job site. Breaking...");
                this.breakingJobSite = true;
                this.placingJobSite = false;
            }
            return;
        }
        if (!AutoLibrarianHack.MC.field_1724.method_24518(class_1802.field_16312)) {
            InventoryUtils.selectItem(class_1802.field_16312, 36);
            return;
        }
        class_1268 hand = AutoLibrarianHack.MC.field_1724.method_6047().method_31574(class_1802.field_16312) ? class_1268.field_5808 : class_1268.field_5810;
        IKeyMapping sneakKey = IKeyMapping.get(AutoLibrarianHack.MC.field_1690.field_1832);
        sneakKey.method_23481(true);
        if (!AutoLibrarianHack.MC.field_1724.method_5715()) {
            return;
        }
        BlockPlacer.BlockPlacingParams params = BlockPlacer.getBlockPlacingParams(this.jobSite);
        if (params == null) {
            sneakKey.resetPressedState();
            return;
        }
        this.faceTarget.face(params.hitVec());
        class_1269 result = AutoLibrarianHack.MC.field_1761.method_2896(AutoLibrarianHack.MC.field_1724, hand, params.toHitResult());
        if (result instanceof class_1269.class_9860 && (success = (class_1269.class_9860)result).comp_2909() == class_1269.class_9861.field_52427) {
            this.swingHand.swing(hand);
        }
        sneakKey.resetPressedState();
    }

    private void openTradeScreen() {
        class_1269.class_9860 success;
        if (AutoLibrarianHack.MC.field_1752 > 0) {
            return;
        }
        class_636 im = AutoLibrarianHack.MC.field_1761;
        class_746 player = AutoLibrarianHack.MC.field_1724;
        if (EntityUtils.distanceToHitboxSq((class_1297)this.villager) > this.range.getValueSq()) {
            ChatUtils.error("Villager is out of range. Consider trapping the villager so it doesn't wander away.");
            this.setEnabled(false);
            return;
        }
        class_238 box = this.villager.method_5829();
        class_243 start = RotationUtils.getEyesPos();
        class_243 end = box.method_1005();
        class_243 hitVec = box.method_992(start, end).orElse(start);
        class_3966 hitResult = new class_3966((class_1297)this.villager, hitVec);
        this.faceTarget.face(end);
        class_1268 hand = class_1268.field_5808;
        class_1269 actionResult = im.method_2917((class_1657)player, (class_1297)this.villager, hitResult, hand);
        if (!actionResult.method_23665()) {
            im.method_2905((class_1657)player, (class_1297)this.villager, hand);
        }
        if (actionResult instanceof class_1269.class_9860 && (success = (class_1269.class_9860)actionResult).comp_2909() == class_1269.class_9861.field_52427) {
            this.swingHand.swing(hand);
        }
        AutoLibrarianHack.MC.field_1752 = 4;
    }

    private void closeTradeScreen() {
        AutoLibrarianHack.MC.field_1724.method_7346();
        AutoLibrarianHack.MC.field_1752 = 4;
    }

    private BookOffer findEnchantedBookOffer(class_1916 tradeOffers) {
        for (class_1914 tradeOffer : tradeOffers) {
            int price;
            int level;
            Set enchantmentLevelMap;
            class_1799 stack = tradeOffer.method_8250();
            if (stack.method_7909() != class_1802.field_8598 || (enchantmentLevelMap = class_1890.method_57532((class_1799)stack).method_57539()).isEmpty()) continue;
            Object2IntMap.Entry firstEntry = (Object2IntMap.Entry)enchantmentLevelMap.stream().findFirst().orElseThrow();
            String enchantment = ((class_6880)firstEntry.getKey()).method_55840();
            BookOffer bookOffer = new BookOffer(enchantment, level = firstEntry.getIntValue(), price = tradeOffer.method_19272().method_7947());
            if (!bookOffer.isFullyValid()) {
                System.out.println("Found invalid enchanted book offer.\nComponent data: " + String.valueOf(enchantmentLevelMap));
                continue;
            }
            return bookOffer;
        }
        return null;
    }

    private void setTargetVillager() {
        double rangeSq = this.range.getValueSq();
        Stream<class_1646> stream = StreamSupport.stream(AutoLibrarianHack.MC.field_1687.method_18112().spliterator(), true).filter(e -> !e.method_31481()).filter(class_1646.class::isInstance).map(e -> (class_1646)e).filter(e -> e.method_6032() > 0.0f).filter(e -> EntityUtils.distanceToHitboxSq((class_1297)e) <= rangeSq).filter(e -> e.method_7231().comp_3521().method_40230().orElse(null) == class_3852.field_17060).filter(e -> e.method_7231().comp_3522() == 1).filter(e -> !this.experiencedVillagers.contains(e));
        this.villager = stream.min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
        if (this.villager == null) {
            Object errorMsg = "Couldn't find a nearby librarian.";
            int numExperienced = this.experiencedVillagers.size();
            if (numExperienced > 0) {
                errorMsg = (String)errorMsg + " (Except for " + numExperienced + " that " + (numExperienced == 1 ? "is" : "are") + " already experienced.)";
            }
            ChatUtils.error((String)errorMsg);
            ChatUtils.message("Make sure both the librarian and the lectern are reachable from where you are standing.");
            this.setEnabled(false);
            return;
        }
        System.out.println("Found villager at " + String.valueOf(this.villager.method_24515()));
    }

    private void setTargetJobSite() {
        class_243 eyesVec = RotationUtils.getEyesPos();
        double rangeSq = this.range.getValueSq();
        Stream<class_2338> stream = BlockUtils.getAllInBoxStream(class_2338.method_49638((class_2374)eyesVec), this.range.getValueCeil()).filter(pos -> eyesVec.method_1025(class_243.method_24953((class_2382)pos)) <= rangeSq).filter(pos -> BlockUtils.getBlock(pos) == class_2246.field_16330);
        this.jobSite = stream.min(Comparator.comparingDouble(pos -> this.villager.method_5707(class_243.method_24953((class_2382)pos)))).orElse(null);
        if (this.jobSite == null) {
            ChatUtils.error("Couldn't find the librarian's lectern.");
            ChatUtils.message("Make sure both the librarian and the lectern are reachable from where you are standing.");
            this.setEnabled(false);
            return;
        }
        System.out.println("Found lectern at " + String.valueOf(this.jobSite));
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        int green = -1073676544;
        int red = -1057030144;
        if (this.villager != null) {
            RenderUtils.drawOutlinedBox(matrixStack, this.villager.method_5829(), green, false);
        }
        if (this.jobSite != null) {
            RenderUtils.drawOutlinedBox(matrixStack, new class_238(this.jobSite), green, false);
        }
        List<class_238> expVilBoxes = this.experiencedVillagers.stream().map(class_1297::method_5829).toList();
        RenderUtils.drawOutlinedBoxes(matrixStack, expVilBoxes, red, false);
        RenderUtils.drawCrossBoxes(matrixStack, expVilBoxes, red, false);
        if (this.breakingJobSite) {
            this.overlay.render(matrixStack, partialTicks, this.jobSite);
        }
    }
}
