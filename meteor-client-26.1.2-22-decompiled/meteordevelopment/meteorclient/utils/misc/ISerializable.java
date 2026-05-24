package meteordevelopment.meteorclient.utils.misc;

import net.minecraft.nbt.CompoundTag;

public interface ISerializable<T> {
    public CompoundTag toTag();

    public T fromTag(CompoundTag var1);
}
