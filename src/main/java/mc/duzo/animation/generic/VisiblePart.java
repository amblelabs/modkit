package mc.duzo.animation.generic;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

import java.util.function.Consumer;

public enum VisiblePart {
	HEAD(model -> model.head.visible = true),
	HAT(model -> model.hat.visible = true),
	BODY(model -> model.body.visible = true),
	JACKET(model -> model.jacket.visible = true),
	LEFT_ARM(model -> model.leftArm.visible = true),
	LEFT_SLEEVE(model -> model.leftSleeve.visible = true),
	RIGHT_ARM(model -> model.rightArm.visible = true),
	RIGHT_SLEEVE(model -> model.rightSleeve.visible = true),
	LEFT_LEG(model -> model.leftLeg.visible = true),
	LEFT_PANTS(model -> model.leftPants.visible = true),
	RIGHT_LEG(model -> model.rightLeg.visible = true),
	RIGHT_PANTS(model -> model.rightPants.visible = true);

	private final Consumer<PlayerEntityModel<AbstractClientPlayerEntity>> apply;

	VisiblePart(Consumer<PlayerEntityModel<AbstractClientPlayerEntity>> apply) {
		this.apply = apply;
	}

	public void apply(PlayerEntityModel<AbstractClientPlayerEntity> model) {
		this.apply.accept(model);
	}
}
