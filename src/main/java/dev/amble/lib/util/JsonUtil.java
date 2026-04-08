/*
 * Copyright (C) 2025 AmbleLabs
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.amble.lib.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for common JSON parsing operations.
 */
public final class JsonUtil {

	private JsonUtil() {}

	/**
	 * Parses an enum value from a JSON element that can be either a boolean or a string.
	 * <p>
	 * This handles the common Bedrock animation pattern where a field can be:
	 * <ul>
	 *   <li>{@code true} / {@code false} - maps to {@code trueValue} / {@code falseValue}</li>
	 *   <li>{@code "true"} / {@code "false"} (as strings) - same as above</li>
	 *   <li>An enum name as a string (e.g., {@code "hold_on_last_frame"}) - parsed via {@code valueOf}</li>
	 * </ul>
	 *
	 * @param element      The JSON element to parse (may be null)
	 * @param enumClass    The enum class to parse into
	 * @param trueValue    The enum value to return for boolean {@code true}
	 * @param falseValue   The enum value to return for boolean {@code false}
	 * @param defaultValue The default value to return if parsing fails or element is null
	 * @param <E>          The enum type
	 * @return The parsed enum value, or {@code defaultValue} if parsing fails
	 */
	public static <E extends Enum<E>> E parseEnumOrBoolean(
			@Nullable JsonElement element,
			Class<E> enumClass,
			E trueValue,
			E falseValue,
			E defaultValue
	) {
		if (element == null || !element.isJsonPrimitive()) {
			return defaultValue;
		}

		JsonPrimitive primitive = element.getAsJsonPrimitive();

		if (primitive.isBoolean()) {
			return primitive.getAsBoolean() ? trueValue : falseValue;
		}

		if (primitive.isString()) {
			String value = primitive.getAsString();

			// Handle "true"/"false" strings
			if ("true".equalsIgnoreCase(value)) {
				return trueValue;
			}
			if ("false".equalsIgnoreCase(value)) {
				return falseValue;
			}

			// Try to parse as enum name (case-insensitive, supports underscores)
			try {
				return Enum.valueOf(enumClass, value.toUpperCase());
			} catch (IllegalArgumentException ignored) {
				return defaultValue;
			}
		}

		return defaultValue;
	}
}

