package dev.amble.lib.client.bedrock;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.animation.AnimatedInstance;
import dev.amble.lib.animation.client.AnimatedEntityModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

public class BedrockEntityModel<T extends Entity & AnimatedEntity> extends net.minecraft.client.render.entity.model.EntityModel<T> implements AnimatedEntityModel {
	private final BedrockModel model;
	private final ModelPart root;
	private final int textureWidth;
	private final int textureHeight;

	public BedrockEntityModel(BedrockModel model) {
		this.model = model;
		this.root = model.create().createModel();
		this.textureWidth = model.geometry.get(0).description.textureWidth;
		this.textureHeight = model.geometry.get(0).description.textureHeight;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		this.applyAnimationPre(entity, animationProgress);
		this.applyAnimation(entity, animationProgress);
	}

	public void setAngles(AnimatedInstance instance, float animationProgress) {
		this.applyAnimationPre(instance, animationProgress);
		this.applyAnimation(instance, animationProgress);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);

		List<BedrockModel.PerFaceCube> deferred = model.deferredPerFaceCubes();
		if (deferred.isEmpty()) return;

		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));

		BedrockPerFaceRenderer.render(
				this.root,
				deferred,
				matrices,
				vertices,
				light,
				overlay,
				red, green, blue, alpha,
				this.textureWidth,
				this.textureHeight
		);
		matrices.pop();
	}

	@Override
	public ModelPart getPart() {
		return root;
	}
}