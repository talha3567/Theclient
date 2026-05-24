package net.wurstclient.hacks;

import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_3966;
import net.minecraft.class_465;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.HandleInputListener;
import net.wurstclient.events.PreMotionListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.util.EntityUtils;

@SearchTags(value={"trigger bot", "AutoAttack", "auto attack", "AutoClicker", "auto clicker"})
public final class TriggerBotHack
extends Hack
implements PreMotionListener,
HandleInputListener {
    private final SliderSetting range = new SliderSetting("Range", 4.25, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final AttackSpeedSliderSetting speed = new AttackSpeedSliderSetting();
    private final SliderSetting speedRandMS = new SliderSetting("Speed randomization", "Helps you bypass anti-cheat plugins by varying the delay between attacks.\n\n\u00b1100ms is recommended for Vulcan.\n\n0 (off) is fine for NoCheat+, AAC, Grim, Verus, Spartan, and vanilla servers.", 100.0, 0.0, 1000.0, 50.0, SliderSetting.ValueDisplay.INTEGER.withPrefix("\u00b1").withSuffix("ms").withLabel(0.0, "off"));
    private final SwingHandSetting swingHand = new SwingHandSetting(this, SwingHandSetting.SwingHand.CLIENT);
    private final CheckboxSetting attackWhileBlocking = new CheckboxSetting("Attack while blocking", "Attacks even while you're blocking with a shield or using items.\n\nThis would not be possible in vanilla and won't work if \"Simulate mouse click\" is enabled.", false);
    private final CheckboxSetting simulateMouseClick = new CheckboxSetting("Simulate mouse click", "Simulates an actual mouse click (or key press) when attacking. Can be used to trick CPS measuring tools into thinking that you're attacking manually.\n\n\u00a7c\u00a7lWARNING:\u00a7r Simulating mouse clicks can lead to unexpected behavior, like in-game menus clicking themselves. Also, the \"Swing hand\" and \"Attack while blocking\" settings will not work while this option is enabled.", false);
    private final EntityFilterList entityFilters = EntityFilterList.genericCombat();
    private boolean simulatingMouseClick;

    public TriggerBotHack() {
        super("TriggerBot");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.range);
        this.addSetting(this.speed);
        this.addSetting(this.speedRandMS);
        this.addSetting(this.swingHand);
        this.addSetting(this.attackWhileBlocking);
        this.addSetting(this.simulateMouseClick);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        TriggerBotHack.WURST.getHax().clickAuraHack.setEnabled(false);
        TriggerBotHack.WURST.getHax().crystalAuraHack.setEnabled(false);
        TriggerBotHack.WURST.getHax().fightBotHack.setEnabled(false);
        TriggerBotHack.WURST.getHax().killauraLegitHack.setEnabled(false);
        TriggerBotHack.WURST.getHax().killauraHack.setEnabled(false);
        TriggerBotHack.WURST.getHax().multiAuraHack.setEnabled(false);
        TriggerBotHack.WURST.getHax().protectHack.setEnabled(false);
        TriggerBotHack.WURST.getHax().tpAuraHack.setEnabled(false);
        this.speed.resetTimer(this.speedRandMS.getValue());
        EVENTS.add(PreMotionListener.class, this);
        EVENTS.add(HandleInputListener.class, this);
    }

    @Override
    protected void onDisable() {
        if (this.simulatingMouseClick) {
            IKeyMapping.get(TriggerBotHack.MC.field_1690.field_1886).simulatePress(false);
            this.simulatingMouseClick = false;
        }
        EVENTS.remove(PreMotionListener.class, this);
        EVENTS.remove(HandleInputListener.class, this);
    }

    @Override
    public void onPreMotion() {
        if (!this.simulatingMouseClick) {
            return;
        }
        IKeyMapping.get(TriggerBotHack.MC.field_1690.field_1886).simulatePress(false);
        this.simulatingMouseClick = false;
    }

    @Override
    public void onHandleInput() {
        class_239 class_2392;
        this.speed.updateTimer();
        if (!this.speed.isTimeToAttack()) {
            return;
        }
        if (TriggerBotHack.MC.field_1755 instanceof class_465) {
            return;
        }
        class_746 player = TriggerBotHack.MC.field_1724;
        if (!this.attackWhileBlocking.isChecked() && player.method_6115()) {
            return;
        }
        if (TriggerBotHack.MC.field_1765 == null || !((class_2392 = TriggerBotHack.MC.field_1765) instanceof class_3966)) {
            return;
        }
        class_3966 eResult = (class_3966)class_2392;
        class_1297 target = eResult.method_17782();
        if (!this.isCorrectEntity(target)) {
            return;
        }
        TriggerBotHack.WURST.getHax().autoSwordHack.setSlot(target);
        if (this.simulateMouseClick.isChecked()) {
            IKeyMapping.get(TriggerBotHack.MC.field_1690.field_1886).simulatePress(true);
            this.simulatingMouseClick = true;
        } else {
            TriggerBotHack.MC.field_1761.method_2918((class_1657)player, target);
            this.swingHand.swing(class_1268.field_5808);
        }
        this.speed.resetTimer(this.speedRandMS.getValue());
    }

    private boolean isCorrectEntity(class_1297 entity) {
        if (!EntityUtils.IS_ATTACKABLE.test(entity)) {
            return false;
        }
        if (EntityUtils.distanceToHitboxSq(entity) > this.range.getValueSq()) {
            return false;
        }
        return this.entityFilters.testOne(entity);
    }
}
