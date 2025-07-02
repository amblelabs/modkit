package dev.amble.lib.test.core.block.entities;

import dev.amble.lib.container.impl.BlockEntityContainer;
import dev.amble.lib.test.core.block.TestBlocks;
import net.minecraft.block.entity.BlockEntityType;

public class TestBlockEntities implements BlockEntityContainer {
	public static final BlockEntityType<TestLinkBlockEntity> TEST_LINK_BLOCK_ENTITY = BlockEntityType.Builder.create(TestLinkBlockEntity::new, TestBlocks.TEST_LINK_BLOCK).build(null);
}
