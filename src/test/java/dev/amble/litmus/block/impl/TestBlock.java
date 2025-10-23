package dev.amble.litmus.block.impl;

import dev.amble.lib.block.ABlockSettings;
import dev.amble.lib.block.AWaterloggableBlock;
import dev.amble.lib.block.behavior.base.BlockWithEntityBehavior;
import dev.amble.litmus.block.entity.impl.TestBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;

public class TestBlock extends AWaterloggableBlock implements BlockEntityProvider {

	public TestBlock(ABlockSettings settings) {
		super(settings, new BlockWithEntityBehavior.Ticking(TestBlockEntity::new));
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}
