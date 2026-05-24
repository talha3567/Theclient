package net.wurstclient.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.StreamSupport;
import net.minecraft.class_1309;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_4587;
import net.wurstclient.ai.PathFinder;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.MathUtils;

public final class PathCmd
extends Command
implements UpdateListener,
RenderListener {
    private final CheckboxSetting debugMode = new CheckboxSetting("Debug mode", false);
    private final CheckboxSetting depthTest = new CheckboxSetting("Depth test", false);
    private PathFinder pathFinder;
    private boolean enabled;
    private long startTime;
    private class_2338 lastGoal;

    public PathCmd() {
        super("path", "Shows the shortest path to a specific point.\nUseful for labyrinths and caves.", ".path <x> <y> <z>", ".path <entity>", ".path -debug", ".path -depth", ".path -refresh", "Turn off: .path");
        this.addSetting(this.debugMode);
        this.addSetting(this.depthTest);
    }

    @Override
    public void call(String[] args) throws CmdException {
        class_2338 goal;
        boolean refresh = false;
        if (args.length > 0 && args[0].startsWith("-")) {
            switch (args[0]) {
                case "-debug": {
                    this.debugMode.setChecked(!this.debugMode.isChecked());
                    ChatUtils.message("Debug mode " + (this.debugMode.isChecked() ? "on" : "off") + ".");
                    return;
                }
                case "-depth": {
                    this.depthTest.setChecked(!this.depthTest.isChecked());
                    ChatUtils.message("Depth test " + (this.depthTest.isChecked() ? "on" : "off") + ".");
                    return;
                }
                case "-refresh": {
                    if (this.lastGoal == null) {
                        throw new CmdError("Cannot refresh: no previous path.");
                    }
                    refresh = true;
                }
            }
        }
        if (this.enabled) {
            EVENTS.remove(UpdateListener.class, this);
            EVENTS.remove(RenderListener.class, this);
            this.enabled = false;
            if (args.length == 0) {
                return;
            }
        }
        if (refresh) {
            goal = this.lastGoal;
        } else {
            this.lastGoal = goal = this.argsToPos(args);
        }
        this.pathFinder = new PathFinder(goal);
        this.enabled = true;
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
        System.out.println("Finding path...");
        this.startTime = System.nanoTime();
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
        class_1309 entity = StreamSupport.stream(PathCmd.MC.field_1687.method_18112().spliterator(), true).filter(class_1309.class::isInstance).map(e -> (class_1309)e).filter(e -> !e.method_31481() && e.method_6032() > 0.0f).filter(e -> e != PathCmd.MC.field_1724).filter(e -> !(e instanceof FakePlayerEntity)).filter(e -> name.equalsIgnoreCase(e.method_5476().getString())).min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
        if (entity == null) {
            throw new CmdError("Entity \"" + name + "\" could not be found.");
        }
        return class_2338.method_49638((class_2374)entity.method_73189());
    }

    private class_2338 argsToXyzPos(String ... xyz) throws CmdSyntaxError {
        class_2338 playerPos = class_2338.method_49638((class_2374)PathCmd.MC.field_1724.method_73189());
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
        double passedTime = (double)(System.nanoTime() - this.startTime) / 1000000.0;
        this.pathFinder.think();
        boolean foundPath = this.pathFinder.isDone();
        if (foundPath || this.pathFinder.isFailed()) {
            ArrayList<Object> path = new ArrayList();
            if (foundPath) {
                path = this.pathFinder.formatPath();
            } else {
                ChatUtils.error("Could not find a path.");
            }
            EVENTS.remove(UpdateListener.class, this);
            System.out.println("Done after " + passedTime + "ms");
            if (this.debugMode.isChecked()) {
                System.out.println("Length: " + path.size() + ", processed: " + this.pathFinder.countProcessedBlocks() + ", queue: " + this.pathFinder.getQueueSize() + ", cost: " + this.pathFinder.getCost(this.pathFinder.getCurrentPos()));
            }
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        this.pathFinder.renderPath(matrixStack, this.debugMode.isChecked(), this.depthTest.isChecked());
    }

    public class_2338 getLastGoal() {
        return this.lastGoal;
    }

    public boolean isDebugMode() {
        return this.debugMode.isChecked();
    }

    public boolean isDepthTest() {
        return this.depthTest.isChecked();
    }
}
