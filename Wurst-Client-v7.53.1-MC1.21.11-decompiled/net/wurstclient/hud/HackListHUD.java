package net.wurstclient.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.wurstclient.WurstClient;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.other_features.HackListOtf;

public final class HackListHUD
implements UpdateListener {
    private final ArrayList<HackListEntry> activeHax = new ArrayList();
    private final HackListOtf otf;
    private int posY;
    private int textColor;

    public HackListHUD() {
        this.otf = WurstClient.INSTANCE.getOtfs().hackListOtf;
        WurstClient.INSTANCE.getEventManager().add(UpdateListener.class, this);
    }

    public void render(class_332 context, float partialTicks) {
        if (this.otf.getMode() == HackListOtf.Mode.HIDDEN) {
            return;
        }
        this.posY = this.otf.getPosition() == HackListOtf.Position.LEFT && WurstClient.INSTANCE.getOtfs().wurstLogoOtf.isVisible() ? 22 : 2;
        if (WurstClient.INSTANCE.getHax().rainbowUiHack.isEnabled()) {
            float[] acColor = WurstClient.INSTANCE.getGui().getAcColor();
            this.textColor = 0x4000000 | (int)(acColor[0] * 255.0f) << 16 | (int)(acColor[1] * 255.0f) << 8 | (int)(acColor[2] * 255.0f);
        } else {
            this.textColor = this.otf.getColor(4);
        }
        int height = this.posY + this.activeHax.size() * 9;
        if (this.otf.getMode() == HackListOtf.Mode.COUNT || height > context.method_51443()) {
            this.drawCounter(context);
        } else {
            this.drawHackList(context, partialTicks);
        }
    }

    private void drawCounter(class_332 context) {
        long size;
        String s = size + " hack" + ((size = this.activeHax.stream().filter(e -> e.hack.isEnabled()).count()) != 1L ? "s" : "") + " active";
        this.drawString(context, s);
    }

    private void drawHackList(class_332 context, float partialTicks) {
        if (this.otf.isAnimations()) {
            for (HackListEntry e : this.activeHax) {
                this.drawWithOffset(context, e, partialTicks);
            }
        } else {
            for (HackListEntry e : this.activeHax) {
                this.drawString(context, e.hack.getRenderName());
            }
        }
    }

    public void updateState(Hack hack) {
        int offset = this.otf.isAnimations() ? 4 : 0;
        HackListEntry entry = new HackListEntry(hack, offset);
        if (hack.isEnabled()) {
            if (this.activeHax.contains(entry)) {
                return;
            }
            this.activeHax.add(entry);
            this.sort();
        } else if (!this.otf.isAnimations()) {
            this.activeHax.remove(entry);
        }
    }

    private void sort() {
        Comparator<HackListEntry> comparator = Comparator.comparing(hle -> hle.hack, this.otf.getComparator());
        Collections.sort(this.activeHax, comparator);
    }

    @Override
    public void onUpdate() {
        if (this.otf.shouldSort()) {
            this.sort();
        }
        if (!this.otf.isAnimations()) {
            return;
        }
        Iterator<HackListEntry> itr = this.activeHax.iterator();
        while (itr.hasNext()) {
            HackListEntry e = itr.next();
            boolean enabled = e.hack.isEnabled();
            e.prevOffset = e.offset;
            if (enabled && e.offset > 0) {
                --e.offset;
                continue;
            }
            if (!enabled && e.offset < 4) {
                ++e.offset;
                continue;
            }
            if (enabled || e.offset < 4) continue;
            itr.remove();
        }
    }

    private void drawString(class_332 context, String s) {
        int posX;
        class_327 tr = WurstClient.MC.field_1772;
        if (this.otf.getPosition() == HackListOtf.Position.LEFT) {
            posX = 2;
        } else {
            int screenWidth = context.method_51421();
            int stringWidth = tr.method_1727(s);
            posX = screenWidth - stringWidth - 2;
        }
        context.method_51433(tr, s, posX + 1, this.posY + 1, -16777216, false);
        context.field_59826.method_71067();
        context.method_51433(tr, s, posX, this.posY, this.textColor | 0xFF000000, false);
        this.posY += 9;
    }

    private void drawWithOffset(class_332 context, HackListEntry e, float partialTicks) {
        float posX;
        class_327 tr = WurstClient.MC.field_1772;
        String s = e.hack.getRenderName();
        float offset = (float)e.offset * partialTicks + (float)e.prevOffset * (1.0f - partialTicks);
        if (this.otf.getPosition() == HackListOtf.Position.LEFT) {
            posX = 2.0f - 5.0f * offset;
        } else {
            int screenWidth = context.method_51421();
            int stringWidth = tr.method_1727(s);
            posX = (float)(screenWidth - stringWidth - 2) + 5.0f * offset;
        }
        int alpha = (int)(255.0f * (1.0f - offset / 4.0f)) << 24;
        context.method_51433(tr, s, (int)posX + 1, this.posY + 1, 0x4000000 | alpha, false);
        context.field_59826.method_71067();
        context.method_51433(tr, s, (int)posX, this.posY, this.textColor | alpha, false);
        this.posY += 9;
    }

    private static final class HackListEntry {
        private final Hack hack;
        private int offset;
        private int prevOffset;

        public HackListEntry(Hack mod, int offset) {
            this.hack = mod;
            this.offset = offset;
            this.prevOffset = offset;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof HackListEntry)) {
                return false;
            }
            HackListEntry other = (HackListEntry)obj;
            return this.hack == other.hack;
        }

        public int hashCode() {
            return this.hack.hashCode();
        }
    }
}
