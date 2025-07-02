package dev.amble.lib.test.core.block;

import dev.amble.lib.container.impl.BlockContainer;
import net.minecraft.block.Block;

public class TestBlocks extends BlockContainer {
	public static final Block TEST_LINK_BLOCK = new TestLinkBlock(Block.Settings.copy(net.minecraft.block.Blocks.STONE));
}
