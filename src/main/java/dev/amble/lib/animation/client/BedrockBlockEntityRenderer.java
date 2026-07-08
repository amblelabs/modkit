package dev.amble.lib.animation.client;

import dev.amble.lib.animation.AnimatedBlockEntity;
import dev.amble.lib.animation.AnimatedInstance;
import dev.amble.lib.client.bedrock.BedrockEntityModel;
import dev.amble.lib.client.bedrock.BedrockModel;
import dev.amble.lib.client.bedrock.BedrockModelReference;
import dev.amble.lib.client.bedrock.BedrockModelRegistry;
import dev.amble.plushies.MarketablePlushieBlock;
import dev.amble.plushies.MarketablePlushieBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class BedrockBlockEntityRenderer<T extends BlockEntity & AnimatedBlockEntity> implements BlockEntityRenderer<T> {

	public BedrockBlockEntityRenderer() {}

	public BedrockBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		this();
	}

	@Override
	public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		BedrockEntityModel<?> model = getOrCreateModel(entity);

		matrices.push();
		matrices.translate(0.5D, 0.0D, 0.5D);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRenderYaw()));

		if (entity.getWorld() != null) {
			int sky = entity.getWorld().getLightLevel(LightType.SKY, entity.getPos().up().up());
			light = LightmapTextureManager.pack(0, sky);
		}

        model.setAngles(entity, entity.getAge() + tickDelta);

		model.render(
				matrices,
				vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(this.getTexture(entity))),
				light,
				overlay,
				1.0f, 1.0f, 1.0f, 1.0f
		);

		Identifier emission = entity.getEmissionTexture();
		if (emission != null) {
			model.render(
					matrices,
					vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCullZOffset(emission)),
					LightmapTextureManager.MAX_LIGHT_COORDINATE,
					overlay,
					1.0f, 1.0f, 1.0f, 1.0f
			);
		}

		matrices.pop();
	}

	public Identifier getTexture(T entity) {
		return entity.getTexture();
	}

	protected BedrockEntityModel<?> getOrCreateModel(T entity) {
		BedrockModelReference ref = entity.getModel();
		if (ref == null) {
			throw new IllegalStateException("BlockEntity " + entity + " does not have a BedrockModelReference");
		}

		Identifier modelId = ref.id();
        return new BedrockEntityModel<>(BedrockModelRegistry.getInstance().get(modelId));
    }
}