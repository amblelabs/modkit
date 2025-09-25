package dev.amble.litmus.block;

import dev.amble.lib.block.ABlockSettings;
import dev.amble.lib.container.impl.BlockContainer;
import dev.amble.litmus.block.impl.TestBlock;
import net.minecraft.block.Block;

public class LitmusBlocks extends BlockContainer {
	public static final Block TEST_BLOCK = new TestBlock(new ABlockSettings());
}
