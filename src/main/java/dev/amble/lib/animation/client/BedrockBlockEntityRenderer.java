package dev.amble.lib.animation.client;

import dev.amble.lib.animation.AnimatedBlockEntity;
import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.client.bedrock.BedrockEntityModel;
import dev.amble.lib.client.bedrock.BedrockModel;
import dev.amble.lib.client.bedrock.BedrockModelReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

@Environment(EnvType.CLIENT)
public class BedrockBlockEntityRenderer<T extends BlockEntity & AnimatedBlockEntity> implements BlockEntityRenderer<T> {
	protected BedrockEntityModel model;

	public BedrockBlockEntityRenderer() {
	}

	public BedrockBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		this();
	}

	@Override
	public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if (this.model == null) this.refreshModel(entity);

		matrices.push();
		matrices.translate(0.5D, 0.0D, 0.5D);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));

		light = entity.getWorld().getLightLevel(LightType.SKY, entity.getPos().up().up());
		light = LightmapTextureManager.pack(0, light);

		this.model.setAngles(entity, entity.getAge() + tickDelta);
		this.model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(this.getTexture(entity))), light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

		Identifier emission = entity.getEmissionTexture();
		if (emission != null) {
			this.model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCullZOffset(emission)), LightmapTextureManager.MAX_LIGHT_COORDINATE, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
		}

		matrices.pop();
	}

	public Identifier getTexture(T entity) {
		return entity.getTexture();
	}

	protected BedrockEntityModel refreshModel(T entity) {
		BedrockModelReference ref = entity.getModel();
		if (ref == null) throw new IllegalStateException("BlockEntity " + entity + " does not have a BedrockModelReference");
		BedrockModel bedrock = ref.get().orElseThrow(() -> new IllegalStateException("BedrockModel " + ref.id() + " not found for block entity " + entity));

		this.model = new BedrockEntityModel<>(bedrock);
		return this.model;
	}
}
