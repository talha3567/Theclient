package net.wurstclient.commands;

import java.util.Comparator;
import java.util.stream.StreamSupport;
import net.minecraft.class_1309;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_4587;
import net.wurstclient.ai.PathFinder;
import net.wurstclient.ai.PathProcessor;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.commands.PathCmd;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.MathUtils;

public final class GoToCmd
extends Command
implements UpdateListener,
RenderListener {
    private PathFinder pathFinder;
    private PathProcessor processor;
    private boolean enabled;

    public GoToCmd() {
        super("goto", "Walks or flies you to a specific location.", ".goto <x> <y> <z>", ".goto <entity>", ".goto -path", "Turn off: .goto");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (this.enabled) {
            this.disable();
            if (args.length == 0) {
                return;
            }
        }
        if (args.length == 1 && args[0].equals("-path")) {
            class_2338 goal = GoToCmd.WURST.getCmds().pathCmd.getLastGoal();
            if (goal == null) {
                throw new CmdError("No previous position on .path.");
            }
            this.pathFinder = new PathFinder(goal);
        } else {
            class_2338 goal = this.argsToPos(args);
            this.pathFinder = new PathFinder(goal);
        }
        this.enabled = true;
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    private class_2338 argsToPos(String ... args) throws CmdException {
        switch (args.length) {
            default: {
                throw new CmdSyntaxError("Invalid coordinates.");
            }
            case 1: {
                return this.argsToEntityPos(args[0]);
            }
            case 3: 
        }
        return this.argsToXyzPos(args);
    }

    private class_2338 argsToEntityPos(String name) throws CmdError {
        class_1309 entity = StreamSupport.stream(GoToCmd.MC.field_1687.method_18112().spliterator(), true).filter(class_1309.class::isInstance).map(e -> (class_1309)e).filter(e -> !e.method_31481() && e.method_6032() > 0.0f).filter(e -> e != GoToCmd.MC.field_1724).filter(e -> !(e instanceof FakePlayerEntity)).filter(e -> name.equalsIgnoreCase(e.method_5476().getString())).min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
        if (entity == null) {
            throw new CmdError("Entity \"" + name + "\" could not be found.");
        }
        return class_2338.method_49638((class_2374)entity.method_73189());
    }

    private class_2338 argsToXyzPos(String ... xyz) throws CmdSyntaxError {
        class_2338 playerPos = class_2338.method_49638((class_2374)GoToCmd.MC.field_1724.method_73189());
        int[] player = new int[]{playerPos.method_10263(), playerPos.method_10264(), playerPos.method_10260()};
        int[] pos = new int[3];
        for (int i = 0; i < 3; ++i) {
            if (MathUtils.isInteger(xyz[i])) {
                pos[i] = Integer.parseInt(xyz[i]);
                continue;
            }
            if (xyz[i].equals("~")) {
                pos[i] = player[i];
                continue;
            }
            if (xyz[i].startsWith("~") && MathUtils.isInteger(xyz[i].substring(1))) {
                pos[i] = player[i] + Integer.parseInt(xyz[i].substring(1));
                continue;
            }
            throw new CmdSyntaxError("Invalid coordinates.");
        }
        return new class_2338(pos[0], pos[1], pos[2]);
    }

    @Override
    public void onUpdate() {
        if (!this.pathFinder.isDone()) {
            PathProcessor.lockControls();
            this.pathFinder.think();
            if (!this.pathFinder.isDone()) {
                if (this.pathFinder.isFailed()) {
                    ChatUtils.error("Could not find a path.");
                    this.disable();
                }
                return;
            }
            this.pathFinder.formatPath();
            this.processor = this.pathFinder.getProcessor();
            System.out.println("Done");
        }
        if (this.processor != null && !this.pathFinder.isPathStillValid(this.processor.getIndex())) {
            System.out.println("Updating path...");
            this.pathFinder = new PathFinder(this.pathFinder.getGoal());
            return;
        }
        this.processor.process();
        if (this.processor.isDone()) {
            this.disable();
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        PathCmd pathCmd = GoToCmd.WURST.getCmds().pathCmd;
        this.pathFinder.renderPath(matrixStack, pathCmd.isDebugMode(), pathCmd.isDepthTest());
    }

    private void disable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.pathFinder = null;
        this.processor = null;
        PathProcessor.releaseControls();
        this.enabled = false;
    }

    public boolean isActive() {
        return this.enabled;
    }
}
