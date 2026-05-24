package net.wurstclient.util;

import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_640;
import net.minecraft.class_745;
import net.minecraft.class_746;
import net.wurstclient.WurstClient;
import org.jetbrains.annotations.Nullable;

public class FakePlayerEntity
extends class_745 {
    private final class_746 player;
    private final class_638 world;
    private class_640 playerListEntry;

    public FakePlayerEntity() {
        super(WurstClient.MC.field_1687, WurstClient.MC.field_1724.method_7334());
        this.player = WurstClient.MC.field_1724;
        this.world = WurstClient.MC.field_1687;
        this.method_5826(UUID.randomUUID());
        this.method_5719((class_1297)this.player);
        this.copyInventory();
        this.method_6127().method_26846(this.player.method_6127());
        this.copyRotation();
        this.spawn();
    }

    @Nullable
    protected class_640 method_3123() {
        if (this.playerListEntry == null) {
            this.playerListEntry = class_310.method_1551().method_1562().method_2871(this.method_7334().id());
        }
        return this.playerListEntry;
    }

    protected void method_6087(class_1297 entity) {
    }

    private void copyInventory() {
        this.method_31548().method_7377(this.player.method_31548());
    }

    private void copyRotation() {
        this.field_6241 = this.player.field_6241;
        this.field_6283 = this.player.field_6283;
    }

    private void spawn() {
        this.world.method_53875((class_1297)this);
    }

    public void despawn() {
        this.method_31472();
    }

    public void resetPlayerPosition() {
        this.player.method_5808(this.method_23317(), this.method_23318(), this.method_23321(), this.method_36454(), this.method_36455());
    }
}
