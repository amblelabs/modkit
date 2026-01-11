package dev.amble.lib.client.gui.lua;

import dev.amble.lib.client.gui.*;
import dev.amble.lib.script.lua.ClientMinecraftData;
import dev.amble.lib.script.lua.LuaExpose;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

/**
 * A Lua-friendly wrapper around {@link AmbleElement}.
 * <p>
 * This class implements AmbleElement by delegating to a wrapped element,
 * while also exposing a simplified API for Lua scripts.
 * <ul>
 *   <li>It provides only the methods that make sense for Lua scripting via @LuaExpose</li>
 *   <li>It converts Java types to Lua-compatible return values</li>
 *   <li>It implements AmbleElement by delegating to the wrapped element</li>
 * </ul>
 */
public final class LuaElement implements AmbleElement {

	private final AmbleElement element;
	private final ClientMinecraftData minecraftData = new ClientMinecraftData();

	public LuaElement(AmbleElement element) {
		this.element = element;
	}

	// ===== AmbleElement implementation (delegating to wrapped element) =====

	@LuaExpose
	@Override
	public Identifier id() {
		return element.id();
	}

	@LuaExpose
	@Override
	public boolean isVisible() {
		return element.isVisible();
	}

	@Override
	public Rectangle getLayout() {
		return element.getLayout();
	}

	@Override
	public void setLayout(Rectangle layout) {
		element.setLayout(layout);
	}

	@Override
	public Rectangle getPreferredLayout() {
		return element.getPreferredLayout();
	}

	@Override
	public void setPreferredLayout(Rectangle preferredLayout) {
		element.setPreferredLayout(preferredLayout);
	}

	@Override
	public @Nullable AmbleElement getParent() {
		return element.getParent();
	}

	@Override
	public void setParent(@Nullable AmbleElement parent) {
		element.setParent(parent);
	}

	@Override
	public int getPadding() {
		return element.getPadding();
	}

	@Override
	public void setPadding(int padding) {
		element.setPadding(padding);
	}

	@Override
	public int getSpacing() {
		return element.getSpacing();
	}

	@Override
	public void setSpacing(int spacing) {
		element.setSpacing(spacing);
	}

	@Override
	public UIAlign getHorizontalAlign() {
		return element.getHorizontalAlign();
	}

	@Override
	public void setHorizontalAlign(UIAlign align) {
		element.setHorizontalAlign(align);
	}

	@Override
	public UIAlign getVerticalAlign() {
		return element.getVerticalAlign();
	}

	@Override
	public void setVerticalAlign(UIAlign align) {
		element.setVerticalAlign(align);
	}

	@Override
	public boolean requiresNewRow() {
		return element.requiresNewRow();
	}

	@Override
	public void setRequiresNewRow(boolean requiresNewRow) {
		element.setRequiresNewRow(requiresNewRow);
	}

	@Override
	public List<AmbleElement> getChildren() {
		return element.getChildren();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		element.render(context, mouseX, mouseY, delta);
	}

	// ===== Lua-exposed methods =====

	@LuaExpose
	public int x() {
		return element.getLayout().x;
	}

	@LuaExpose
	public int y() {
		return element.getLayout().y;
	}

	@LuaExpose
	public int width() {
		return element.getLayout().width;
	}

	@LuaExpose
	public int height() {
		return element.getLayout().height;
	}

	@LuaExpose
	public void setPosition(int x, int y) {
		element.setPosition(new Vec2f(x, y));
	}

	@LuaExpose
	public void setDimensions(int width, int height) {
		element.setDimensions(new Vec2f(width, height));
	}

	@Override
	@LuaExpose
	public void setVisible(boolean visible) {
		element.setVisible(visible);
	}

	@LuaExpose
	public AmbleElement parent() {
		return element.getParent();
	}

	@LuaExpose
	public AmbleElement child(int index) {
		if (index < 0 || index >= element.getChildren().size()) return null;
		return element.getChildren().get(index);
	}

	@LuaExpose
	public int childCount() {
		return element.getChildren().size();
	}

	@LuaExpose
	public AmbleText findFirstText() {
		return findFirstTextRecursive(element);
	}

	private static AmbleText findFirstTextRecursive(AmbleElement element) {
		if (element instanceof AmbleText text) {
			return text;
		}
		for (AmbleElement child : element.getChildren()) {
			AmbleText found = findFirstTextRecursive(child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	@LuaExpose
	public void setText(String text) {
		if (element instanceof AmbleText t) {
			t.setText(Text.literal(text));
		}
	}

	@LuaExpose
	public String getText() {
		if (element instanceof AmbleText t) {
			return t.getText().getString();
		}
		return null;
	}

	@LuaExpose
	public void closeScreen() {
		MinecraftClient.getInstance().setScreen(null);
	}

	@LuaExpose
	public ClientMinecraftData minecraft() {
		return minecraftData;
	}

	// ===== AmbleEntityDisplay methods =====

	/**
	 * Gets the entity UUID as a string.
	 * Only works if the underlying element is an AmbleEntityDisplay.
	 *
	 * @return the UUID string, or null if not an entity display or no UUID set
	 */
	@LuaExpose
	public String getEntityUuid() {
		if (element instanceof AmbleEntityDisplay display) {
			return display.getEntityUuidAsString();
		}
		return null;
	}

	/**
	 * Sets the entity UUID from a string.
	 * Only works if the underlying element is an AmbleEntityDisplay.
	 * Accepts a UUID string or "player" for the local player.
	 *
	 * @param uuid the UUID string, or "player" for local player
	 */
	@LuaExpose
	public void setEntityUuid(String uuid) {
		if (element instanceof AmbleEntityDisplay display) {
			display.setEntityUuidFromString(uuid);
		}
	}

	/**
	 * Checks if the entity display follows the cursor.
	 * Only works if the underlying element is an AmbleEntityDisplay.
	 *
	 * @return true if following cursor, false otherwise (or if not an entity display)
	 */
	@LuaExpose
	public boolean isFollowCursor() {
		if (element instanceof AmbleEntityDisplay display) {
			return display.isFollowCursor();
		}
		return false;
	}

	/**
	 * Sets whether the entity display should follow the cursor.
	 * Only works if the underlying element is an AmbleEntityDisplay.
	 *
	 * @param followCursor true to follow cursor, false to use fixed look-at position
	 */
	@LuaExpose
	public void setFollowCursor(boolean followCursor) {
		if (element instanceof AmbleEntityDisplay display) {
			display.setFollowCursor(followCursor);
		}
	}

	/**
	 * Sets the fixed look-at position for the entity display.
	 * Only works if the underlying element is an AmbleEntityDisplay.
	 * Coordinates are relative to the element's position.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	@LuaExpose
	public void setLookAt(int x, int y) {
		if (element instanceof AmbleEntityDisplay display) {
			display.setFixedLookAt(new Vec2f(x, y));
		}
	}

	/**
	 * Sets the entity scale for the entity display.
	 * Only works if the underlying element is an AmbleEntityDisplay.
	 *
	 * @param scale the scale multiplier (1.0 = normal size)
	 */
	@LuaExpose
	public void setEntityScale(float scale) {
		if (element instanceof AmbleEntityDisplay display) {
			display.setEntityScale(scale);
		}
	}

	/**
	 * Returns the underlying AmbleElement wrapped by this LuaElement.
	 * This method is for internal use only and should not be called from Lua scripts.
	 *
	 * @return the wrapped AmbleElement
	 */
	@ApiStatus.Internal
	AmbleElement unwrap() {
		return element;
	}
}
