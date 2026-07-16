package dev.amble.lib.animation.client;

import dev.amble.lib.animation.AnimatedBlockEntity;
import dev.amble.lib.client.bedrock.BedrockEntityModel;
import dev.amble.lib.client.bedrock.BedrockModelReference;
import dev.amble.lib.client.bedrock.BedrockModelRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

@Environment(EnvType.CLIENT)
public class BedrockBlockEntityRenderer<T extends BlockEntity & AnimatedBlockEntity> implements BlockEntityRenderer<T> {

	protected BedrockEntityModel model;

	public BedrockBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if (this.model == null) this.refreshModel(entity);

		matrices.push();
		matrices.translate(0.5D, 0.0D, 0.5D);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRenderYaw()));

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

	protected BedrockEntityModel refreshModel(T entity) {
		BedrockModelReference ref = entity.getModel();
		if (ref == null) {
			throw new IllegalStateException("BlockEntity " + entity + " does not have a BedrockModelReference");
		}
		Identifier modelId = ref.id();
		return this.model = new BedrockEntityModel<>(BedrockModelRegistry.getInstance().get(modelId));
	}
}