package net.wurstclient.commands;

import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.UpdateListener;

public final class DropCmd
extends Command
implements UpdateListener {
    private int slowModeTimer;
    private int slowModeSlotCounter;

    public DropCmd() {
        super("drop", "Drops all your items on the ground.", ".drop", "Slow mode: .drop slow", "If regular .drop kicks you from the server,", "use slow mode instead.");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length > 1) {
            throw new CmdSyntaxError();
        }
        if (args.length < 1) {
            this.dropAllItems();
            return;
        }
        if (!args[0].equalsIgnoreCase("slow")) {
            throw new CmdSyntaxError();
        }
        this.slowModeTimer = 5;
        this.slowModeSlotCounter = 9;
        EVENTS.add(UpdateListener.class, this);
    }

    private void dropAllItems() {
        for (int i = 9; i < 45; ++i) {
            IMC.getInteractionManager().windowClick_THROW(i);
        }
    }

    @Override
    public void onUpdate() {
        --this.slowModeTimer;
        if (this.slowModeTimer > 0) {
            return;
        }
        this.skipEmptySlots();
        IMC.getInteractionManager().windowClick_THROW(this.slowModeSlotCounter);
        ++this.slowModeSlotCounter;
        this.slowModeTimer = 5;
        if (this.slowModeSlotCounter >= 45) {
            EVENTS.remove(UpdateListener.class, this);
        }
    }

    private void skipEmptySlots() {
        while (this.slowModeSlotCounter < 45) {
            int adjustedSlot = this.slowModeSlotCounter;
            if (adjustedSlot >= 36) {
                adjustedSlot -= 36;
            }
            if (!DropCmd.MC.field_1724.method_31548().method_5438(adjustedSlot).method_7960()) break;
            ++this.slowModeSlotCounter;
        }
    }
}
