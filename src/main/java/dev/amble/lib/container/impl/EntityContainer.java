package dev.amble.lib.container.impl;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.animation.client.BedrockEntityRenderer;
import dev.amble.lib.animation.HasBedrockModel;
import dev.amble.lib.util.RegistrationUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import dev.amble.lib.container.RegistryContainer;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;

public interface EntityContainer extends RegistryContainer<EntityType<?>> {
	@Override
	default void postProcessField(Identifier identifier, EntityType<?> value, Field field) {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return;

		// automagically register bedrock renderer
		if (!field.isAnnotationPresent(HasBedrockModel.class)) return;

		RegistrationUtil.registerBedrockRenderer(value);
	}

	@Override
	default Class<EntityType<?>> getTargetClass() {
		return RegistryContainer.conform(EntityType.class);
	}

	@Override
	default Registry<EntityType<?>> getRegistry() {
		return Registries.ENTITY_TYPE;
	}
}
