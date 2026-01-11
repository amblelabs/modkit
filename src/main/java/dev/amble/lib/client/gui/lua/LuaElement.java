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
		if (element instanceof AmbleTextInput input) {
			input.setText(text);
		} else if (element instanceof AmbleText t) {
			t.setText(Text.literal(text));
		}
	}

	@LuaExpose
	public String getText() {
		if (element instanceof AmbleTextInput input) {
			return input.getText();
		} else if (element instanceof AmbleText t) {
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

	// ===== AmbleTextInput methods =====

	/**
	 * Gets the placeholder text.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @return the placeholder text, or null if not a text input
	 */
	@LuaExpose
	public String getPlaceholder() {
		if (element instanceof AmbleTextInput input) {
			return input.getPlaceholder();
		}
		return null;
	}

	/**
	 * Sets the placeholder text.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @param placeholder the placeholder text
	 */
	@LuaExpose
	public void setPlaceholder(String placeholder) {
		if (element instanceof AmbleTextInput input) {
			input.setPlaceholder(placeholder);
		}
	}

	/**
	 * Gets the maximum text length.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @return the max length, or -1 if not a text input
	 */
	@LuaExpose
	public int getMaxLength() {
		if (element instanceof AmbleTextInput input) {
			return input.getMaxLength();
		}
		return -1;
	}

	/**
	 * Sets the maximum text length.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @param maxLength the max length
	 */
	@LuaExpose
	public void setMaxLength(int maxLength) {
		if (element instanceof AmbleTextInput input) {
			input.setMaxLength(maxLength);
		}
	}

	/**
	 * Checks if the text input is editable.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @return true if editable, false otherwise
	 */
	@LuaExpose
	public boolean isEditable() {
		if (element instanceof AmbleTextInput input) {
			return input.isEditable();
		}
		return false;
	}

	/**
	 * Sets whether the text input is editable.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @param editable whether the input is editable
	 */
	@LuaExpose
	public void setEditable(boolean editable) {
		if (element instanceof AmbleTextInput input) {
			input.setEditable(editable);
		}
	}

	/**
	 * Checks if the text input is focused.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @return true if focused, false otherwise
	 */
	@LuaExpose
	public boolean isInputFocused() {
		if (element instanceof AmbleTextInput input) {
			return input.isFocused();
		}
		return false;
	}

	/**
	 * Sets whether the text input is focused.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @param focused whether the input should be focused
	 */
	@LuaExpose
	public void setInputFocused(boolean focused) {
		if (element instanceof AmbleTextInput input) {
			input.setFocused(focused);
			input.onFocusChanged(focused);
		}
	}

	/**
	 * Gets the selection start position.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @return the selection start, or -1 if not a text input
	 */
	@LuaExpose
	public int getSelectionStart() {
		if (element instanceof AmbleTextInput input) {
			return input.getSelectionStart();
		}
		return -1;
	}

	/**
	 * Gets the selection end position.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @return the selection end, or -1 if not a text input
	 */
	@LuaExpose
	public int getSelectionEnd() {
		if (element instanceof AmbleTextInput input) {
			return input.getSelectionEnd();
		}
		return -1;
	}

	/**
	 * Sets the text selection range.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @param start selection start position
	 * @param end selection end position
	 */
	@LuaExpose
	public void setSelection(int start, int end) {
		if (element instanceof AmbleTextInput input) {
			input.setSelection(start, end);
		}
	}

	/**
	 * Selects all text in the input.
	 * Only works if the underlying element is an AmbleTextInput.
	 */
	@LuaExpose
	public void selectAll() {
		if (element instanceof AmbleTextInput input) {
			input.selectAll();
		}
	}

	/**
	 * Sets the selection color.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @param r red component (0-255)
	 * @param g green component (0-255)
	 * @param b blue component (0-255)
	 * @param a alpha component (0-255)
	 */
	@LuaExpose
	public void setSelectionColor(int r, int g, int b, int a) {
		if (element instanceof AmbleTextInput input) {
			input.setSelectionColor(new java.awt.Color(r, g, b, a));
		}
	}

	/**
	 * Sets the border color.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @param r red component (0-255)
	 * @param g green component (0-255)
	 * @param b blue component (0-255)
	 * @param a alpha component (0-255)
	 */
	@LuaExpose
	public void setBorderColor(int r, int g, int b, int a) {
		if (element instanceof AmbleTextInput input) {
			input.setBorderColor(new java.awt.Color(r, g, b, a));
		}
	}

	/**
	 * Sets the focused border color.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @param r red component (0-255)
	 * @param g green component (0-255)
	 * @param b blue component (0-255)
	 * @param a alpha component (0-255)
	 */
	@LuaExpose
	public void setFocusedBorderColor(int r, int g, int b, int a) {
		if (element instanceof AmbleTextInput input) {
			input.setFocusedBorderColor(new java.awt.Color(r, g, b, a));
		}
	}

	/**
	 * Sets the text color.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @param r red component (0-255)
	 * @param g green component (0-255)
	 * @param b blue component (0-255)
	 * @param a alpha component (0-255)
	 */
	@LuaExpose
	public void setTextColor(int r, int g, int b, int a) {
		if (element instanceof AmbleTextInput input) {
			input.setTextColor(new java.awt.Color(r, g, b, a));
		}
	}

	/**
	 * Sets the placeholder color.
	 * Only works if the underlying element is an AmbleTextInput.
	 *
	 * @param r red component (0-255)
	 * @param g green component (0-255)
	 * @param b blue component (0-255)
	 * @param a alpha component (0-255)
	 */
	@LuaExpose
	public void setPlaceholderColor(int r, int g, int b, int a) {
		if (element instanceof AmbleTextInput input) {
			input.setPlaceholderColor(new java.awt.Color(r, g, b, a));
		}
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

	// ===== AmbleSlider methods =====

	/**
	 * Gets the current slider value.
	 * Only works if the underlying element is an AmbleSlider.
	 *
	 * @return the current value, or 0 if not a slider
	 */
	@LuaExpose
	public float getValue() {
		if (element instanceof AmbleSlider slider) {
			return slider.getValue();
		}
		return 0;
	}

	/**
	 * Sets the slider value.
	 * Only works if the underlying element is an AmbleSlider.
	 *
	 * @param value the value to set (will be clamped to min/max)
	 */
	@LuaExpose
	public void setValue(float value) {
		if (element instanceof AmbleSlider slider) {
			slider.setValue(value);
		}
	}

	/**
	 * Gets the slider minimum value.
	 * Only works if the underlying element is an AmbleSlider.
	 *
	 * @return the minimum value, or 0 if not a slider
	 */
	@LuaExpose
	public float getMin() {
		if (element instanceof AmbleSlider slider) {
			return slider.getMin();
		}
		return 0;
	}

	/**
	 * Sets the slider minimum value.
	 * Only works if the underlying element is an AmbleSlider.
	 *
	 * @param min the minimum value
	 */
	@LuaExpose
	public void setMin(float min) {
		if (element instanceof AmbleSlider slider) {
			slider.setMin(min);
		}
	}

	/**
	 * Gets the slider maximum value.
	 * Only works if the underlying element is an AmbleSlider.
	 *
	 * @return the maximum value, or 0 if not a slider
	 */
	@LuaExpose
	public float getMax() {
		if (element instanceof AmbleSlider slider) {
			return slider.getMax();
		}
		return 0;
	}

	/**
	 * Sets the slider maximum value.
	 * Only works if the underlying element is an AmbleSlider.
	 *
	 * @param max the maximum value
	 */
	@LuaExpose
	public void setMax(float max) {
		if (element instanceof AmbleSlider slider) {
			slider.setMax(max);
		}
	}

	/**
	 * Gets the slider step value.
	 * Only works if the underlying element is an AmbleSlider.
	 *
	 * @return the step value, or 0 if not a slider
	 */
	@LuaExpose
	public float getStep() {
		if (element instanceof AmbleSlider slider) {
			return slider.getStep();
		}
		return 0;
	}

	/**
	 * Sets the slider step value.
	 * Only works if the underlying element is an AmbleSlider.
	 *
	 * @param step the step value (0 = continuous)
	 */
	@LuaExpose
	public void setStep(float step) {
		if (element instanceof AmbleSlider slider) {
			slider.setStep(step);
		}
	}

	// ===== AmbleColorPicker methods =====

	/**
	 * Gets the current color as RGB values.
	 * Only works if the underlying element is an AmbleColorPicker.
	 *
	 * @return array of [r, g, b, a] values (0-255), or null if not a color picker
	 */
	@LuaExpose
	public int[] getColorRGBA() {
		if (element instanceof AmbleColorPicker picker) {
			return new int[] { picker.getRed(), picker.getGreen(), picker.getBlue(), picker.getAlpha() };
		}
		return null;
	}

	/**
	 * Sets the color from RGBA values.
	 * Only works if the underlying element is an AmbleColorPicker.
	 *
	 * @param r red component (0-255)
	 * @param g green component (0-255)
	 * @param b blue component (0-255)
	 * @param a alpha component (0-255)
	 */
	@LuaExpose
	public void setColorRGBA(int r, int g, int b, int a) {
		if (element instanceof AmbleColorPicker picker) {
			picker.setColor(r, g, b, a);
		}
	}

	/**
	 * Gets the current color as a hex string.
	 * Only works if the underlying element is an AmbleColorPicker.
	 *
	 * @return hex string (RRGGBB or RRGGBBAA), or null if not a color picker
	 */
	@LuaExpose
	public String getColorHex() {
		if (element instanceof AmbleColorPicker picker) {
			return picker.getColorHex();
		}
		return null;
	}

	/**
	 * Sets the color from a hex string.
	 * Only works if the underlying element is an AmbleColorPicker.
	 *
	 * @param hex hex string (with or without #, 6 or 8 characters)
	 */
	@LuaExpose
	public void setColorHex(String hex) {
		if (element instanceof AmbleColorPicker picker) {
			picker.setColorHex(hex);
		}
	}

	/**
	 * Checks if the color picker is expanded.
	 * Only works if the underlying element is an AmbleColorPicker.
	 *
	 * @return true if expanded, false otherwise
	 */
	@LuaExpose
	public boolean isPickerExpanded() {
		if (element instanceof AmbleColorPicker picker) {
			return picker.isExpanded();
		}
		return false;
	}

	/**
	 * Sets the expanded state of the color picker.
	 * Only works if the underlying element is an AmbleColorPicker.
	 *
	 * @param expanded true to expand, false to collapse
	 */
	@LuaExpose
	public void setPickerExpanded(boolean expanded) {
		if (element instanceof AmbleColorPicker picker) {
			picker.setExpanded(expanded);
		}
	}

	/**
	 * Checks if the color picker includes alpha support.
	 * Only works if the underlying element is an AmbleColorPicker.
	 *
	 * @return true if alpha is included, false otherwise
	 */
	@LuaExpose
	public boolean isIncludeAlpha() {
		if (element instanceof AmbleColorPicker picker) {
			return picker.isIncludeAlpha();
		}
		return false;
	}

	/**
	 * Sets whether the color picker includes alpha support.
	 * Only works if the underlying element is an AmbleColorPicker.
	 *
	 * @param includeAlpha true to include alpha slider
	 */
	@LuaExpose
	public void setIncludeAlpha(boolean includeAlpha) {
		if (element instanceof AmbleColorPicker picker) {
			picker.setIncludeAlpha(includeAlpha);
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
