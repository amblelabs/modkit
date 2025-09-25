package dev.amble.lib.container.impl;

import dev.amble.lib.animation.AnimatedBlockEntity;
import dev.amble.lib.animation.client.BedrockBlockEntityRenderer;
import dev.amble.lib.animation.HasBedrockModel;
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

        registerRenderer((BlockEntityType<? extends AnimatedBlockEntity>) value);
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
