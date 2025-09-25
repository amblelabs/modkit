package dev.amble.litmus.block.entity;

import dev.amble.lib.container.impl.BlockEntityContainer;
import dev.amble.litmus.block.LitmusBlocks;
import dev.amble.litmus.block.entity.impl.TestBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;

public class LitmusBlockEntityTypes implements BlockEntityContainer {
	public static BlockEntityType<TestBlockEntity> TEST_BLOCK = FabricBlockEntityTypeBuilder.create(TestBlockEntity::new, LitmusBlocks.TEST_BLOCK).build();
}
