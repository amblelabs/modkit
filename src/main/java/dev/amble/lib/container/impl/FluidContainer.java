package dev.amble.lib.container.impl;

import dev.amble.lib.container.RegistryContainer;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface FluidContainer extends RegistryContainer<Fluid> {

    @Override
    default Class<Fluid> getTargetClass() {
        return Fluid.class;
    }

    @Override
    default Registry<Fluid> getRegistry() {
        return Registries.FLUID;
    }
}
