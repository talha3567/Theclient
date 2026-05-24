package net.wurstclient.hacks;

import java.util.SequencedCollection;
import java.util.stream.IntStream;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_465;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"auto steal", "ChestStealer", "chest stealer", "steal store buttons", "Steal/Store buttons"})
public final class AutoStealHack
extends Hack {
    private final SliderSetting delay = new SliderSetting("Delay", "Delay between moving stacks of items.\nShould be at least 70ms for NoCheat+ servers.", 100.0, 0.0, 500.0, 10.0, SliderSetting.ValueDisplay.INTEGER.withSuffix("ms"));
    private final CheckboxSetting buttons = new CheckboxSetting("Steal/Store buttons", true);
    private final CheckboxSetting reverseSteal = new CheckboxSetting("Reverse steal order", false);
    private Thread thread;

    public AutoStealHack() {
        super("AutoSteal");
        this.setCategory(Category.ITEMS);
        this.addSetting(this.buttons);
        this.addSetting(this.delay);
        this.addSetting(this.reverseSteal);
    }

    public void steal(class_465<?> screen, int rows) {
        this.startClickingSlots(screen, 0, rows * 9, true);
    }

    public void store(class_465<?> screen, int rows) {
        this.startClickingSlots(screen, rows * 9, rows * 9 + 36, false);
    }

    private void startClickingSlots(class_465<?> screen, int from, int to, boolean steal) {
        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();
        }
        this.thread = Thread.ofPlatform().name("AutoSteal").uncaughtExceptionHandler((t, e) -> e.printStackTrace()).daemon().start(() -> this.shiftClickSlots(screen, from, to, steal));
    }

    private void shiftClickSlots(class_465<?> screen, int from, int to, boolean steal) {
        SequencedCollection<Object> slots = IntStream.range(from, to).mapToObj(i -> (class_1735)screen.method_17577().field_7761.get(i)).toList();
        if (this.reverseSteal.isChecked() && steal) {
            slots = slots.reversed();
        }
        for (class_1735 slot : slots) {
            try {
                if (slot.method_7677().method_7960()) continue;
                Thread.sleep(this.delay.getValueI());
                if (AutoStealHack.MC.field_1755 == null) break;
                screen.method_2383(slot, slot.field_7874, 0, class_1713.field_7794);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public boolean areButtonsVisible() {
        return this.buttons.isChecked();
    }
}
