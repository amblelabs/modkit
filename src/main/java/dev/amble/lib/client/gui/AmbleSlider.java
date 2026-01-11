package dev.amble.lib.client.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.gui.lua.LuaElement;
import dev.amble.lib.client.gui.registry.AmbleElementParser;
import dev.amble.lib.script.LuaScript;
import dev.amble.lib.script.ScriptManager;
import dev.amble.lib.script.lua.LuaBinder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.awt.*;

/**
 * A slider element that allows users to select a value within a range.
 * <p>
 * Features:
 * <ul>
 *   <li>Horizontal slider with configurable min/max values</li>
 *   <li>Customizable track and thumb colors</li>
 *   <li>Mouse drag support for smooth value adjustment</li>
 *   <li>Keyboard support (arrow keys) when focused</li>
 *   <li>Script callback for value changes</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
public class AmbleSlider extends AmbleContainer implements Focusable {

	// Value properties
	@Setter
	private float min = 0.0f;
	@Setter
	private float max = 1.0f;
	private float value = 0.0f;

	// Focus state
	@Setter
	private boolean focused = false;

	// Visual customization
	@Setter
	private Color trackColor = new Color(60, 60, 60);
	@Setter
	private Color trackFilledColor = new Color(80, 140, 200);
	@Setter
	private Color thumbColor = new Color(200, 200, 200);
	@Setter
	private Color thumbHoverColor = new Color(255, 255, 255);
	@Setter
	private Color borderColor = new Color(100, 100, 100);
	@Setter
	private Color focusedBorderColor = new Color(80, 160, 255);

	@Setter
	private int trackHeight = 4;
	@Setter
	private int thumbWidth = 8;
	@Setter
	private int thumbHeight = 16;

	// Interaction state
	private boolean isDragging = false;
	private boolean isThumbHovered = false;

	// Step for keyboard navigation (0 = continuous)
	@Setter
	private float step = 0.0f;

	// Script support
	@Setter
	private @Nullable LuaScript script;

	/**
	 * Sets the current value, clamping to min/max range.
	 */
	public void setValue(float value) {
		float oldValue = this.value;
		this.value = Math.max(min, Math.min(max, value));
		if (oldValue != this.value) {
			onValueChanged();
		}
	}

	/**
	 * Gets the value as a normalized 0-1 range.
	 */
	public float getNormalizedValue() {
		if (max == min) return 0;
		return (value - min) / (max - min);
	}

	/**
	 * Sets the value from a normalized 0-1 range.
	 */
	public void setNormalizedValue(float normalized) {
		setValue(min + normalized * (max - min));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		// Render background
		getBackground().render(context, getLayout());

		Rectangle layout = getLayout();
		int padding = getPadding();

		// Calculate track area
		int trackX = layout.x + padding;
		int trackY = layout.y + (layout.height - trackHeight) / 2;
		int trackWidth = layout.width - padding * 2;

		// Draw track background
		context.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, trackColor.getRGB());

		// Draw filled portion of track
		int filledWidth = (int) (trackWidth * getNormalizedValue());
		context.fill(trackX, trackY, trackX + filledWidth, trackY + trackHeight, trackFilledColor.getRGB());

		// Calculate thumb position
		int thumbX = trackX + filledWidth - thumbWidth / 2;
		int thumbY = layout.y + (layout.height - thumbHeight) / 2;

		// Clamp thumb within track bounds
		thumbX = Math.max(trackX - thumbWidth / 2, Math.min(trackX + trackWidth - thumbWidth / 2, thumbX));

		// Check if thumb is hovered
		isThumbHovered = mouseX >= thumbX && mouseX <= thumbX + thumbWidth &&
				mouseY >= thumbY && mouseY <= thumbY + thumbHeight;

		// Draw thumb
		Color currentThumbColor = (isThumbHovered || isDragging) ? thumbHoverColor : thumbColor;
		context.fill(thumbX, thumbY, thumbX + thumbWidth, thumbY + thumbHeight, currentThumbColor.getRGB());

		// Draw border
		Color border = focused ? focusedBorderColor : borderColor;
		drawBorder(context, layout, border);

		// Render children
		for (AmbleElement child : getChildren()) {
			if (child.isVisible()) {
				child.render(context, mouseX, mouseY, delta);
			}
		}
	}

	private void drawBorder(DrawContext context, Rectangle layout, Color color) {
		int x = layout.x;
		int y = layout.y;
		int w = layout.width;
		int h = layout.height;
		int c = color.getRGB();

		// Top
		context.fill(x, y, x + w, y + 1, c);
		// Bottom
		context.fill(x, y + h - 1, x + w, y + h, c);
		// Left
		context.fill(x, y, x + 1, y + h, c);
		// Right
		context.fill(x + w - 1, y, x + w, y + h, c);
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button) {
		if (button == 0 && isHovered(mouseX, mouseY)) {
			isDragging = true;
			updateValueFromMouse(mouseX);
		}
		super.onClick(mouseX, mouseY, button);
	}

	@Override
	public void onRelease(double mouseX, double mouseY, int button) {
		if (button == 0) {
			isDragging = false;
		}
		super.onRelease(mouseX, mouseY, button);
	}

	/**
	 * Handles mouse drag for value adjustment.
	 */
	public void onMouseDragged(double mouseX, double mouseY, int button) {
		if (isDragging && button == 0) {
			updateValueFromMouse(mouseX);
		}
	}

	private void updateValueFromMouse(double mouseX) {
		Rectangle layout = getLayout();
		int padding = getPadding();
		int trackX = layout.x + padding;
		int trackWidth = layout.width - padding * 2;

		// Calculate normalized value from mouse position
		float normalized = (float) (mouseX - trackX) / trackWidth;
		normalized = Math.max(0, Math.min(1, normalized));

		// Apply step if set
		if (step > 0) {
			float range = max - min;
			float steps = range / step;
			normalized = Math.round(normalized * steps) / steps;
		}

		setNormalizedValue(normalized);
	}

	@Override
	public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
		if (!focused) return false;

		float increment = step > 0 ? step : (max - min) / 100f;

		// Handle Ctrl for larger steps
		if ((modifiers & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0) {
			increment *= 10;
		}

		switch (keyCode) {
			case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT:
			case org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN:
				setValue(value - increment);
				return true;
			case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT:
			case org.lwjgl.glfw.GLFW.GLFW_KEY_UP:
				setValue(value + increment);
				return true;
			case org.lwjgl.glfw.GLFW.GLFW_KEY_HOME:
				setValue(min);
				return true;
			case org.lwjgl.glfw.GLFW.GLFW_KEY_END:
				setValue(max);
				return true;
		}

		return false;
	}

	@Override
	public boolean onCharTyped(char chr, int modifiers) {
		// Slider doesn't handle character input
		return false;
	}

	@Override
	public boolean canFocus() {
		return true;
	}

	@Override
	public void onFocusChanged(boolean focused) {
		this.focused = focused;
	}

	/**
	 * Called when the value changes.
	 */
	protected void onValueChanged() {
		if (script != null && script.onValueChanged() != null && !script.onValueChanged().isnil()) {
			Varargs args = LuaValue.varargsOf(new LuaValue[]{
					LuaBinder.bind(new LuaElement(this)),
					LuaValue.valueOf(value)
			});

			try {
				script.onValueChanged().invoke(args);
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error invoking onValueChanged script for AmbleSlider {}:", id(), e);
			}
		}
	}

	// ===== Builder =====

	public static Builder sliderBuilder() {
		return new Builder();
	}

	public static class Builder extends AbstractBuilder<AmbleSlider, Builder> {

		@Override
		protected AmbleSlider create() {
			return new AmbleSlider();
		}

		@Override
		protected Builder self() {
			return this;
		}

		public Builder min(float min) {
			container.setMin(min);
			return this;
		}

		public Builder max(float max) {
			container.setMax(max);
			return this;
		}

		public Builder value(float value) {
			container.setValue(value);
			return this;
		}

		public Builder step(float step) {
			container.setStep(step);
			return this;
		}

		public Builder trackColor(Color color) {
			container.setTrackColor(color);
			return this;
		}

		public Builder trackFilledColor(Color color) {
			container.setTrackFilledColor(color);
			return this;
		}

		public Builder thumbColor(Color color) {
			container.setThumbColor(color);
			return this;
		}

		public Builder thumbHoverColor(Color color) {
			container.setThumbHoverColor(color);
			return this;
		}

		public Builder borderColor(Color color) {
			container.setBorderColor(color);
			return this;
		}

		public Builder focusedBorderColor(Color color) {
			container.setFocusedBorderColor(color);
			return this;
		}

		public Builder trackHeight(int height) {
			container.setTrackHeight(height);
			return this;
		}

		public Builder thumbWidth(int width) {
			container.setThumbWidth(width);
			return this;
		}

		public Builder thumbHeight(int height) {
			container.setThumbHeight(height);
			return this;
		}

		public Builder script(LuaScript script) {
			container.setScript(script);
			return this;
		}
	}

	// ===== Parser =====

	/**
	 * Parser for AmbleSlider elements.
	 * <p>
	 * This parser handles JSON objects that have the "slider" property set to true.
	 * <p>
	 * Supported JSON properties:
	 * <ul>
	 *   <li>{@code slider} - Boolean, must be true to create a slider</li>
	 *   <li>{@code min} - Float minimum value (default: 0)</li>
	 *   <li>{@code max} - Float maximum value (default: 1)</li>
	 *   <li>{@code value} - Float initial value (default: 0)</li>
	 *   <li>{@code step} - Float step increment for snapping (default: 0 = continuous)</li>
	 *   <li>{@code track_color} - Color array [r,g,b] or [r,g,b,a]</li>
	 *   <li>{@code track_filled_color} - Color array for filled portion</li>
	 *   <li>{@code thumb_color} - Color array for thumb</li>
	 *   <li>{@code thumb_hover_color} - Color array for hovered thumb</li>
	 *   <li>{@code border_color} - Color array for border</li>
	 *   <li>{@code focused_border_color} - Color array for focused border</li>
	 *   <li>{@code track_height} - Integer height of track in pixels</li>
	 *   <li>{@code thumb_width} - Integer width of thumb in pixels</li>
	 *   <li>{@code thumb_height} - Integer height of thumb in pixels</li>
	 *   <li>{@code script} - Script ID for event handling</li>
	 * </ul>
	 */
	public static class Parser implements AmbleElementParser {

		@Override
		public @Nullable AmbleContainer parse(JsonObject json, @Nullable Identifier resourceId, AmbleContainer base) {
			if (!json.has("slider") || !json.get("slider").getAsBoolean()) {
				return null;
			}

			AmbleSlider slider = AmbleSlider.sliderBuilder().build();
			slider.copyFrom(base);

			// Parse min/max/value
			if (json.has("min")) {
				slider.setMin(json.get("min").getAsFloat());
			}
			if (json.has("max")) {
				slider.setMax(json.get("max").getAsFloat());
			}
			if (json.has("value")) {
				slider.setValue(json.get("value").getAsFloat());
			}
			if (json.has("step")) {
				slider.setStep(json.get("step").getAsFloat());
			}

			// Parse colors
			if (json.has("track_color")) {
				slider.setTrackColor(parseColor(json.get("track_color").getAsJsonArray()));
			}
			if (json.has("track_filled_color")) {
				slider.setTrackFilledColor(parseColor(json.get("track_filled_color").getAsJsonArray()));
			}
			if (json.has("thumb_color")) {
				slider.setThumbColor(parseColor(json.get("thumb_color").getAsJsonArray()));
			}
			if (json.has("thumb_hover_color")) {
				slider.setThumbHoverColor(parseColor(json.get("thumb_hover_color").getAsJsonArray()));
			}
			if (json.has("border_color")) {
				slider.setBorderColor(parseColor(json.get("border_color").getAsJsonArray()));
			}
			if (json.has("focused_border_color")) {
				slider.setFocusedBorderColor(parseColor(json.get("focused_border_color").getAsJsonArray()));
			}

			// Parse dimensions
			if (json.has("track_height")) {
				slider.setTrackHeight(json.get("track_height").getAsInt());
			}
			if (json.has("thumb_width")) {
				slider.setThumbWidth(json.get("thumb_width").getAsInt());
			}
			if (json.has("thumb_height")) {
				slider.setThumbHeight(json.get("thumb_height").getAsInt());
			}

			// Parse script
			if (json.has("script")) {
				Identifier scriptId = new Identifier(json.get("script").getAsString())
						.withPrefixedPath("script/")
						.withSuffixedPath(".lua");
				LuaScript script = ScriptManager.getInstance().load(
						scriptId,
						MinecraftClient.getInstance().getResourceManager()
				);
				slider.setScript(script);
			}

			return slider;
		}

		private Color parseColor(JsonArray colorArray) {
			int r = colorArray.get(0).getAsInt();
			int g = colorArray.get(1).getAsInt();
			int b = colorArray.get(2).getAsInt();
			int a = colorArray.size() > 3 ? colorArray.get(3).getAsInt() : 255;
			return new Color(r, g, b, a);
		}

		@Override
		public int priority() {
			return 85;
		}
	}
}

