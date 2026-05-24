package net.wurstclient.util;

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1511;
import net.minecraft.class_1678;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.wurstclient.WurstClient;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.RotationUtils;

public final class EntityUtils
extends Enum<EntityUtils> {
    protected static final WurstClient WURST;
    protected static final class_310 MC;
    public static final Predicate<class_1297> IS_ATTACKABLE;
    public static final Predicate<class_1429> IS_VALID_ANIMAL;
    private static final /* synthetic */ EntityUtils[] $VALUES;

    public static EntityUtils[] values() {
        return (EntityUtils[])$VALUES.clone();
    }

    public static EntityUtils valueOf(String name) {
        return Enum.valueOf(EntityUtils.class, name);
    }

    public static Stream<class_1297> getAttackableEntities() {
        return StreamSupport.stream(EntityUtils.MC.field_1687.method_18112().spliterator(), true).filter(IS_ATTACKABLE);
    }

    public static Stream<class_1429> getValidAnimals() {
        return StreamSupport.stream(EntityUtils.MC.field_1687.method_18112().spliterator(), true).filter(class_1429.class::isInstance).map(e -> (class_1429)e).filter(IS_VALID_ANIMAL);
    }

    public static class_243 getLerpedPos(class_1297 e, float partialTicks) {
        if (e.method_31481()) {
            return e.method_73189();
        }
        double x = class_3532.method_16436((double)partialTicks, (double)e.field_6038, (double)e.method_23317());
        double y = class_3532.method_16436((double)partialTicks, (double)e.field_5971, (double)e.method_23318());
        double z = class_3532.method_16436((double)partialTicks, (double)e.field_5989, (double)e.method_23321());
        return new class_243(x, y, z);
    }

    public static class_238 getLerpedBox(class_1297 e, float partialTicks) {
        if (e.method_31481()) {
            return e.method_5829();
        }
        class_243 offset = EntityUtils.getLerpedPos(e, partialTicks).method_1020(e.method_73189());
        return e.method_5829().method_997(offset);
    }

    public static double distanceToHitboxSq(class_1297 e) {
        return EntityUtils.distanceToHitboxSqr(RotationUtils.getEyesPos(), e);
    }

    public static double distanceToHitboxSqr(class_243 point, class_1297 e) {
        class_238 box = e.method_5829();
        double x = class_3532.method_15350((double)point.field_1352, (double)box.field_1323, (double)box.field_1320);
        double y = class_3532.method_15350((double)point.field_1351, (double)box.field_1322, (double)box.field_1325);
        double z = class_3532.method_15350((double)point.field_1350, (double)box.field_1321, (double)box.field_1324);
        return point.method_1025(new class_243(x, y, z));
    }

    private static /* synthetic */ EntityUtils[] $values() {
        return new EntityUtils[0];
    }

    static {
        $VALUES = EntityUtils.$values();
        WURST = WurstClient.INSTANCE;
        MC = WurstClient.MC;
        IS_ATTACKABLE = e -> e != null && !e.method_31481() && (e instanceof class_1309 && ((class_1309)e).method_6032() > 0.0f || e instanceof class_1511 || e instanceof class_1678) && e != EntityUtils.MC.field_1724 && !(e instanceof FakePlayerEntity) && !WURST.getFriends().isFriend((class_1297)e);
        IS_VALID_ANIMAL = a -> a != null && !a.method_31481() && a.method_6032() > 0.0f;
    }
}
