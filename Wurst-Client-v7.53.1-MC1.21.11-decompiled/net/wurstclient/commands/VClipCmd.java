package net.wurstclient.commands;

import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_746;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.MathUtils;

public final class VClipCmd
extends Command {
    public VClipCmd() {
        super("vclip", "Lets you clip through blocks vertically.\nThe maximum distance is 10 blocks.", ".vclip <height>", ".vclip (up|down)");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length != 1) {
            throw new CmdSyntaxError();
        }
        if (MathUtils.isDouble(args[0])) {
            this.vclip(Double.parseDouble(args[0]));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "up": {
                this.vclip(this.calculateHeight(class_2350.field_11036));
                break;
            }
            case "down": {
                this.vclip(this.calculateHeight(class_2350.field_11033));
                break;
            }
            default: {
                throw new CmdSyntaxError();
            }
        }
    }

    private double calculateHeight(class_2350 direction) throws CmdError {
        class_238 maxOffsetBox;
        class_238 box = VClipCmd.MC.field_1724.method_5829();
        if (!this.hasCollisions(box.method_991(maxOffsetBox = box.method_989(0.0, (double)(direction.method_10164() * 10), 0.0)))) {
            throw new CmdError("There is nothing to clip through!");
        }
        for (int i = 1; i <= 10; ++i) {
            double height = direction.method_10164() * i;
            class_238 offsetBox = box.method_989(0.0, height, 0.0);
            if (this.hasCollisions(offsetBox)) {
                class_238 newOffsetBox;
                double subBlockOffset = this.getSubBlockOffset(offsetBox);
                if (subBlockOffset >= 1.0 || height + subBlockOffset > 10.0 || this.hasCollisions(newOffsetBox = offsetBox.method_989(0.0, subBlockOffset, 0.0))) continue;
                height += subBlockOffset;
                offsetBox = newOffsetBox;
            }
            if (!this.hasCollisions(box.method_991(offsetBox))) continue;
            return height;
        }
        throw new CmdError("There are no free blocks where you can fit!");
    }

    private boolean hasCollisions(class_238 box) {
        return BlockUtils.getBlockCollisions(box).findAny().isPresent();
    }

    private double getSubBlockOffset(class_238 offsetBox) {
        return BlockUtils.getBlockCollisions(offsetBox).mapToDouble(box -> box.field_1325).max().getAsDouble() - offsetBox.field_1322;
    }

    private void vclip(double height) {
        class_746 p = VClipCmd.MC.field_1724;
        p.method_5814(p.method_23317(), p.method_23318() + height, p.method_23321());
    }
}
