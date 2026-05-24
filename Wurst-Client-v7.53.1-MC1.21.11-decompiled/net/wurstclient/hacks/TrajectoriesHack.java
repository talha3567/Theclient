package net.wurstclient.hacks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.function.Predicate;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1306;
import net.minecraft.class_1675;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1787;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1811;
import net.minecraft.class_1823;
import net.minecraft.class_1835;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_4537;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"ArrowTrajectories", "ArrowPrediction", "aim assist", "arrow trajectories", "bow trajectories"})
public final class TrajectoriesHack
extends Hack
implements RenderListener {
    private final ColorSetting missColor = new ColorSetting("Miss Color", "Color of the trajectory when it doesn't hit anything.", Color.GRAY);
    private final ColorSetting entityHitColor = new ColorSetting("Entity Hit Color", "Color of the trajectory when it hits an entity.", Color.RED);
    private final ColorSetting blockHitColor = new ColorSetting("Block Hit Color", "Color of the trajectory when it hits a block.", Color.GREEN);

    public TrajectoriesHack() {
        super("Trajectories");
        this.setCategory(Category.RENDER);
        this.addSetting(this.missColor);
        this.addSetting(this.entityHitColor);
        this.addSetting(this.blockHitColor);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(RenderListener.class, this);
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        Trajectory trajectory = this.getTrajectory(partialTicks);
        if (trajectory.isEmpty()) {
            return;
        }
        ColorSetting color = this.getColor(trajectory);
        int lineColor = color.getColorI(192);
        int quadColor = color.getColorI(64);
        class_238 endBox = trajectory.getEndBox();
        ArrayList<class_243> path = trajectory.path();
        RenderUtils.drawSolidBox(matrixStack, endBox, quadColor, false);
        RenderUtils.drawOutlinedBox(matrixStack, endBox, lineColor, false);
        RenderUtils.drawCurvedLine(matrixStack, path, lineColor, false);
    }

    private Trajectory getTrajectory(float partialTicks) {
        class_746 player = TrajectoriesHack.MC.field_1724;
        ArrayList<class_243> path = new ArrayList<class_243>();
        class_239.class_240 type = class_239.class_240.field_1333;
        class_1268 hand = class_1268.field_5808;
        class_1799 stack = player.method_6047();
        if (!this.isThrowable(stack)) {
            hand = class_1268.field_5810;
            stack = player.method_6079();
            if (!this.isThrowable(stack)) {
                return new Trajectory(path, type);
            }
        }
        class_1792 item = stack.method_7909();
        double throwPower = this.getThrowPower(item);
        double gravity = this.getProjectileGravity(item);
        class_3959.class_242 fluidHandling = this.getFluidHandling(item);
        double yaw = Math.toRadians(player.method_36454());
        double pitch = Math.toRadians(player.method_36455());
        class_243 arrowPos = EntityUtils.getLerpedPos((class_1297)player, partialTicks).method_1019(this.getHandOffset(hand, yaw));
        class_243 arrowMotion = this.getStartingMotion(yaw, pitch, throwPower);
        for (int i = 0; i < 1000; ++i) {
            path.add(arrowPos);
            arrowPos = arrowPos.method_1019(arrowMotion.method_1021(0.1));
            arrowMotion = arrowMotion.method_1021(0.999);
            arrowMotion = arrowMotion.method_1031(0.0, -gravity * 0.1, 0.0);
            class_243 lastPos = path.size() > 1 ? path.get(path.size() - 2) : RotationUtils.getEyesPos();
            class_3965 bResult = BlockUtils.raycast(lastPos, arrowPos, fluidHandling);
            if (bResult.method_17783() != class_239.class_240.field_1333) {
                type = class_239.class_240.field_1332;
                path.set(path.size() - 1, bResult.method_17784());
                break;
            }
            class_238 box = new class_238(lastPos, arrowPos);
            Predicate<class_1297> predicate = e -> !e.method_7325() && e.method_5863();
            double maxDistSq = 4096.0;
            class_3966 eResult = class_1675.method_18075((class_1297)player, (class_243)lastPos, (class_243)arrowPos, (class_238)box, predicate, (double)maxDistSq);
            if (eResult == null || eResult.method_17783() == class_239.class_240.field_1333) continue;
            type = class_239.class_240.field_1331;
            path.set(path.size() - 1, eResult.method_17784());
            break;
        }
        return new Trajectory(path, type);
    }

    private boolean isThrowable(class_1799 stack) {
        if (stack.method_7960()) {
            return false;
        }
        class_1792 item = stack.method_7909();
        return item instanceof class_1811 || item instanceof class_1823 || item instanceof class_1771 || item instanceof class_1776 || item instanceof class_4537 || item instanceof class_1787 || item instanceof class_1835;
    }

    private double getThrowPower(class_1792 item) {
        if (!(item instanceof class_1811)) {
            return 1.5;
        }
        float bowPower = (float)(72000 - TrajectoriesHack.MC.field_1724.method_6014()) / 20.0f;
        if ((bowPower = bowPower * bowPower + bowPower * 2.0f) > 3.0f || bowPower <= 0.3f) {
            bowPower = 3.0f;
        }
        return bowPower;
    }

    private double getProjectileGravity(class_1792 item) {
        if (item instanceof class_1811) {
            return 0.05;
        }
        if (item instanceof class_4537) {
            return 0.4;
        }
        if (item instanceof class_1787) {
            return 0.15;
        }
        if (item instanceof class_1835) {
            return 0.015;
        }
        return 0.03;
    }

    private class_3959.class_242 getFluidHandling(class_1792 item) {
        if (item instanceof class_1787) {
            return class_3959.class_242.field_1347;
        }
        return class_3959.class_242.field_1348;
    }

    private class_243 getHandOffset(class_1268 hand, double yaw) {
        class_1306 mainArm = (class_1306)TrajectoriesHack.MC.field_1690.method_42552().method_41753();
        boolean rightSide = mainArm == class_1306.field_6183 && hand == class_1268.field_5808 || mainArm == class_1306.field_6182 && hand == class_1268.field_5810;
        double sideMultiplier = rightSide ? -1.0 : 1.0;
        double handOffsetX = Math.cos(yaw) * 0.16 * sideMultiplier;
        double handOffsetY = (double)TrajectoriesHack.MC.field_1724.method_5751() - 0.1;
        double handOffsetZ = Math.sin(yaw) * 0.16 * sideMultiplier;
        return new class_243(handOffsetX, handOffsetY, handOffsetZ);
    }

    private class_243 getStartingMotion(double yaw, double pitch, double throwPower) {
        double cosOfPitch = Math.cos(pitch);
        double arrowMotionX = -Math.sin(yaw) * cosOfPitch;
        double arrowMotionY = -Math.sin(pitch);
        double arrowMotionZ = Math.cos(yaw) * cosOfPitch;
        return new class_243(arrowMotionX, arrowMotionY, arrowMotionZ).method_1029().method_1021(throwPower);
    }

    private ColorSetting getColor(Trajectory trajectory) {
        return switch (trajectory.type()) {
            default -> throw new MatchException(null, null);
            case class_239.class_240.field_1333 -> this.missColor;
            case class_239.class_240.field_1331 -> this.entityHitColor;
            case class_239.class_240.field_1332 -> this.blockHitColor;
        };
    }

    private record Trajectory(ArrayList<class_243> path, class_239.class_240 type) {
        public boolean isEmpty() {
            return this.path.isEmpty();
        }

        public class_238 getEndBox() {
            class_243 end = this.path.get(this.path.size() - 1);
            return new class_238(end.method_61888(0.5), end.method_61889(0.5));
        }
    }
}
