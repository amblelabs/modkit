package dev.amble.lib.container.impl;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.animation.AnimatedBlockEntity;
import dev.amble.lib.animation.AnimatedInstance;
import dev.amble.lib.animation.client.BedrockBlockEntityRenderer;
import dev.amble.lib.animation.HasBedrockModel;
import dev.amble.lib.util.RegistrationUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import dev.amble.lib.container.RegistryContainer;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;

public interface BlockEntityContainer extends RegistryContainer<BlockEntityType<?>> {
    @Override
    default void postProcessField(Identifier identifier, BlockEntityType<?> value, Field field) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return;

        // automagically register bedrock renderer
        if (!field.isAnnotationPresent(HasBedrockModel.class)) return;

	    Class<?> cls = value.getClass();
	    if (AnimatedBlockEntity.class.isAssignableFrom(cls)) {
		    registerRenderer((BlockEntityType<? extends AnimatedBlockEntity>) value);
	    } else {
		    AmbleKit.LOGGER.error("Tried to register bedrock renderer for block entity type {} but its class {} does not implement AnimatedBlockEntity", identifier, cls);
	    }
    }

    @Environment(EnvType.CLIENT)
    private static void registerRenderer(BlockEntityType<? extends AnimatedBlockEntity> type) {
        BlockEntityRendererRegistry.register(type, BedrockBlockEntityRenderer::new);
    }

    @Override
    default Class<BlockEntityType<?>> getTargetClass() {
        return RegistryContainer.conform(BlockEntityType.class);
    }

    @Override
    default Registry<BlockEntityType<?>> getRegistry() {
        return Registries.BLOCK_ENTITY_TYPE;
    }
}
