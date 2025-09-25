package dev.amble.litmus;

import dev.amble.lib.container.RegistryContainer;
import dev.amble.litmus.block.LitmusBlocks;
import dev.amble.litmus.block.entity.LitmusBlockEntityTypes;
import dev.amble.litmus.entity.LitmusEntities;
import dev.amble.litmus.entity.impl.TestEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LitmusMod implements ModInitializer {
	public static final String MOD_ID = "litmus";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		RegistryContainer.register(LitmusBlocks.class, MOD_ID);
		RegistryContainer.register(LitmusBlockEntityTypes.class, MOD_ID);
		RegistryContainer.register(LitmusEntities.class, MOD_ID);

		FabricDefaultAttributeRegistry.register(LitmusEntities.TEST_ENTITY,
				TestEntity.createMobAttributes());
	}
}
