package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_1536;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2739;
import net.minecraft.class_2767;
import net.minecraft.class_3417;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.autofish.AutoFishDebugDraw;
import net.wurstclient.hacks.autofish.AutoFishRodSelector;
import net.wurstclient.hacks.autofish.FishingSpotManager;
import net.wurstclient.hacks.autofish.ShallowWaterWarningCheckbox;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"AutoFishing", "auto fishing", "AutoFisher", "auto fisher", "AFKFishBot", "afk fish bot", "AFKFishingBot", "afk fishing bot", "AFKFisherBot", "afk fisher bot"})
public final class AutoFishHack
extends Hack
implements UpdateListener,
PacketInputListener,
RenderListener {
    private final EnumSetting<BiteMode> biteMode = new EnumSetting("Bite mode", "\u00a7lSound\u00a7r mode detects bites by listening for the bite sound. This method is less accurate, but is more resilient against anti-cheats. See the \"Valid range\" setting.\n\n\u00a7lEntity\u00a7r mode detects bites by checking for the fishing hook's entity update packet. It's more accurate than the sound method, but is less resilient against anti-cheats.", (Enum[])BiteMode.values(), (Enum)BiteMode.SOUND);
    private final SliderSetting validRange = new SliderSetting("Valid range", "Any bites that occur outside of this range will be ignored.\n\nIncrease your range if bites are not being detected, decrease it if other people's bites are being detected as yours.\n\nThis setting has no effect when \"Bite mode\" is set to \"Entity\".", 1.5, 0.25, 8.0, 0.25, SliderSetting.ValueDisplay.DECIMAL);
    private final SliderSetting catchDelay = new SliderSetting("Catch delay", "How long AutoFish will wait after a bite before reeling in.", 0.0, 0.0, 60.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withSuffix(" ticks").withLabel(1.0, "1 tick"));
    private final SliderSetting retryDelay = new SliderSetting("Retry delay", "If casting or reeling in the fishing rod fails, this is how long AutoFish will wait before trying again.", 15.0, 0.0, 100.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withSuffix(" ticks").withLabel(1.0, "1 tick"));
    private final SliderSetting patience = new SliderSetting("Patience", "How long AutoFish will wait if it doesn't get a bite before reeling in.", 60.0, 10.0, 120.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withSuffix("s"));
    private final ShallowWaterWarningCheckbox shallowWaterWarning = new ShallowWaterWarningCheckbox();
    private final FishingSpotManager fishingSpots = new FishingSpotManager();
    private final AutoFishDebugDraw debugDraw = new AutoFishDebugDraw(this.validRange, this.fishingSpots);
    private final AutoFishRodSelector rodSelector = new AutoFishRodSelector(this);
    private int castRodTimer;
    private int reelInTimer;
    private boolean biteDetected;

    public AutoFishHack() {
        super("AutoFish");
        this.setCategory(Category.OTHER);
        this.addSetting(this.biteMode);
        this.addSetting(this.validRange);
        this.addSetting(this.catchDelay);
        this.addSetting(this.retryDelay);
        this.addSetting(this.patience);
        this.debugDraw.getSettings().forEach(x$0 -> this.addSetting((Setting)x$0));
        this.rodSelector.getSettings().forEach(x$0 -> this.addSetting((Setting)x$0));
        this.addSetting(this.shallowWaterWarning);
        this.fishingSpots.getSettings().forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    public String getRenderName() {
        if (this.rodSelector.isOutOfRods()) {
            return this.getName() + " [out of rods]";
        }
        return this.getName();
    }

    @Override
    protected void onEnable() {
        this.castRodTimer = 0;
        this.reelInTimer = 0;
        this.biteDetected = false;
        this.rodSelector.reset();
        this.debugDraw.reset();
        this.fishingSpots.reset();
        this.shallowWaterWarning.reset();
        AutoFishHack.WURST.getHax().antiAfkHack.setEnabled(false);
        AutoFishHack.WURST.getHax().aimAssistHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PacketInputListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PacketInputListener.class, this);
        EVENTS.remove(RenderListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (this.castRodTimer > 0) {
            --this.castRodTimer;
        }
        if (this.reelInTimer > 0) {
            --this.reelInTimer;
        }
        if (!this.rodSelector.update()) {
            return;
        }
        if (!this.isFishing()) {
            if (this.castRodTimer > 0) {
                return;
            }
            this.reelInTimer = 20 * this.patience.getValueI();
            if (!this.fishingSpots.onCast()) {
                return;
            }
            MC.method_1583();
            this.castRodTimer = this.retryDelay.getValueI();
            return;
        }
        if (this.biteDetected) {
            this.shallowWaterWarning.checkWaterType();
            this.reelInTimer = this.catchDelay.getValueI();
            this.fishingSpots.onBite(AutoFishHack.MC.field_1724.field_7513);
            this.biteDetected = false;
        } else if (AutoFishHack.MC.field_1724.field_7513.method_26957() != null) {
            this.reelInTimer = this.catchDelay.getValueI();
        }
        if (this.reelInTimer == 0) {
            MC.method_1583();
            this.reelInTimer = this.retryDelay.getValueI();
            this.castRodTimer = this.retryDelay.getValueI();
        }
    }

    @Override
    public void onReceivedPacket(PacketInputListener.PacketInputEvent event) {
        switch (this.biteMode.getSelected().ordinal()) {
            case 0: {
                this.processSoundUpdate(event);
                break;
            }
            case 1: {
                this.processEntityUpdate(event);
            }
        }
    }

    private void processSoundUpdate(PacketInputListener.PacketInputEvent event) {
        class_2596<?> class_25962 = event.getPacket();
        if (!(class_25962 instanceof class_2767)) {
            return;
        }
        class_2767 sound = (class_2767)class_25962;
        if (!class_3417.field_14660.equals(sound.method_11894().comp_349())) {
            return;
        }
        if (!this.isFishing()) {
            return;
        }
        this.debugDraw.updateSoundPos(sound);
        class_243 bobber = AutoFishHack.MC.field_1724.field_7513.method_73189();
        double dx = Math.abs(sound.method_11890() - bobber.method_10216());
        double dz = Math.abs(sound.method_11893() - bobber.method_10215());
        if (Math.max(dx, dz) > this.validRange.getValue()) {
            return;
        }
        this.biteDetected = true;
    }

    private void processEntityUpdate(PacketInputListener.PacketInputEvent event) {
        class_2596<?> class_25962 = event.getPacket();
        if (!(class_25962 instanceof class_2739)) {
            return;
        }
        class_2739 update = (class_2739)class_25962;
        class_1297 class_12972 = AutoFishHack.MC.field_1687.method_8469(update.comp_1127());
        if (!(class_12972 instanceof class_1536)) {
            return;
        }
        class_1536 bobber = (class_1536)class_12972;
        if (bobber != AutoFishHack.MC.field_1724.field_7513) {
            return;
        }
        if (!this.isFishing()) {
            return;
        }
        this.biteDetected = true;
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        this.debugDraw.render(matrixStack, partialTicks);
    }

    private boolean isFishing() {
        class_746 player = AutoFishHack.MC.field_1724;
        return player != null && player.field_7513 != null && !player.field_7513.method_31481() && player.method_6047().method_31574(class_1802.field_8378);
    }

    private static enum BiteMode {
        SOUND("Sound"),
        ENTITY("Entity");

        private final String name;

        private BiteMode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
