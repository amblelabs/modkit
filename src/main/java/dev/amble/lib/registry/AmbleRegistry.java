package dev.amble.lib.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public interface AmbleRegistry<T> extends Registry<T> {

    RegistryKey<? extends Registry<T>> key();
    Registry<T> cached();
}
