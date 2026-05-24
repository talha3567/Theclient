package net.wurstclient.commands;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.wurstclient.Category;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.util.RenderUtils;

public final class TacoCmd
extends Command
implements GUIRenderListener,
UpdateListener {
    private final class_2960[] tacos = new class_2960[]{class_2960.method_60655((String)"wurst", (String)"dancingtaco1.png"), class_2960.method_60655((String)"wurst", (String)"dancingtaco2.png"), class_2960.method_60655((String)"wurst", (String)"dancingtaco3.png"), class_2960.method_60655((String)"wurst", (String)"dancingtaco4.png")};
    private boolean enabled;
    private int ticks = 0;

    public TacoCmd() {
        super("taco", "Spawns a dancing taco on your hotbar.\n\"I love that little guy. So cute!\" -WiZARD", new String[0]);
        this.setCategory(Category.FUN);
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length != 0) {
            throw new CmdSyntaxError("Tacos don't need arguments!");
        }
        boolean bl = this.enabled = !this.enabled;
        if (this.enabled) {
            EVENTS.add(GUIRenderListener.class, this);
            EVENTS.add(UpdateListener.class, this);
        } else {
            EVENTS.remove(GUIRenderListener.class, this);
            EVENTS.remove(UpdateListener.class, this);
        }
    }

    @Override
    public String getPrimaryAction() {
        return "Be a BOSS!";
    }

    @Override
    public void doPrimaryAction() {
        WURST.getCmdProcessor().process("taco");
    }

    @Override
    public void onUpdate() {
        this.ticks = this.ticks >= 31 ? 0 : ++this.ticks;
    }

    @Override
    public void onRenderGUI(class_332 context, float partialTicks) {
        int color = TacoCmd.WURST.getHax().rainbowUiHack.isEnabled() ? RenderUtils.toIntColor(WURST.getGui().getAcColor(), 1.0f) : -1;
        int x = context.method_51421() / 2 - 32 + 76;
        int y = context.method_51443() - 32 - 19;
        int w = 64;
        int h = 32;
        context.method_25291(class_10799.field_56883, this.tacos[this.ticks / 8], x, y, 0.0f, 0.0f, w, h, w, h, color);
    }
}
