package net.wurstclient.hacks.autofish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_1536;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.autofish.FishingSpot;
import net.wurstclient.hacks.autofish.PositionAndRotation;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.Rotation;
import net.wurstclient.util.RotationUtils;

public final class FishingSpotManager {
    private static final class_310 MC = WurstClient.MC;
    private final CheckboxSetting mcmmoMode = new CheckboxSetting("mcMMO mode", "If enabled, AutoFish will cycle between two different fishing spots to bypass mcMMO's overfishing mechanic.\n\nAll other mcMMO settings will do nothing if this is disabled.", false);
    private final SliderSetting mcmmoRange = new SliderSetting("mcMMO range", "The value of mcMMO's MoveRange config option. This is the minimum distance between two fishing spots needed to avoid overfishing.\n\nmcMMO only cares about the position of the bobber, so you don't need to move your character unless some other anti-AFK plugin is present.", 3.0, 1.0, 50.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withSuffix(" blocks"));
    private final CheckboxSetting mcmmoRangeBug = new CheckboxSetting("mcMMO range bug", "At the time of writing, there is a bug in mcMMO's range calculation, meaning the default range of 3 blocks is actually just 2 blocks.\n\nUncheck this box if they ever fix it.", true);
    private final SliderSetting mcmmoLimit = new SliderSetting("mcMMO limit", "The value of mcMMO's OverFishLimit config option. Overfishing starts at this value, so you can actually only catch (limit - 1) fish from the same spot.", 10.0, 2.0, 1000.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final ArrayList<FishingSpot> fishingSpots = new ArrayList();
    private FishingSpot lastSpot;
    private FishingSpot nextSpot;
    private PositionAndRotation castPosRot;
    private int fishCaughtAtLastSpot;
    private boolean spot1MsgShown;
    private boolean spot2MsgShown;
    private boolean setupDoneMsgShown;

    public boolean onCast() {
        this.castPosRot = new PositionAndRotation((class_1297)FishingSpotManager.MC.field_1724);
        if (!this.mcmmoMode.isChecked()) {
            return true;
        }
        if (this.lastSpot == null) {
            if (this.spot1MsgShown) {
                return true;
            }
            ChatUtils.message("Starting AutoFish mcMMO mode.");
            ChatUtils.message("Please wait while the first fishing spot is being recorded.");
            this.spot1MsgShown = true;
            return true;
        }
        this.spot1MsgShown = false;
        if (this.nextSpot == null && (this.nextSpot = this.chooseNextSpot()) == null) {
            if (this.spot2MsgShown) {
                return false;
            }
            ChatUtils.message("AutoFish mcMMO mode requires another fishing spot.");
            ChatUtils.message("Move your camera (or the player, if necessary) so that the bobber will land outside of the red box, then cast the rod.");
            this.spot2MsgShown = true;
            this.setupDoneMsgShown = false;
            return false;
        }
        this.spot2MsgShown = false;
        if (!this.setupDoneMsgShown) {
            ChatUtils.message("All done! AutoFish will now run automatically and switch between the fishing spots as needed.");
            this.setupDoneMsgShown = true;
        }
        if (this.fishCaughtAtLastSpot >= this.mcmmoLimit.getValueI() - 1) {
            this.moveToNextSpot();
            return false;
        }
        return true;
    }

    private void moveToNextSpot() {
        IKeyMapping forwardKey = IKeyMapping.get(FishingSpotManager.MC.field_1690.field_1894);
        IKeyMapping jumpKey = IKeyMapping.get(FishingSpotManager.MC.field_1690.field_1903);
        PositionAndRotation nextPosRot = this.nextSpot.input();
        forwardKey.resetPressedState();
        jumpKey.resetPressedState();
        class_243 nextPos = nextPosRot.pos();
        double distance = nextPos.method_1022(this.castPosRot.pos());
        if (distance > 0.1) {
            Rotation needed = RotationUtils.getNeededRotations(nextPos).withPitch(0.0f);
            if (!RotationUtils.isAlreadyFacing(needed)) {
                RotationUtils.slowlyTurnTowards(needed, 5.0f).applyToClientPlayer();
                return;
            }
            jumpKey.method_23481(FishingSpotManager.MC.field_1724.method_5799() || FishingSpotManager.MC.field_1724.field_5976);
            if (distance < 0.2) {
                FishingSpotManager.MC.field_1724.method_5814(nextPos.field_1352, nextPos.field_1351, nextPos.field_1350);
            } else if (distance > 0.7 || FishingSpotManager.MC.field_1724.field_6012 % 10 == 0) {
                forwardKey.method_23481(true);
            }
            return;
        }
        Rotation nextRot = nextPosRot.rotation();
        if (!RotationUtils.isAlreadyFacing(nextRot)) {
            RotationUtils.slowlyTurnTowards(nextRot, 5.0f).applyToClientPlayer();
            return;
        }
        this.lastSpot = this.nextSpot;
        this.nextSpot = null;
        this.fishCaughtAtLastSpot = 0;
    }

    public void onBite(class_1536 bobber) {
        boolean sameBobberPos;
        boolean samePlayerInput = this.lastSpot != null && this.lastSpot.input().isNearlyIdenticalTo(this.castPosRot);
        boolean bl = sameBobberPos = this.lastSpot != null && this.isInRange(this.lastSpot.bobberPos(), bobber.method_73189());
        this.fishCaughtAtLastSpot = sameBobberPos ? ++this.fishCaughtAtLastSpot : 1;
        if (!samePlayerInput) {
            this.lastSpot = new FishingSpot(this.castPosRot, bobber);
            this.fishingSpots.add(this.lastSpot);
            return;
        }
        if (!sameBobberPos) {
            FishingSpot updatedSpot = new FishingSpot(this.lastSpot.input(), bobber);
            this.fishingSpots.remove(this.lastSpot);
            this.fishingSpots.add(updatedSpot);
            this.lastSpot = updatedSpot;
        }
    }

    public void reset() {
        this.fishingSpots.clear();
        this.lastSpot = null;
        this.nextSpot = null;
        this.castPosRot = null;
        this.fishCaughtAtLastSpot = 0;
        this.spot1MsgShown = false;
        this.spot2MsgShown = false;
        this.setupDoneMsgShown = false;
    }

    private FishingSpot chooseNextSpot() {
        return this.fishingSpots.stream().filter(spot -> spot != this.lastSpot).filter(spot -> !this.isInRange(spot.bobberPos(), this.lastSpot.bobberPos())).min(Comparator.comparingDouble(spot -> spot.input().differenceTo(this.lastSpot.input()))).orElse(null);
    }

    private boolean isInRange(class_243 pos1, class_243 pos2) {
        double dz;
        double dy = Math.abs(pos1.field_1351 - pos2.field_1351);
        if (dy > 2.0) {
            return false;
        }
        double dx = Math.abs(pos1.field_1352 - pos2.field_1352);
        return Math.max(dx, dz = Math.abs(pos1.field_1350 - pos2.field_1350)) <= (double)this.getRange();
    }

    public int getRange() {
        if (this.mcmmoRangeBug.isChecked()) {
            return this.mcmmoRange.getValueI() / 2 * 2;
        }
        return this.mcmmoRange.getValueI();
    }

    public FishingSpot getLastSpot() {
        return this.lastSpot;
    }

    public boolean isSetupDone() {
        return this.lastSpot != null && this.nextSpot != null;
    }

    public boolean isMcmmoMode() {
        return this.mcmmoMode.isChecked();
    }

    public Stream<Setting> getSettings() {
        return Stream.of(this.mcmmoMode, this.mcmmoRange, this.mcmmoRangeBug, this.mcmmoLimit);
    }

    public List<FishingSpot> getFishingSpots() {
        return Collections.unmodifiableList(this.fishingSpots);
    }
}
