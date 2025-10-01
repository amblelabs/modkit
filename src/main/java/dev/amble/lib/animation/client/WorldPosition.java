package dev.amble.lib.animation.client;

import dev.amble.lib.animation.EffectProvider;
import dev.amble.lib.client.bedrock.BedrockAnimation;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicReference;

public class WorldPosition {
	private static final WorldPosition INSTANCE = new WorldPosition();

	private BlockView area;
	@Getter
	private Vec3d pos = Vec3d.ZERO;
	private final Vector3f horizontalPlane = new Vector3f(0.0F, 0.0F, 1.0F);
	private final Vector3f verticalPlane = new Vector3f(0.0F, 1.0F, 0.0F);
	private final Vector3f diagonalPlane = new Vector3f(1.0F, 0.0F, 0.0F);
	@Getter
	private float pitch;
	@Getter
	private float yaw;
	private final Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);

	protected void moveBy(double x, double y, double z) {
		double d = (double) this.horizontalPlane.x() * x + (double) this.verticalPlane.x() * y + (double) this.diagonalPlane.x() * z;
		double e = (double) this.horizontalPlane.y() * x + (double) this.verticalPlane.y() * y + (double) this.diagonalPlane.y() * z;
		double f = (double) this.horizontalPlane.z() * x + (double) this.verticalPlane.z() * y + (double) this.diagonalPlane.z() * z;
		this.setPos(new Vec3d(this.pos.x + d, this.pos.y + e, this.pos.z + f));
	}

	protected void setRotation(float yaw, float pitch) {
		this.pitch = pitch;
		this.yaw = yaw;
		this.rotation.rotationYXZ(-yaw * (float) (Math.PI / 180.0), pitch * (float) (Math.PI / 180.0), 0.0F);
		this.horizontalPlane.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
		this.verticalPlane.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
		this.diagonalPlane.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
	}

	protected void setRotation(Vec3d rotation) {
		Pair<Float, Float> rots = BedrockAnimation.eulerToPitchYaw(rotation);

		this.pitch = rots.getLeft();
		this.yaw = rots.getRight();

		this.rotation.rotationXYZ((float) rotation.getX(), (float) rotation.getY(), (float) rotation.getZ());
		this.horizontalPlane.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
		this.verticalPlane.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
		this.diagonalPlane.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
	}

	protected void setPos(double x, double y, double z) {
		this.setPos(new Vec3d(x, y, z));
	}

	protected void setPos(Vec3d pos) {
		this.pos = pos;
	}

	public void spawnParticle(ParticleEffect particle, Vec3d velocity, int count) {
		if (!(this.area instanceof ClientWorld world)) return;

		for (int i = 0; i < count; i++) {
			world.addParticle(particle, this.pos.x, this.pos.y, this.pos.z, velocity.x, velocity.y, velocity.z);
		}
	}

	public WorldPosition update(BedrockAnimation anim, String boneName, float progress, EffectProvider target, ModelPart root) {
		float tickDelta = MinecraftClient.getInstance().getTickDelta();

		this.area = target.getWorld();

		this.setPos(
			target.getEffectPosition(tickDelta)
		);

		Vec3d position = anim.boneTimelines.containsKey(boneName) ? anim.boneTimelines.get(boneName).position().resolve(progress) : Vec3d.ZERO;
		Vec3d animRotation = anim.boneTimelines.containsKey(boneName) ? anim.boneTimelines.get(boneName).rotation().resolve(progress) : Vec3d.ZERO;

		AtomicReference<Float> height = new AtomicReference<>((float) 0);
		AtomicReference<Float> lowest = new AtomicReference<>((float) 0);

		ModelPart bone = root.traverse().filter(part -> part.hasChild(boneName)).findFirst().map(part -> part.getChild(boneName)).orElse(null);

		if (bone != null) {
			position = position.add(bone.getDefaultTransform().pivotX, bone.getDefaultTransform().pivotY, bone.getDefaultTransform().pivotZ);

			bone.forEachCuboid(new MatrixStack(), (matrix, path, index, cuboid) -> {
				height.updateAndGet(v -> cuboid.minY + bone.getDefaultTransform().pivotY + -1.68F*16F);
			});

			root.forEachCuboid(new MatrixStack(), (matrix, path, index, cuboid) -> {
				lowest.updateAndGet(v -> Math.max(v, cuboid.maxY + bone.getDefaultTransform().pivotY));
			});
		}

		Pair<Float, Float> rots = anim.getRotations(boneName, progress);
		float animYaw = rots.getRight();
		float animPitch = rots.getLeft();

		float entityYaw;

		if (anim.metadata.fpsCameraCopiesHead()) {
			entityYaw = (target instanceof ClientPlayerEntity clientPlayer) ? (MathHelper.lerpAngleDegrees(tickDelta, clientPlayer.prevHeadYaw, clientPlayer.headYaw)) : target.getHeadYaw();
		} else {
			entityYaw = (target instanceof ClientPlayerEntity clientPlayer) ? (MathHelper.lerpAngleDegrees(tickDelta, clientPlayer.prevBodyYaw, clientPlayer.bodyYaw)) : target.getBodyYaw();
		}

		float entityPitch = (target instanceof ClientPlayerEntity clientPlayer) ? (MathHelper.lerp(tickDelta, clientPlayer.prevPitch, clientPlayer.getPitch(tickDelta))) : target.getPitch();

		Vec3d relativePos = position.rotateY((float) Math.toRadians(90)).multiply(-1 / 16F);
		this.setRotation(entityYaw, 0);
		// todo \/ the clipping causes the camera to break when on ground
		this.moveBy(relativePos.x, relativePos.y + 1.68, relativePos.z);
		//this.setRotation(animRotation);
		this.setRotation(animYaw + entityYaw, animPitch);

		this.moveBy(0, height.get() / 32F, 0);


		return this;
	}

	public static WorldPosition create(BedrockAnimation anim, String part, float progress, EffectProvider target, ModelPart root) {
		return new WorldPosition().update(anim, part, progress, target, root);
	}

	public static WorldPosition get(BedrockAnimation anim, String part, float progress, EffectProvider target, ModelPart root) {
		return INSTANCE.update(anim, part, progress, target, root);
	}
}
