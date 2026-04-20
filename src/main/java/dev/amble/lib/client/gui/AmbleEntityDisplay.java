package dev.amble.lib.client.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.amble.lib.client.gui.registry.AmbleElementParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.UUID;

/**
 * A GUI element that displays an entity within a rectangular area.
 * <p>
 * The entity is rendered using Minecraft's inventory-style entity rendering.
 * Supports dynamic entity lookup by UUID, cursor-following for entity rotation,
 * and fixed look-at positions.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AmbleEntityDisplay extends AmbleContainer {
	/**
	 * Special marker UUID for "use local player". All zeros.
	 */
	private static final UUID PLAYER_MARKER_UUID = new UUID(0L, 0L);

	/**
	 * The UUID of the entity to display. If null, displays "N/A".
	 * If set to PLAYER_MARKER_UUID, uses the local player.
	 */
	private @Nullable UUID entityUuid;

	/**
	 * Whether the entity should rotate to follow the mouse cursor.
	 * If false, uses {@link #fixedLookAt} position instead.
	 */
	@Setter
	private boolean followCursor = false;

	/**
	 * The fixed position the entity should look at when {@link #followCursor} is false.
	 * Coordinates are relative to the element's position.
	 * Defaults to center of the element if not set.
	 */
	@Setter
	private @Nullable Vec2f fixedLookAt = null;

	/**
	 * Scale multiplier for the entity rendering.
	 */
	@Setter
	private float entityScale = 1.0f;

	// Entity cache
	private transient @Nullable UUID cachedUuid = null;
	private transient @Nullable LivingEntity cachedEntity = null;

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		Rectangle layout = getLayout();

		// Try to find the entity (using cache)
		LivingEntity livingEntity = findLivingEntity();

		if (livingEntity == null) {
			// Render "N/A" text centered in the rectangle
			renderNoEntity(context, layout);
			return;
		}

		// Calculate rendering parameters
		int centerX = layout.x + layout.width / 2;
		int bottomY = layout.y + layout.height - getPadding() - 5; // Offset from bottom

		// Calculate the look-at position relative to entity center
		float lookAtX, lookAtY;
		if (followCursor) {
			lookAtX = centerX - mouseX;
			lookAtY = (layout.y + layout.height / 3.0f) - mouseY;
		} else if (fixedLookAt != null) {
			lookAtX = centerX - (layout.x + fixedLookAt.x);
			lookAtY = (layout.y + layout.height / 3.0f) - (layout.y + fixedLookAt.y);
		} else {
			// Default: look straight ahead
			lookAtX = 0;
			lookAtY = 0;
		}

		// Calculate entity size to fit in the rectangle with a small margin
		float entityHeight = livingEntity.getHeight();
		int availableHeight = layout.height - getPadding() * 2;
		int size = (int) ((availableHeight / entityHeight) * (entityScale - 0.1f));

		// Use Minecraft's built-in entity rendering with mouse-based rotation
		InventoryScreen.drawEntity(context, centerX, bottomY, size, lookAtX, lookAtY, livingEntity);
	}

	/**
	 * Renders "N/A" text when no entity is available.
	 */
	private void renderNoEntity(DrawContext context, Rectangle layout) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		String text = "N/A";
		int textWidth = textRenderer.getWidth(text);
		int textX = layout.x + (layout.width - textWidth) / 2;
		int textY = layout.y + (layout.height - textRenderer.fontHeight) / 2;
		context.drawText(textRenderer, text, textX, textY, 0xAAAAAA, false);
	}

	/**
	 * Finds the living entity in the world by UUID, using cache for efficiency.
	 *
	 * @return the living entity, or null if not found or not a LivingEntity
	 */
	private @Nullable LivingEntity findLivingEntity() {
		if (entityUuid == null) {
			cachedEntity = null;
			cachedUuid = null;
			return null;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) {
			return null;
		}

		// Handle special "player" marker UUID
		UUID lookupUuid = entityUuid;
		if (PLAYER_MARKER_UUID.equals(entityUuid)) {
			if (client.player != null) {
				lookupUuid = client.player.getUuid();
			} else {
				return null;
			}
		}

		// Check if cache is valid
		if (lookupUuid.equals(cachedUuid) && cachedEntity != null && cachedEntity.isAlive()) {
			return cachedEntity;
		}

		// Cache miss - look up entity
		cachedUuid = lookupUuid;
		cachedEntity = null;

		// First try to find as player (more efficient)
		Entity player = client.world.getPlayerByUuid(lookupUuid);
		if (player instanceof LivingEntity living) {
			cachedEntity = living;
			return living;
		}

		// Search through all entities
		for (Entity entity : client.world.getEntities()) {
			if (lookupUuid.equals(entity.getUuid()) && entity instanceof LivingEntity living) {
				cachedEntity = living;
				return living;
			}
		}

		return null;
	}

	/**
	 * Invalidates the entity cache, forcing a re-lookup on next render.
	 */
	public void invalidateEntityCache() {
		cachedEntity = null;
		cachedUuid = null;
	}

	/**
	 * Sets the entity UUID. Also invalidates the cache.
	 */
	public void setEntityUuid(@Nullable UUID entityUuid) {
		if (!java.util.Objects.equals(this.entityUuid, entityUuid)) {
			this.entityUuid = entityUuid;
			invalidateEntityCache();
		}
	}

	/**
	 * Sets the entity UUID from a string.
	 *
	 * @param uuidString the UUID string, or "player" for the local player
	 */
	public void setEntityUuidFromString(@Nullable String uuidString) {
		if (uuidString == null || uuidString.isEmpty()) {
			setEntityUuid(null);
			return;
		}

		if ("player".equalsIgnoreCase(uuidString)) {
			// Use special marker that will be resolved at render time
			setEntityUuid(PLAYER_MARKER_UUID);
			return;
		}

		try {
			setEntityUuid(UUID.fromString(uuidString));
		} catch (IllegalArgumentException e) {
			setEntityUuid(null);
		}
	}

	/**
	 * Gets the entity UUID as a string.
	 *
	 * @return the UUID string, "player" for local player marker, or null if not set
	 */
	public @Nullable String getEntityUuidAsString() {
		if (entityUuid == null) {
			return null;
		}
		if (PLAYER_MARKER_UUID.equals(entityUuid)) {
			return "player";
		}
		return entityUuid.toString();
	}

	public static Builder entityDisplayBuilder() {
		return new Builder();
	}

	public static class Builder extends AbstractBuilder<AmbleEntityDisplay, Builder> {

		@Override
		protected AmbleEntityDisplay create() {
			return new AmbleEntityDisplay();
		}

		@Override
		protected Builder self() {
			return this;
		}

		public Builder entityUuid(UUID uuid) {
			container.setEntityUuid(uuid);
			return this;
		}

		public Builder entityUuid(String uuidString) {
			container.setEntityUuidFromString(uuidString);
			return this;
		}

		public Builder followCursor(boolean followCursor) {
			container.setFollowCursor(followCursor);
			return this;
		}

		public Builder fixedLookAt(Vec2f lookAt) {
			container.setFixedLookAt(lookAt);
			return this;
		}

		public Builder fixedLookAt(float x, float y) {
			container.setFixedLookAt(new Vec2f(x, y));
			return this;
		}

		public Builder entityScale(float scale) {
			container.setEntityScale(scale);
			return this;
		}
	}

	/**
	 * Parser for AmbleEntityDisplay elements.
	 * <p>
	 * This parser handles JSON objects that have the "entity_uuid" property.
	 * <p>
	 * Supported JSON properties:
	 * <ul>
	 *   <li>{@code entity_uuid} - String UUID or "player" for local player</li>
	 *   <li>{@code follow_cursor} - Boolean, whether entity follows mouse (default: false)</li>
	 *   <li>{@code look_at} - Array [x, y] for fixed look position relative to element</li>
	 *   <li>{@code entity_scale} - Float scale multiplier (default: 1.0)</li>
	 * </ul>
	 */
	public static class Parser implements AmbleElementParser {

		@Override
		public @Nullable AmbleContainer parse(JsonObject json, @Nullable Identifier resourceId, AmbleContainer base) {
			if (!json.has("entity_uuid")) {
				return null;
			}

			AmbleEntityDisplay display = AmbleEntityDisplay.entityDisplayBuilder().build();
			display.copyFrom(base);

			// Parse entity UUID
			String uuidString = json.get("entity_uuid").getAsString();
			display.setEntityUuidFromString(uuidString);

			// Parse follow_cursor
			if (json.has("follow_cursor")) {
				display.setFollowCursor(json.get("follow_cursor").getAsBoolean());
			}

			// Parse look_at position
			if (json.has("look_at")) {
				if (json.get("look_at").isJsonArray()) {
					JsonArray lookAtArray = json.get("look_at").getAsJsonArray();
					if (lookAtArray.size() >= 2) {
						float x = lookAtArray.get(0).getAsFloat();
						float y = lookAtArray.get(1).getAsFloat();
						display.setFixedLookAt(new Vec2f(x, y));
					}
				}
			}

			// Parse entity_scale
			if (json.has("entity_scale")) {
				display.setEntityScale(json.get("entity_scale").getAsFloat());
			}

			return display;
		}

		@Override
		public int priority() {
			// Higher than text (50), lower than button (100)
			return 75;
		}
	}
}

