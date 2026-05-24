package net.wurstclient;

import net.minecraft.class_243;
import net.minecraft.class_746;
import net.wurstclient.WurstClient;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.PreMotionListener;
import net.wurstclient.util.Rotation;
import net.wurstclient.util.RotationUtils;

public final class RotationFaker
implements PreMotionListener,
PostMotionListener {
    private boolean fakeRotation;
    private float serverYaw;
    private float serverPitch;
    private float realYaw;
    private float realPitch;

    @Override
    public void onPreMotion() {
        if (!this.fakeRotation) {
            return;
        }
        class_746 player = WurstClient.MC.field_1724;
        this.realYaw = player.method_36454();
        this.realPitch = player.method_36455();
        player.method_36456(this.serverYaw);
        player.method_36457(this.serverPitch);
    }

    @Override
    public void onPostMotion() {
        if (!this.fakeRotation) {
            return;
        }
        class_746 player = WurstClient.MC.field_1724;
        player.method_36456(this.realYaw);
        player.method_36457(this.realPitch);
        this.fakeRotation = false;
    }

    public void faceVectorPacket(class_243 vec) {
        Rotation needed = RotationUtils.getNeededRotations(vec);
        class_746 player = WurstClient.MC.field_1724;
        this.fakeRotation = true;
        this.serverYaw = RotationUtils.limitAngleChange(player.method_36454(), needed.yaw());
        this.serverPitch = needed.pitch();
    }

    public void faceVectorClient(class_243 vec) {
        Rotation needed = RotationUtils.getNeededRotations(vec);
        class_746 player = WurstClient.MC.field_1724;
        player.method_36456(RotationUtils.limitAngleChange(player.method_36454(), needed.yaw()));
        player.method_36457(needed.pitch());
    }

    public void faceVectorClientIgnorePitch(class_243 vec) {
        Rotation needed = RotationUtils.getNeededRotations(vec);
        class_746 player = WurstClient.MC.field_1724;
        player.method_36456(RotationUtils.limitAngleChange(player.method_36454(), needed.yaw()));
        player.method_36457(0.0f);
    }

    public float getServerYaw() {
        return this.fakeRotation ? this.serverYaw : WurstClient.MC.field_1724.method_36454();
    }

    public float getServerPitch() {
        return this.fakeRotation ? this.serverPitch : WurstClient.MC.field_1724.method_36455();
    }
}
