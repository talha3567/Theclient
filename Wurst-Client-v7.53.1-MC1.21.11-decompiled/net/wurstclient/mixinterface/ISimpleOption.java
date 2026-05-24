package net.wurstclient.mixinterface;

import net.minecraft.class_7172;

public interface ISimpleOption<T> {
    public void forceSetValue(T var1);

    public static <T> ISimpleOption<T> get(class_7172<T> option) {
        return (ISimpleOption)option;
    }
}
