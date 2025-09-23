/*
 * Copyright (C) 2025 AmbleLabs
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * This code is MPL, due to it referencing this code: https://gitlab.com/cable-mc/cobblemon/-/blob/main/common/src/main/kotlin/com/cobblemon/mod/common/client/render/models/blockbench/bedrock/animation/BedrockAnimation.kt?ref_type=heads
 */


package dev.amble.lib.client.bedrock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import dev.amble.lib.AmbleKit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.AnimationState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;

import static net.minecraft.util.math.MathHelper.catmullRom;

@Environment(EnvType.CLIENT)
public class BedrockAnimation {
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(BedrockModel.LocatorBone.class, new BedrockModel.LocatorBone.Adapter())
			.registerTypeAdapter(BedrockAnimation.class, new BedrockAnimationAdapter())
			.create();

	public final boolean shouldLoop;
	public final double animationLength;
	public final Map<String, BoneTimeline> boneTimelines;
	public final boolean overrideBones;
	public String name;

	public BedrockAnimation(boolean shouldLoop, double animationLength, boolean overrideBones, Map<String, BoneTimeline> boneTimelines) {
		this.shouldLoop = shouldLoop;
		this.animationLength = animationLength;
		this.boneTimelines = boneTimelines;
		this.overrideBones = overrideBones;
	}

	@Environment(EnvType.CLIENT)
	public void apply(ModelPart root, double runningSeconds) {
		if (this.overrideBones) {
			this.resetBones(root, runningSeconds);
		}

		this.boneTimelines.forEach((boneName, timeline) -> {
			try {
				ModelPart bone = root.traverse().filter(part -> part.hasChild(boneName)).findFirst().map(part -> part.getChild(boneName)).orElse(null);
				if (bone == null) {
					if (boneName == "root") {
						bone = root;
					} else {
						throw new IllegalStateException("Bone " + boneName + " not found in model. If this is the root part, ensure it is named 'root'.");
					}
				}

				if (!timeline.position.isEmpty()) {
					Vec3d position = timeline.position.resolve(runningSeconds);

					// traverse includes self
					bone.traverse().forEach(child -> {
						child.pivotX += (float) position.x;
						child.pivotY += (float) position.y;
						child.pivotZ += (float) position.z;
					});
				}

				if (!timeline.rotation.isEmpty()) {
					Vec3d rotation = timeline.rotation.resolve(runningSeconds);

					bone.traverse().forEach(child -> {
						child.pitch += (float) Math.toRadians((float) rotation.x);
						child.yaw += (float) Math.toRadians((float) rotation.y);
						child.roll += (float) Math.toRadians((float) rotation.z);
					});
				}

				if (!timeline.scale.isEmpty()) {
					Vec3d scale = timeline.scale.resolve(runningSeconds);

					bone.traverse().forEach(child -> {
						child.xScale *= (float) scale.x;
						child.yScale *= (float) scale.y;
						child.zScale *= (float) scale.z;
					});
				}
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Failed apply animation to {} in model. Skipping animation application for this bone.", boneName, e);
			}
		});

		boolean isComplete = !this.shouldLoop && runningSeconds >= this.animationLength;
		if (isComplete) {
			this.resetBones(root, runningSeconds);
		}
	}

	@Environment(EnvType.CLIENT)
	public void apply(ModelPart root, AnimationState state, float progress, float speedMultiplier) {
		double seconds = getRunningSeconds(state, progress, speedMultiplier);
		state.run(s -> apply(root, seconds));
	}

	public void apply(ModelPart root, int totalTicks, float rawDelta) {
		float ticks = (float) ((totalTicks / 20F) % (this.animationLength)) * 20;
		float delta = rawDelta / 10F;

		apply(root, (ticks / 20) + delta);
	}

	public double getRunningSeconds(AnimationState state, float progress, float speedMultiplier) {
		state.update(progress, speedMultiplier);

		return getRunningSeconds(state);
	}

	public double getRunningSeconds(AnimationState state) {
		float f = (float)state.getTimeRunning() / 1000.0F;
		double seconds = this.shouldLoop ? f % this.animationLength : f;

		return seconds;
	}

	public boolean isFinished(AnimationState state) {
		if (this.shouldLoop) return false;

		return getRunningSeconds(state) >= this.animationLength;
	}

	public void resetBones(ModelPart root, double runningSeconds) {
		this.boneTimelines.forEach((boneName, timeline) -> {
			try {
				ModelPart bone = root.traverse().filter(part -> part.hasChild(boneName)).findFirst().map(part -> part.getChild(boneName)).orElse(null);
				if (bone == null) {
					if ("root".equals(boneName)) {
						bone = root;
					} else {
						throw new IllegalStateException("Bone " + boneName + " not found in model. If this is the root part, ensure it is named 'root'.");
					}
				}

				bone.traverse().forEach(ModelPart::resetTransform);
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Failed to reset animation on {} in model. Skipping animation reset for this bone.", boneName, e);
			}
		});
	}

	public static class Group {
		@SerializedName("format_version")
		public String version;
		public Map<String, BedrockAnimation> animations;
	}

	public record BoneTimeline(BoneValue position, BoneValue rotation, BoneValue scale) {
	}

	public static class SimpleBoneValue implements BoneValue {
		public final Vec3d value;
		public final Transformation transformation;

		public SimpleBoneValue(Vec3d value, Transformation transformation) {
			this.value = value.multiply(1, (transformation == Transformation.POSITION) ? -1 : 1, 1);
			this.transformation = transformation;
		}

