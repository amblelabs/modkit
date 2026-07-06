package dev.amble.lib.client.bedrock;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.animation.AnimatedInstance;
import dev.amble.lib.animation.client.AnimatedEntityModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedrockEntityModel<T extends Entity & AnimatedEntity> extends net.minecraft.client.render.entity.model.EntityModel<T> implements AnimatedEntityModel {
	private final BedrockModel model;
	private final ModelPart root;
	private final Map<String, ModelPart> partsByName = new HashMap<>();
	private final int textureWidth;
	private final int textureHeight;

	public BedrockEntityModel(BedrockModel model) {
		this.model = model;
		this.root = model.create().createModel();
		this.textureWidth = model.geometry.get(0).description.textureWidth;
		this.textureHeight = model.geometry.get(0).description.textureHeight;
		indexPartsRecursive();
	}

	private void indexPartsRecursive() {
		partsByName.clear();
		indexPartChildren(root);
	}

	@SuppressWarnings("unchecked")
	private void indexPartChildren(ModelPart part) {
		try {
			Field childrenField = ModelPart.class.getDeclaredField("children");
			childrenField.setAccessible(true);
			Map<String, ModelPart> children = (Map<String, ModelPart>) childrenField.get(part);
			if (children == null) return;

			for (Map.Entry<String, ModelPart> e : children.entrySet()) {
				partsByName.put(e.getKey(), e.getValue());
				indexPartChildren(e.getValue());
			}
		} catch (Exception ignored) {
			for (BedrockModel.Bone bone : model.geometry.get(0).bones) {
				if (bone.name == null) continue;
				try {
					ModelPart p = root.getChild(bone.name);
					if (p != null) partsByName.put(bone.name, p);
				} catch (Exception ignored2) {}
			}
		}
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

		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));

		BedrockPerFaceRenderer.render(
				this.root,
				this.model,
				this.partsByName,
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