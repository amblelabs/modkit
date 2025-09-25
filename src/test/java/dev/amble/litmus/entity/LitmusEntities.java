package dev.amble.litmus.entity;

import dev.amble.lib.container.impl.EntityContainer;
import dev.amble.litmus.entity.impl.TestEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

public class LitmusEntities implements EntityContainer {
	public static final EntityType<TestEntity> TEST_ENTITY = EntityType.Builder.create(TestEntity::new, SpawnGroup.MISC)
			.setDimensions(0.6f, 1.8f)
			.build("test_entity");
}
