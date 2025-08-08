package dev.amble.lib.test.core.block.entities;

import dev.amble.lib.api.sync.link.block.AbstractLinkableBlockEntity;
import dev.amble.lib.api.sync.manager.SyncManager;
import dev.amble.lib.api.sync.manager.server.ServerSyncManager;
import dev.amble.lib.test.sync.ExampleRoot;
import dev.amble.lib.test.sync.server.ExampleServerRoot;
import dev.amble.lib.test.sync.server.ExampleServerSyncManager;
import dev.amble.lib.util.ServerLifecycleHooks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TestLinkBlockEntity extends AbstractLinkableBlockEntity<ExampleRoot> {
	public TestLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	public TestLinkBlockEntity(BlockPos pos, BlockState state) {
		this(TestBlockEntities.TEST_LINK_BLOCK_ENTITY, pos, state);
	}

	@Override
	public ExampleServerSyncManager getSyncManager() {
		return ExampleServerSyncManager.getInstance();
	}

	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		ExampleServerRoot root = new ExampleServerRoot(UUID.randomUUID());
		ExampleServerSyncManager.getInstance().add(root);
		this.link(root);
	}

	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		if (!this.isLinked()) return ActionResult.FAIL;

		ExampleRoot root = this.parent().get();
		player.sendMessage(Text.literal("ID: " + root.getUuid()), false);
		player.sendMessage(Text.literal("Is Client? " + world.isClient()), false);
		player.sendMessage(Text.literal("Is Awesome? " + root.first().isAwesome().get()), false);
		player.sendMessage(Text.literal("Is Epic? " + root.second().isEpic().get()), false);

		if (!world.isClient()) {
			root.first().isAwesome().set(!root.first().isAwesome().get());
			root.second().isEpic().set(!root.second().isEpic().get());
		}

		return ActionResult.SUCCESS;
	}

	public void onBreak() {
		// delete
		if (!this.isLinked()) return;
		ExampleServerSyncManager.getInstance().remove(ServerLifecycleHooks.get(), (ExampleServerRoot) this.parent().get());
	}
}
