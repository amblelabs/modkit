package dev.amble.lib.animation.client;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.client.bedrock.BedrockEntityModel;
import dev.amble.lib.client.bedrock.BedrockModel;
import dev.amble.lib.client.bedrock.BedrockModelReference;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BedrockEntityRenderer<T extends LivingEntity & AnimatedEntity> extends LivingEntityRenderer<T, BedrockEntityModel<T>> {
	public BedrockEntityRenderer(EntityRendererFactory.Context ctx, float shadowRadius) {
		super(ctx, null, shadowRadius);
	}

	public BedrockEntityRenderer(EntityRendererFactory.Context ctx) {
		this(ctx, 0.5f);
	}

	@Override
	public void render(T livingEntity, float f, float g, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i) {
		if (this.model == null) this.refreshModel(livingEntity);

		matrices.push();

		matrices.translate(0.0D, -1.5D, 0.0D);
		super.render(livingEntity, f, g, matrices, vertexConsumerProvider, i);

		matrices.pop();
	}

	@Override
	protected void renderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.translate(0, 1.5D, 0);
		super.renderLabelIfPresent(entity, text, matrices, vertexConsumers, light);
		matrices.pop();
	}

	@Override
	public Identifier getTexture(T entity) {
		return entity.getTexture();
	}

	protected BedrockEntityModel<T> refreshModel(T entity) {
		BedrockModelReference ref = entity.getModel();
		if (ref == null) throw new IllegalStateException("Entity " + entity + " does not have a BedrockModelReference");
		BedrockModel bedrock = ref.get().orElseThrow(() -> new IllegalStateException("BedrockModel " + ref.id() + " not found for entity " + entity));

		this.model = new BedrockEntityModel<>(bedrock);
		return this.model;
	}
}
