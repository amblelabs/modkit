package dev.amble.lib.blockentity;

import net.minecraft.nbt.NbtCompound;

public interface StructurePlaceableBlockEntity {
    void amble$onStructurePlaced(NbtCompound nbt);
}
