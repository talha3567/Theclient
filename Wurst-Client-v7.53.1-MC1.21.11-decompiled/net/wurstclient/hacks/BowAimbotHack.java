package net.wurstclient.hacks;

import java.awt.Color;
import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"bow aimbot"})
public final class BowAimbotHack
extends Hack
implements UpdateListener,
RenderListener,
GUIRenderListener {
    private final EnumSetting<Priority> priority = new EnumSetting("Priority", "Determines which entity will be attacked first.\n\u00a7lDistance\u00a7r - Attacks the closest entity.\n\u00a7lAngle\u00a7r - Attacks the entity that requires the least head movement.\n\u00a7lAngle+Dist\u00a7r - A hybrid of Angle and Distance. This is usually the best at figuring out what you want to aim at.\n\u00a7lHealth\u00a7r - Attacks the weakest entity.", (Enum[])Priority.values(), (Enum)Priority.ANGLE_DIST);
    private final SliderSetting predictMovement = new SliderSetting("Predict movement", "Controls the strength of BowAimbot's movement prediction algorithm.", 0.2, 0.0, 2.0, 0.01, SliderSetting.ValueDisplay.PERCENTAGE);
    private final EntityFilterList entityFilters = EntityFilterList.genericCombat();
    private final ColorSetting color = new ColorSetting("ESP color", "Color of the box that BowAimbot draws around the target.", Color.RED);
    private class_1297 target;
    private float velocity;

    public BowAimbotHack() {
        super("BowAimbot");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.priority);
        this.addSetting(this.predictMovement);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
        this.addSetting(this.color);
    }

    @Override
    protected void onEnable() {
        BowAimbotHack.WURST.getHax().excavatorHack.setEnabled(false);
        BowAimbotHack.WURST.getHax().templateToolHack.setEnabled(false);
        EVENTS.add(GUIRenderListener.class, this);
        EVENTS.add(RenderListener.class, this);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(GUIRenderListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        class_746 player = BowAimbotHack.MC.field_1724;
        class_1799 stack = BowAimbotHack.MC.field_1724.method_31548().method_7391();
        class_1792 item = stack.method_7909();
        if (!(item instanceof class_1753) && !(item instanceof class_1764)) {
            this.target = null;
            return;
        }
        if (item instanceof class_1753 && !BowAimbotHack.MC.field_1690.field_1904.method_1434() && !player.method_6115()) {
            this.target = null;
            return;
        }
        if (item instanceof class_1764 && !class_1764.method_7781((class_1799)stack)) {
            this.target = null;
            return;
        }
        if (this.filterEntities(Stream.of(this.target)) == null) {
            this.target = this.filterEntities(StreamSupport.stream(BowAimbotHack.MC.field_1687.method_18112().spliterator(), true));
        }
        if (this.target == null) {
            return;
        }
        this.velocity = (float)(72000 - player.method_6014()) / 20.0f;
        this.velocity = (this.velocity * this.velocity + this.velocity * 2.0f) / 3.0f;
        if (this.velocity > 1.0f) {
            this.velocity = 1.0f;
        }
        double d = RotationUtils.getEyesPos().method_1022(this.target.method_5829().method_1005()) * this.predictMovement.getValue();
        double posX = this.target.method_23317() + (this.target.method_23317() - this.target.field_6038) * d - player.method_23317();
        double posY = this.target.method_23318() + (this.target.method_23318() - this.target.field_5971) * d + (double)this.target.method_17682() * 0.5 - player.method_23318() - (double)player.method_18381(player.method_18376());
        double posZ = this.target.method_23321() + (this.target.method_23321() - this.target.field_5989) * d - player.method_23321();
        float neededYaw = (float)Math.toDegrees(Math.atan2(posZ, posX)) - 90.0f;
        BowAimbotHack.MC.field_1724.method_36456(RotationUtils.limitAngleChange(BowAimbotHack.MC.field_1724.method_36454(), neededYaw));
        double hDistance = Math.sqrt(posX * posX + posZ * posZ);
        double hDistanceSq = hDistance * hDistance;
        float g = 0.006f;
        float velocitySq = this.velocity * this.velocity;
        float velocityPow4 = velocitySq * velocitySq;
        float neededPitch = (float)(-Math.toDegrees(Math.atan(((double)velocitySq - Math.sqrt((double)velocityPow4 - (double)g * ((double)g * hDistanceSq + 2.0 * posY * (double)velocitySq))) / ((double)g * hDistance))));
        if (Float.isNaN(neededPitch)) {
            WURST.getRotationFaker().faceVectorClient(this.target.method_5829().method_1005());
        } else {
            BowAimbotHack.MC.field_1724.method_36457(neededPitch);
        }
    }

    private class_1297 filterEntities(Stream<class_1297> s) {
        Stream<class_1297> stream = s.filter(EntityUtils.IS_ATTACKABLE);
        stream = this.entityFilters.applyTo(stream);
        return stream.min(this.priority.getSelected().comparator).orElse(null);
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (this.target == null) {
            return;
        }
        class_238 box = EntityUtils.getLerpedBox(this.target, partialTicks).method_989(0.0, 0.05, 0.0).method_1014(0.05);
        int quadColor = this.color.getColorI(0.5f * this.velocity);
        RenderUtils.drawSolidBox(matrixStack, box, quadColor, false);
        int lineColor = this.color.getColorI(0.25f * this.velocity);
        RenderUtils.drawOutlinedBox(matrixStack, box, lineColor, false);
    }

    @Override
    public void onRenderGUI(class_332 context, float partialTicks) {
        if (this.target == null) {
            return;
        }
        Object message = this.velocity < 1.0f ? "Charging: " + (int)(this.velocity * 100.0f) + "%" : "Target Locked";
        class_327 tr = BowAimbotHack.MC.field_1772;
        int msgWidth = tr.method_1727((String)message);
        int msgX1 = context.method_51421() / 2 - msgWidth / 2;
        int msgX2 = msgX1 + msgWidth + 3;
        int msgY1 = context.method_51443() / 2 + 1;
        int msgY2 = msgY1 + 10;
        context.method_25294(msgX1, msgY1, msgX2, msgY2, Integer.MIN_VALUE);
        context.method_51433(tr, (String)message, msgX1 + 2, msgY1 + 1, -1, false);
    }

    private static enum Priority {
        DISTANCE("Distance", EntityUtils::distanceToHitboxSq),
        ANGLE("Angle", e -> RotationUtils.getAngleToLookVec(e.method_5829().method_1005())),
        ANGLE_DIST("Angle+Dist", e -> Math.pow(RotationUtils.getAngleToLookVec(e.method_5829().method_1005()), 2.0) + EntityUtils.distanceToHitboxSq(e)),
        HEALTH("Health", e -> e instanceof class_1309 ? (double)((class_1309)e).method_6032() : 2.147483647E9);

        private final String name;
        private final Comparator<class_1297> comparator;

        private Priority(String name, ToDoubleFunction<class_1297> keyExtractor) {
            this.name = name;
            this.comparator = Comparator.comparingDouble(keyExtractor);
        }

        public String toString() {
            return this.name;
        }
    }
}
