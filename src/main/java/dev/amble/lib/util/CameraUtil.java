package dev.amble.lib.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CameraUtil {
	public static void cameraMixin(Consumer<Vec3d> callback, BiConsumer<Float, Float> setRot, Vec3d rotation, Vec3d position, Entity entity, float yaw, float height, boolean isHead) {
		Vec3d rotatedOffset = position/*.rotateY(-(float)Math.toRadians(yaw))*/;

		Vec3d pos = rotatedOffset.multiply(-1 / 16F);

		if (isHead) pos = pos.add(0, height, 0);

		setRot.accept((float) (rotation.y + yaw), (float) rotation.x);
		callback.accept(pos.add(0, height, 0));
	}
}