		@Override
		public Vec3d resolve(double time) {
			return value;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}
	}

	public static class KeyFrameBoneValue extends TreeMap<Double, KeyFrame> implements BoneValue {

		private KeyFrame getAtIndex(SortedMap<Double, KeyFrame> map, Integer index) {
			if (index == null) return null;
			if (index < 0 || index >= map.size()) return null;
			Double key = new ArrayList<>(map.keySet()).get(index);
			return map.get(key);
		}

		@Override
		public Vec3d resolve(double time) {
			List<Double> keyList = new ArrayList<>(this.keySet());

			Integer afterIndex = null;
			for (int i = 0; i < keyList.size(); i++) {
				if (keyList.get(i) > time) {
					afterIndex = i;
					break;
				}
			}

			Integer beforeIndex;
			if (afterIndex == null) {
				beforeIndex = this.size() - 1;
			} else if (afterIndex == 0) {
				beforeIndex = null;
			} else {
				beforeIndex = afterIndex - 1;
			}

			KeyFrame after = getAtIndex(this, afterIndex);
			KeyFrame before = getAtIndex(this, beforeIndex);

			Vec3d afterData = (after != null && after.getPre() != null) ? after.getPre().resolve(time) : Vec3d.ZERO;
			Vec3d beforeData = (before != null && before.getPost() != null) ? before.getPost().resolve(time) : Vec3d.ZERO;

			if (before != null || after != null) {
				boolean smoothBefore = before != null && before.interpolationType == InterpolationType.SMOOTH;
				boolean smoothAfter = after != null && after.interpolationType == InterpolationType.SMOOTH;

				if (smoothBefore || smoothAfter) {
					if (before != null && after != null) {
						Integer beforePlusIndex = beforeIndex == 0 ? null : beforeIndex - 1;
						KeyFrame beforePlus = getAtIndex(this, beforePlusIndex);

						Integer afterPlusIndex = afterIndex == this.size() - 1 ? null : afterIndex + 1;
						KeyFrame afterPlus = getAtIndex(this, afterPlusIndex);

						Vec3d beforePlusData = (beforePlus != null && beforePlus.getPost() != null) ? beforePlus.getPost().resolve(time) : beforeData;
						Vec3d afterPlusData = (afterPlus != null && afterPlus.getPre() != null) ? afterPlus.getPre().resolve(time) : afterData;

						double t = (time - before.time) / (after.time - before.time);

						return new Vec3d(
								catmullRom((float) t, (float) beforePlusData.x, (float) beforeData.x, (float) afterData.x, (float) afterPlusData.x),
								catmullRom((float) t, (float) beforePlusData.y, (float) beforeData.y, (float) afterData.y, (float) afterPlusData.y),
								catmullRom((float) t, (float) beforePlusData.z, (float) beforeData.z, (float) afterData.z, (float) afterPlusData.z)
						);
					} else if (before != null) {
						return beforeData;
					} else {
						return afterData;
					}
				} else {
					if (before != null && after != null) {
						double alpha = MathHelper.lerp(time, before.time, after.time);

						return new Vec3d(
								beforeData.getX() + (afterData.getX() - beforeData.getX()) * alpha,
								beforeData.getY() + (afterData.getY() - beforeData.getY()) * alpha,
								beforeData.getZ() + (afterData.getZ() - beforeData.getZ()) * alpha
						);
					} else if (before != null) {
						return beforeData;
					} else {
						return afterData;
					}
				}
			} else {
				return new Vec3d(0.0, 0.0, 0.0);
			}
		}
	}

	public static class EmptyBoneValue implements BoneValue {
		public static final EmptyBoneValue INSTANCE = new EmptyBoneValue();

		private EmptyBoneValue() {}

		@Override
		public Vec3d resolve(double time) {
			return Vec3d.ZERO;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}
	}


	public interface BoneValue {
		Vec3d resolve(double time);
		boolean isEmpty();
	}

	public abstract static class KeyFrame {
		public final double time;
		public final Transformation transformation;
		public final InterpolationType interpolationType;

		public KeyFrame(double time, Transformation transformation, InterpolationType interpolationType) {
			this.time = time;
			this.transformation = transformation;
			this.interpolationType = interpolationType;
		}

		public abstract BoneValue getPre();
		public abstract BoneValue getPost();
	}

	public static class SimpleKeyFrame extends KeyFrame {
		public final BoneValue data;

		public SimpleKeyFrame(double time, Transformation transformation, InterpolationType interpolationType, BoneValue data) {
			super(time, transformation, interpolationType);
			this.data = data;
		}

		@Override
		public BoneValue getPre() {
			return data;
		}

		@Override
		public BoneValue getPost() {
			return data;
		}
	}

	public static class JumpKeyFrame extends KeyFrame {

		private final BoneValue pre;
		private final BoneValue post;

		public JumpKeyFrame(
				double time,
				Transformation transformation,
				InterpolationType interpolationType,
				BoneValue pre,
				BoneValue post
		) {
			super(time, transformation, interpolationType);
			this.pre = pre;
			this.post = post;
		}

		@Override
		public BoneValue getPre() {
			return pre;
		}

		@Override
		public BoneValue getPost() {
			return post;
		}
	}

	public enum InterpolationType {
		SMOOTH, LINEAR
	}

	public enum Transformation {
		POSITION, ROTATION, SCALE
	}
}
