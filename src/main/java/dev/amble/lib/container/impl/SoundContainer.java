package dev.amble.lib.container.impl;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

import dev.amble.lib.container.RegistryContainer;

public interface SoundContainer extends RegistryContainer<SoundEvent> {

    @Override
    default Registry<SoundEvent> getRegistry() {
        return Registries.SOUND_EVENT;
    }

    @Override
    default Class<SoundEvent> getTargetClass() {
        return SoundEvent.class;
    }
}
