package dev.amble.lib.mixin;

import dev.amble.lib.blockentity.StructurePlaceableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    public void place(BlockEntity blockEntity, NbtCompound nbt) {
        if (blockEntity instanceof StructurePlaceableBlockEntity placeable) placeable.amble$onStructurePlaced(nbt);

        blockEntity.readNbt(nbt);
    }
}