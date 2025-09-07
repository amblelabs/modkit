package mc.duzo.animation.generic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VisibilityList extends ArrayList<VisiblePart> {
	public VisibilityList() {
		super();
	}

	public VisibilityList(List<VisiblePart> parts) {
		super(parts);
	}

	public void apply(PlayerEntityModel<AbstractClientPlayerEntity> model, @Nullable AbstractClientPlayerEntity player) {
		model.setVisible(false);

		for (VisiblePart part : this) {
			part.apply(model);
		}

		if (player == null) return;

		MinecraftClient client = MinecraftClient.getInstance();
		if (player.equals(client.player) && !client.gameRenderer.getCamera().isThirdPerson()) {
			// force everything to be visible in first person
			model.setVisible(true);
		}
	}

	public static VisibilityList all() {
		return new VisibilityList(List.of(VisiblePart.values()));
	}

	public static VisibilityList none() {
		return new VisibilityList();
	}

	public static VisibilityList firstLayer() {
		return of(VisiblePart.HEAD,
				VisiblePart.BODY,
				VisiblePart.LEFT_ARM,
				VisiblePart.RIGHT_ARM,
				VisiblePart.LEFT_LEG,
				VisiblePart.RIGHT_LEG);
	}

	public static VisibilityList headOnly() {
		return of(VisiblePart.HEAD);
	}

	public static VisibilityList of(VisiblePart... parts) {
		return new VisibilityList(List.of(parts));
	}
}
