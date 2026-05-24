package meteordevelopment.meteorclient.utils.player;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

public class CustomPlayerInput
extends ClientInput {
    public void tick() {
        float f;
        float f2 = this.keyPresses.forward() == this.keyPresses.backward() ? 0.0f : (f = this.keyPresses.forward() ? 1.0f : -1.0f);
        float g = this.keyPresses.left() == this.keyPresses.right() ? 0.0f : (this.keyPresses.left() ? 1.0f : -1.0f);
        this.moveVector = new Vec2(g, f).normalized();
    }

    public void stop() {
        this.keyPresses = Input.EMPTY;
    }

    public void forward(boolean bool) {
        this.keyPresses = new Input(bool, this.keyPresses.backward(), this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
    }

    public void backward(boolean bool) {
        this.keyPresses = new Input(this.keyPresses.forward(), bool, this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
    }

    public void left(boolean bool) {
        this.keyPresses = new Input(this.keyPresses.forward(), this.keyPresses.backward(), bool, this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
    }

    public void right(boolean bool) {
        this.keyPresses = new Input(this.keyPresses.forward(), this.keyPresses.backward(), this.keyPresses.left(), bool, this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
    }

    public void jump(boolean bool) {
        this.keyPresses = new Input(this.keyPresses.forward(), this.keyPresses.backward(), this.keyPresses.left(), this.keyPresses.right(), bool, this.keyPresses.shift(), this.keyPresses.sprint());
    }

    public void sneak(boolean bool) {
        this.keyPresses = new Input(this.keyPresses.forward(), this.keyPresses.backward(), this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), bool, this.keyPresses.sprint());
    }

    public void sprint(boolean bool) {
        this.keyPresses = new Input(this.keyPresses.forward(), this.keyPresses.backward(), this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), bool);
    }
}
