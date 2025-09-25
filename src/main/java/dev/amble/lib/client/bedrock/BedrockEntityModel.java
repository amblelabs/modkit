package dev.amble.lib.client.bedrock;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.animation.AnimatedInstance;
import dev.amble.lib.animation.client.AnimatedEntityModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class BedrockEntityModel<T extends Entity & AnimatedEntity> extends EntityModel<T> implements AnimatedEntityModel {
	private final BedrockModel model;
	private final ModelPart root;

	public BedrockEntityModel(BedrockModel model) {
		this.model = model;
		this.root = model.create().createModel();
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
		this.getPart().render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}
}
