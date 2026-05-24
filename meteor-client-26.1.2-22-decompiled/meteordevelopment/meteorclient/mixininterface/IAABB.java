package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.core.BlockPos;

public interface IAABB {
    public void meteor$expand(double var1);

    public void meteor$set(double var1, double var3, double var5, double var7, double var9, double var11);

    default public void meteor$set(BlockPos pos) {
        this.meteor$set(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
}
