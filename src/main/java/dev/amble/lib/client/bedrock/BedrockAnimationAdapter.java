/*
 * Copyright (C) 2025 AmbleLabs
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * This code is MPL, due to it referencing this code: https://gitlab.com/cable-mc/cobblemon/-/blob/main/common/src/main/kotlin/com/cobblemon/mod/common/client/render/models/blockbench/bedrock/animation/BedrockAnimationAdapter.kt
 */


package dev.amble.lib.client.bedrock;

import com.google.gson.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class BedrockAnimationAdapter implements JsonDeserializer<BedrockAnimation> {

	public static final BedrockAnimationAdapter INSTANCE = new BedrockAnimationAdapter();

	public BedrockAnimationAdapter() {}

	@Override
	public BedrockAnimation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!json.isJsonObject()) {
			throw new IllegalStateException("animation json could not be parsed");
		}

		JsonObject jsonObj = json.getAsJsonObject();
		double animationLength = jsonObj.has("animation_length") ? jsonObj.get("animation_length").getAsDouble() : -1.0;
		boolean shouldLoop = animationLength > 0 && jsonObj.has("loop") && jsonObj.get("loop").getAsBoolean();
		boolean overrideBones = jsonObj.has("override_previous_animation") && jsonObj.get("override_previous_animation").getAsBoolean();

		Map<String, BedrockAnimation.BoneTimeline> boneTimelines = new HashMap<>();

		if (jsonObj.has("bones")) {
			for (Map.Entry<String, JsonElement> entry : jsonObj.getAsJsonObject("bones").entrySet()) {
				boneTimelines.put(entry.getKey(), deserializeBoneTimeline(entry.getValue().getAsJsonObject()));
			}
		}

		return new BedrockAnimation(shouldLoop, animationLength, overrideBones, boneTimelines);
	}

	private BedrockAnimation.BoneTimeline deserializeBoneTimeline(JsonObject bone) {
		BedrockAnimation.BoneValue positions = BedrockAnimation.EmptyBoneValue.INSTANCE;
		BedrockAnimation.BoneValue rotations = BedrockAnimation.EmptyBoneValue.INSTANCE;
		BedrockAnimation.BoneValue scale = BedrockAnimation.EmptyBoneValue.INSTANCE;

		if (bone.has("position") && bone.get("position").isJsonObject()) {
			positions = deserializeKeyframe(bone.getAsJsonObject("position"), BedrockAnimation.Transformation.POSITION);
		}

		if (bone.has("rotation") && bone.get("rotation").isJsonObject()) {
			rotations = deserializeKeyframe(bone.getAsJsonObject("rotation"), BedrockAnimation.Transformation.ROTATION);
		}

		if (bone.has("scale")) {
			JsonElement json = bone.get("scale");
			if (json.isJsonObject()) {
				scale = deserializeKeyframe(json.getAsJsonObject(), BedrockAnimation.Transformation.SCALE);
			}
		}

		return new BedrockAnimation.BoneTimeline(positions, rotations, scale);
	}

	private BedrockAnimation.KeyFrameBoneValue deserializeKeyframe(JsonObject frames, BedrockAnimation.Transformation transformation) {
		BedrockAnimation.KeyFrameBoneValue keyframes = new BedrockAnimation.KeyFrameBoneValue();

		for (Map.Entry<String, JsonElement> entry : frames.entrySet()) {
			double time = Double.parseDouble(entry.getKey());
			JsonElement keyframeJson = entry.getValue();

			if (keyframeJson.isJsonObject()) {
				JsonObject kfObj = keyframeJson.getAsJsonObject();
				BedrockAnimation.InterpolationType type = "catmullrom".equals(
						kfObj.has("lerp_mode") ? kfObj.get("lerp_mode").getAsString() : "linear")
						? BedrockAnimation.InterpolationType.SMOOTH
						: BedrockAnimation.InterpolationType.LINEAR;


				if (kfObj.has("post")) {
					JsonElement post = kfObj.get("post");
					keyframes.put(time, new BedrockAnimation.JumpKeyFrame(time, transformation, type, deserializeSimpleBoneValue(kfObj.has("pre") ? kfObj.getAsJsonArray("pre") : post.getAsJsonArray(), transformation),
							deserializeSimpleBoneValue(post.getAsJsonArray(), transformation)));
				} else if (kfObj.has("pre")) {
					JsonElement pre = kfObj.get("pre");
					keyframes.put(time, new BedrockAnimation.JumpKeyFrame(time, transformation, type, deserializeSimpleBoneValue(pre.getAsJsonArray(), transformation), deserializeSimpleBoneValue(kfObj.has("post") ? kfObj.getAsJsonArray("post") : pre.getAsJsonArray(), transformation)));
				}
			} else {
				keyframes.put(time, new BedrockAnimation.SimpleKeyFrame(
						time,
						transformation,
						BedrockAnimation.InterpolationType.LINEAR,
						deserializeSimpleBoneValue(keyframeJson.getAsJsonArray(), transformation)
				));
			}
		}

		return keyframes;
	}

	private BedrockAnimation.SimpleBoneValue deserializeSimpleBoneValue(JsonArray array, BedrockAnimation.Transformation transformation) {
		return new BedrockAnimation.SimpleBoneValue(new Vec3d(parseMath(String.valueOf(array.get(0))),
				parseMath(String.valueOf(array.get(1))),
				parseMath(String.valueOf(array.get(2)))), transformation);
	}

	public static float parseMath(String data) {
		// parses math expressions like "1 + 2 * 3" or "1 - 2 / 3"
		// using net.objecthunter.exp4j
		Expression expression = new ExpressionBuilder(data).build();
		double result = expression.evaluate();
		return (float) result;
	}
}
