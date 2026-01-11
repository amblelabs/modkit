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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.awt.*;

/**
 * A color picker element with collapsed swatch and expanded picker overlay.
 * <p>
 * Features:
 * <ul>
 *   <li>Collapsed state shows a color swatch with configurable border</li>
 *   <li>Expanded state overlays a popup with hue bar, SV square, and input fields</li>
 *   <li>Hex input field (#RRGGBB or #RRGGBBAA format)</li>
 *   <li>RGB numeric input fields (auto-clamp 0-255)</li>
 *   <li>Optional alpha slider</li>
 *   <li>Script callback for color changes</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
public class AmbleColorPicker extends AmbleContainer implements Focusable {

	// Current color (stored as RGBA)
	private int red = 255;
	private int green = 255;
	private int blue = 255;
	private int alpha = 255;

	// HSV representation for the picker
	private float hue = 0f;
	private float saturation = 0f;
	private float brightness = 1f;

	// Expanded state
	@Setter
	private boolean expanded = false;

	// Focus state
	@Setter
	private boolean focused = false;

	// Alpha support
	@Setter
	private boolean includeAlpha = false;

	// Visual customization - collapsed state
	@Setter
	private Color borderCollapsed = new Color(100, 100, 100);
	@Setter
	private Color borderCollapsedHover = new Color(150, 150, 150);

	// Visual customization - expanded state
	@Setter
	private Color borderExpanded = new Color(60, 60, 60);
	@Setter
	private Color backgroundExpanded = new Color(40, 40, 50, 240);

	// Popup dimensions
	@Setter
	private int popupWidth = 180;
	@Setter
	private int popupHeight = 150;

	// Interaction state
	private boolean isHovered = false;
	private boolean isDraggingHue = false;
	private boolean isDraggingSV = false;
	private boolean isDraggingAlpha = false;

	// Input field state (simplified - we'll render inline)
	private String hexInputText = "FFFFFF";
	private String rInputText = "255";
	private String gInputText = "255";
	private String bInputText = "255";
	private String aInputText = "255";

	// Which input is focused (0=none, 1=hex, 2=r, 3=g, 4=b, 5=a)
	private int focusedInput = 0;
	private int cursorPosition = 0;
	private long lastCursorBlink = 0;
	private boolean cursorVisible = true;

	// Layout constants
	private static final int HUE_BAR_WIDTH = 16;
	private static final int ALPHA_BAR_HEIGHT = 12;
	private static final int INPUT_HEIGHT = 14;
	private static final int LABEL_WIDTH = 14;
	private static final int CURSOR_BLINK_RATE = 530;

	// Script support
	@Setter
	private @Nullable LuaScript script;

	/**
	 * Sets the color from RGBA values (0-255 each).
	 */
	public void setColor(int r, int g, int b, int a) {
		this.red = clamp(r, 0, 255);
		this.green = clamp(g, 0, 255);
		this.blue = clamp(b, 0, 255);
		this.alpha = clamp(a, 0, 255);
		updateHSVFromRGB();
		updateInputTexts();
		onColorChanged();
	}

	/**
	 * Sets the color from RGBA values (0-255 each) without triggering callback.
	 */
	private void setColorInternal(int r, int g, int b, int a) {
		this.red = clamp(r, 0, 255);
		this.green = clamp(g, 0, 255);
		this.blue = clamp(b, 0, 255);
		this.alpha = clamp(a, 0, 255);
		updateHSVFromRGB();
		updateInputTexts();
	}

	/**
	 * Sets the color from a hex string (with or without #, with or without alpha).
	 */
	public void setColorHex(String hex) {
		hex = hex.replace("#", "");
		try {
			if (hex.length() == 6) {
				int r = Integer.parseInt(hex.substring(0, 2), 16);
				int g = Integer.parseInt(hex.substring(2, 4), 16);
				int b = Integer.parseInt(hex.substring(4, 6), 16);
				setColor(r, g, b, alpha);
			} else if (hex.length() == 8) {
				int r = Integer.parseInt(hex.substring(0, 2), 16);
				int g = Integer.parseInt(hex.substring(2, 4), 16);
				int b = Integer.parseInt(hex.substring(4, 6), 16);
				int a = Integer.parseInt(hex.substring(6, 8), 16);
				setColor(r, g, b, a);
			}
		} catch (NumberFormatException e) {
			// Invalid hex, ignore
		}
	}

	/**
	 * Gets the current color as a hex string.
	 */
	public String getColorHex() {
		if (includeAlpha) {
			return String.format("%02X%02X%02X%02X", red, green, blue, alpha);
		}
		return String.format("%02X%02X%02X", red, green, blue);
	}

	/**
	 * Gets the current color as a java.awt.Color.
	 */
	public Color getColor() {
		return new Color(red, green, blue, alpha);
	}

	private void updateHSVFromRGB() {
		float[] hsv = Color.RGBtoHSB(red, green, blue, null);
		this.hue = hsv[0];
		this.saturation = hsv[1];
		this.brightness = hsv[2];
	}

	private void updateRGBFromHSV() {
		int rgb = Color.HSBtoRGB(hue, saturation, brightness);
		this.red = (rgb >> 16) & 0xFF;
		this.green = (rgb >> 8) & 0xFF;
		this.blue = rgb & 0xFF;
		updateInputTexts();
	}

	private void updateInputTexts() {
		hexInputText = String.format("%02X%02X%02X", red, green, blue);
		rInputText = String.valueOf(red);
		gInputText = String.valueOf(green);
		bInputText = String.valueOf(blue);
		aInputText = String.valueOf(alpha);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		Rectangle layout = getLayout();
		isHovered = isHovered(mouseX, mouseY);

		// Render collapsed swatch
		renderCollapsedSwatch(context, layout, mouseX, mouseY);

		// Render expanded popup if open
		if (expanded) {
			renderExpandedPopup(context, layout, mouseX, mouseY, delta);
		}

		// Render children
		for (AmbleElement child : getChildren()) {
			if (child.isVisible()) {
				child.render(context, mouseX, mouseY, delta);
			}
		}
	}

	private void renderCollapsedSwatch(DrawContext context, Rectangle layout, int mouseX, int mouseY) {
		// Draw color swatch
		context.fill(layout.x + 1, layout.y + 1, layout.x + layout.width - 1, layout.y + layout.height - 1,
				new Color(red, green, blue, alpha).getRGB());

		// Draw checkerboard pattern behind if alpha < 255 (to show transparency)
		if (alpha < 255) {
			renderCheckerboard(context, layout.x + 1, layout.y + 1, layout.width - 2, layout.height - 2);
			// Re-draw color with alpha
			context.fill(layout.x + 1, layout.y + 1, layout.x + layout.width - 1, layout.y + layout.height - 1,
					new Color(red, green, blue, alpha).getRGB());
		}

		// Draw border
		Color border = (isHovered || expanded) ? borderCollapsedHover : borderCollapsed;
		drawBorder(context, layout.x, layout.y, layout.width, layout.height, border);
	}

	private void renderExpandedPopup(DrawContext context, Rectangle layout, int mouseX, int mouseY, float delta) {
		// Calculate popup position (below the swatch)
		int popupX = layout.x;
		int popupY = layout.y + layout.height + 2;

		// Calculate actual popup height based on whether alpha is included
		int actualPopupHeight = popupHeight + (includeAlpha ? ALPHA_BAR_HEIGHT + 4 : 0);

		// Draw popup background
		context.fill(popupX, popupY, popupX + popupWidth, popupY + actualPopupHeight, backgroundExpanded.getRGB());
		drawBorder(context, popupX, popupY, popupWidth, actualPopupHeight, borderExpanded);

		int padding = 4;
		int innerX = popupX + padding;
		int innerY = popupY + padding;
		int innerWidth = popupWidth - padding * 2;

		// Calculate SV square dimensions
		int svSize = innerWidth - HUE_BAR_WIDTH - 4;
		int svX = innerX;
		int svY = innerY;

		// Render SV square
		renderSVSquare(context, svX, svY, svSize, mouseX, mouseY);

		// Render hue bar
		int hueX = svX + svSize + 4;
		int hueY = innerY;
		int hueHeight = svSize;
		renderHueBar(context, hueX, hueY, HUE_BAR_WIDTH, hueHeight, mouseX, mouseY);

		// Calculate input area Y position
		int inputY = svY + svSize + 4;

		// Render alpha bar if enabled
		if (includeAlpha) {
			renderAlphaBar(context, innerX, inputY, innerWidth, ALPHA_BAR_HEIGHT, mouseX, mouseY);
			inputY += ALPHA_BAR_HEIGHT + 4;
		}

		// Render input fields
		renderInputFields(context, innerX, inputY, innerWidth, mouseX, mouseY, delta);
	}

	private void renderSVSquare(DrawContext context, int x, int y, int size, int mouseX, int mouseY) {
		// Draw the SV gradient
		for (int py = 0; py < size; py++) {
			for (int px = 0; px < size; px++) {
				float s = (float) px / size;
				float v = 1.0f - (float) py / size;
				int rgb = Color.HSBtoRGB(hue, s, v);
				context.fill(x + px, y + py, x + px + 1, y + py + 1, rgb | 0xFF000000);
			}
		}

		// Draw border
		drawBorder(context, x, y, size, size, new Color(80, 80, 80));

		// Draw crosshair at current position
		int crossX = x + (int) (saturation * size);
		int crossY = y + (int) ((1 - brightness) * size);
		int crossSize = 4;

		// White outline
		context.fill(crossX - crossSize - 1, crossY, crossX + crossSize + 2, crossY + 1, 0xFFFFFFFF);
		context.fill(crossX, crossY - crossSize - 1, crossX + 1, crossY + crossSize + 2, 0xFFFFFFFF);
		// Black center
		context.fill(crossX - crossSize, crossY, crossX + crossSize + 1, crossY + 1, 0xFF000000);
		context.fill(crossX, crossY - crossSize, crossX + 1, crossY + crossSize + 1, 0xFF000000);
	}

	private void renderHueBar(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
		// Draw hue gradient (vertical rainbow)
		for (int py = 0; py < height; py++) {
			float h = (float) py / height;
			int rgb = Color.HSBtoRGB(h, 1f, 1f);
			context.fill(x, y + py, x + width, y + py + 1, rgb | 0xFF000000);
		}

		// Draw border
		drawBorder(context, x, y, width, height, new Color(80, 80, 80));

		// Draw selector at current hue
		int selectorY = y + (int) (hue * height);
		context.fill(x - 1, selectorY - 1, x + width + 1, selectorY + 2, 0xFFFFFFFF);
		context.fill(x, selectorY, x + width, selectorY + 1, 0xFF000000);
	}

	private void renderAlphaBar(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
		// Draw checkerboard background
		renderCheckerboard(context, x, y, width, height);

		// Draw alpha gradient
		for (int px = 0; px < width; px++) {
			int a = (int) ((float) px / width * 255);
			int color = (a << 24) | (red << 16) | (green << 8) | blue;
			context.fill(x + px, y, x + px + 1, y + height, color);
		}

		// Draw border
		drawBorder(context, x, y, width, height, new Color(80, 80, 80));

		// Draw selector at current alpha
		int selectorX = x + (int) ((float) alpha / 255 * width);
		context.fill(selectorX - 1, y - 1, selectorX + 2, y + height + 1, 0xFFFFFFFF);
		context.fill(selectorX, y, selectorX + 1, y + height, 0xFF000000);
	}

	private void renderCheckerboard(DrawContext context, int x, int y, int width, int height) {
		int checkSize = 4;
		for (int py = 0; py < height; py += checkSize) {
			for (int px = 0; px < width; px += checkSize) {
				boolean isLight = ((px / checkSize) + (py / checkSize)) % 2 == 0;
				int color = isLight ? 0xFFCCCCCC : 0xFF999999;
				int x2 = Math.min(x + px + checkSize, x + width);
				int y2 = Math.min(y + py + checkSize, y + height);
				context.fill(x + px, y + py, x2, y2, color);
			}
		}
	}

	private void renderInputFields(DrawContext context, int x, int y, int width, int mouseX, int mouseY, float delta) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int fieldHeight = INPUT_HEIGHT;
		int spacing = 2;

		// Update cursor blink
		long now = System.currentTimeMillis();
		if (now - lastCursorBlink > CURSOR_BLINK_RATE) {
			cursorVisible = !cursorVisible;
			lastCursorBlink = now;
		}

		// Hex input (top row)
		int hexWidth = width;
		renderInputField(context, textRenderer, x, y, hexWidth, fieldHeight, "#", hexInputText, focusedInput == 1, mouseX, mouseY);

		// RGB inputs (bottom row)
		int rgbY = y + fieldHeight + spacing;
		int fieldWidth = (width - spacing * 2 - (includeAlpha ? spacing : 0)) / (includeAlpha ? 4 : 3);

		renderInputField(context, textRenderer, x, rgbY, fieldWidth, fieldHeight, "R", rInputText, focusedInput == 2, mouseX, mouseY);
		renderInputField(context, textRenderer, x + fieldWidth + spacing, rgbY, fieldWidth, fieldHeight, "G", gInputText, focusedInput == 3, mouseX, mouseY);
		renderInputField(context, textRenderer, x + (fieldWidth + spacing) * 2, rgbY, fieldWidth, fieldHeight, "B", bInputText, focusedInput == 4, mouseX, mouseY);

		if (includeAlpha) {
			renderInputField(context, textRenderer, x + (fieldWidth + spacing) * 3, rgbY, fieldWidth, fieldHeight, "A", aInputText, focusedInput == 5, mouseX, mouseY);
		}
	}

	private void renderInputField(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height,
								  String label, String text, boolean isFocused, int mouseX, int mouseY) {
		// Background
		context.fill(x, y, x + width, y + height, isFocused ? 0xFF303040 : 0xFF252530);

		// Border
		Color borderColor = isFocused ? new Color(80, 140, 200) : new Color(60, 60, 70);
		drawBorder(context, x, y, width, height, borderColor);

		// Label
		int labelX = x + 2;
		int textY = y + (height - textRenderer.fontHeight) / 2;
		context.drawText(textRenderer, label, labelX, textY, 0xFF808090, false);

		// Text
		int textX = labelX + textRenderer.getWidth(label) + 2;
		int maxTextWidth = width - (textX - x) - 2;

		// Scissor to prevent text overflow
		context.enableScissor(textX, y, x + width - 2, y + height);
		context.drawText(textRenderer, text, textX, textY, 0xFFFFFFFF, false);

		// Cursor
		if (isFocused && cursorVisible) {
			int cursorX = textX + textRenderer.getWidth(text.substring(0, Math.min(cursorPosition, text.length())));
			context.fill(cursorX, textY - 1, cursorX + 1, textY + textRenderer.fontHeight + 1, 0xFFFFFFFF);
		}

		context.disableScissor();
	}

	private void drawBorder(DrawContext context, int x, int y, int w, int h, Color color) {
		int c = color.getRGB();
		context.fill(x, y, x + w, y + 1, c);
		context.fill(x, y + h - 1, x + w, y + h, c);
		context.fill(x, y, x + 1, y + h, c);
		context.fill(x + w - 1, y, x + w, y + h, c);
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button) {
		if (button != 0) {
			super.onClick(mouseX, mouseY, button);
			return;
		}

		Rectangle layout = getLayout();

		// Check if clicking on collapsed swatch
		if (!expanded && isHovered(mouseX, mouseY)) {
			expanded = true;
			focusedInput = 0;
			return;
		}

		// Check if clicking outside expanded popup
		if (expanded) {
			int popupX = layout.x;
			int popupY = layout.y + layout.height + 2;
			int actualPopupHeight = popupHeight + (includeAlpha ? ALPHA_BAR_HEIGHT + 4 : 0);

			boolean inSwatch = mouseX >= layout.x && mouseX <= layout.x + layout.width &&
					mouseY >= layout.y && mouseY <= layout.y + layout.height;
			boolean inPopup = mouseX >= popupX && mouseX <= popupX + popupWidth &&
					mouseY >= popupY && mouseY <= popupY + actualPopupHeight;

			if (!inSwatch && !inPopup) {
				expanded = false;
				focusedInput = 0;
				applyInputTexts();
				return;
			}

			// Handle clicks within popup
			if (inPopup) {
				handlePopupClick(mouseX, mouseY, popupX, popupY);
			}
		}

		super.onClick(mouseX, mouseY, button);
	}

	private void handlePopupClick(double mouseX, double mouseY, int popupX, int popupY) {
		int padding = 4;
		int innerX = popupX + padding;
		int innerY = popupY + padding;
		int innerWidth = popupWidth - padding * 2;
		int svSize = innerWidth - HUE_BAR_WIDTH - 4;

		// Check SV square
		if (mouseX >= innerX && mouseX < innerX + svSize &&
				mouseY >= innerY && mouseY < innerY + svSize) {
			isDraggingSV = true;
			focusedInput = 0;
			updateSVFromMouse(mouseX, mouseY, innerX, innerY, svSize);
			return;
		}

		// Check hue bar
		int hueX = innerX + svSize + 4;
		if (mouseX >= hueX && mouseX < hueX + HUE_BAR_WIDTH &&
				mouseY >= innerY && mouseY < innerY + svSize) {
			isDraggingHue = true;
			focusedInput = 0;
			updateHueFromMouse(mouseY, innerY, svSize);
			return;
		}

		// Check alpha bar
		int inputY = innerY + svSize + 4;
		if (includeAlpha) {
			if (mouseX >= innerX && mouseX < innerX + innerWidth &&
					mouseY >= inputY && mouseY < inputY + ALPHA_BAR_HEIGHT) {
				isDraggingAlpha = true;
				focusedInput = 0;
				updateAlphaFromMouse(mouseX, innerX, innerWidth);
				return;
			}
			inputY += ALPHA_BAR_HEIGHT + 4;
		}

		// Check input fields
		handleInputFieldClick(mouseX, mouseY, innerX, inputY, innerWidth);
	}

	private void handleInputFieldClick(double mouseX, double mouseY, int x, int y, int width) {
		int fieldHeight = INPUT_HEIGHT;
		int spacing = 2;

		// Hex field
		if (mouseY >= y && mouseY < y + fieldHeight) {
			focusedInput = 1;
			cursorPosition = hexInputText.length();
			resetCursorBlink();
			return;
		}

		// RGB fields
		int rgbY = y + fieldHeight + spacing;
		if (mouseY >= rgbY && mouseY < rgbY + fieldHeight) {
			int fieldWidth = (width - spacing * 2 - (includeAlpha ? spacing : 0)) / (includeAlpha ? 4 : 3);

			if (mouseX >= x && mouseX < x + fieldWidth) {
				focusedInput = 2;
				cursorPosition = rInputText.length();
			} else if (mouseX >= x + fieldWidth + spacing && mouseX < x + fieldWidth * 2 + spacing) {
				focusedInput = 3;
				cursorPosition = gInputText.length();
			} else if (mouseX >= x + (fieldWidth + spacing) * 2 && mouseX < x + fieldWidth * 3 + spacing * 2) {
				focusedInput = 4;
				cursorPosition = bInputText.length();
			} else if (includeAlpha && mouseX >= x + (fieldWidth + spacing) * 3) {
				focusedInput = 5;
				cursorPosition = aInputText.length();
			}
			resetCursorBlink();
		}
	}

	private void resetCursorBlink() {
		cursorVisible = true;
		lastCursorBlink = System.currentTimeMillis();
	}

	@Override
	public void onRelease(double mouseX, double mouseY, int button) {
		if (button == 0) {
			if (isDraggingSV || isDraggingHue || isDraggingAlpha) {
				onColorChanged();
			}
			isDraggingSV = false;
			isDraggingHue = false;
			isDraggingAlpha = false;
		}
		super.onRelease(mouseX, mouseY, button);
	}

	/**
	 * Handles mouse drag for color selection.
	 */
	public void onMouseDragged(double mouseX, double mouseY, int button) {
		if (button != 0 || !expanded) return;

		Rectangle layout = getLayout();
		int popupX = layout.x;
		int popupY = layout.y + layout.height + 2;
		int padding = 4;
		int innerX = popupX + padding;
		int innerY = popupY + padding;
		int innerWidth = popupWidth - padding * 2;
		int svSize = innerWidth - HUE_BAR_WIDTH - 4;

		if (isDraggingSV) {
			updateSVFromMouse(mouseX, mouseY, innerX, innerY, svSize);
		} else if (isDraggingHue) {
			updateHueFromMouse(mouseY, innerY, svSize);
		} else if (isDraggingAlpha) {
			updateAlphaFromMouse(mouseX, innerX, innerWidth);
		}
	}

	private void updateSVFromMouse(double mouseX, double mouseY, int x, int y, int size) {
		saturation = clamp((float) (mouseX - x) / size, 0f, 1f);
		brightness = 1f - clamp((float) (mouseY - y) / size, 0f, 1f);
		updateRGBFromHSV();
	}

	private void updateHueFromMouse(double mouseY, int y, int height) {
		hue = clamp((float) (mouseY - y) / height, 0f, 1f);
		updateRGBFromHSV();
	}

	private void updateAlphaFromMouse(double mouseX, int x, int width) {
		alpha = clamp((int) ((mouseX - x) / width * 255), 0, 255);
		aInputText = String.valueOf(alpha);
	}

	private static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	@Override
	public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
		if (!expanded) return false;

		// Handle Escape to close
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			expanded = false;
			focusedInput = 0;
			applyInputTexts();
			return true;
		}

		// Handle Tab to cycle inputs
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			applyInputTexts();
			int maxInput = includeAlpha ? 5 : 4;
			if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
				focusedInput = focusedInput <= 1 ? maxInput : focusedInput - 1;
			} else {
				focusedInput = focusedInput >= maxInput ? 1 : focusedInput + 1;
			}
			cursorPosition = getCurrentInputText().length();
			resetCursorBlink();
			return true;
		}

		// Handle Enter to apply and close
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			applyInputTexts();
			expanded = false;
			focusedInput = 0;
			return true;
		}

		// Handle text input for focused field
		if (focusedInput > 0) {
			return handleTextInput(keyCode, modifiers);
		}

		return false;
	}

	private boolean handleTextInput(int keyCode, int modifiers) {
		String text = getCurrentInputText();

		switch (keyCode) {
			case GLFW.GLFW_KEY_BACKSPACE:
				if (cursorPosition > 0) {
					text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
					cursorPosition--;
					setCurrentInputText(text);
					resetCursorBlink();
				}
				return true;

			case GLFW.GLFW_KEY_DELETE:
				if (cursorPosition < text.length()) {
					text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
					setCurrentInputText(text);
					resetCursorBlink();
				}
				return true;

			case GLFW.GLFW_KEY_LEFT:
				if (cursorPosition > 0) {
					cursorPosition--;
					resetCursorBlink();
				}
				return true;

			case GLFW.GLFW_KEY_RIGHT:
				if (cursorPosition < text.length()) {
					cursorPosition++;
					resetCursorBlink();
				}
				return true;

			case GLFW.GLFW_KEY_HOME:
				cursorPosition = 0;
				resetCursorBlink();
				return true;

			case GLFW.GLFW_KEY_END:
				cursorPosition = text.length();
				resetCursorBlink();
				return true;
		}

		return false;
	}

	@Override
	public boolean onCharTyped(char chr, int modifiers) {
		if (!expanded || focusedInput == 0) return false;

		String text = getCurrentInputText();
		int maxLength = focusedInput == 1 ? (includeAlpha ? 8 : 6) : 3;

		// Validate character based on input type
		boolean valid;
		if (focusedInput == 1) {
			// Hex input - allow 0-9, A-F
			valid = (chr >= '0' && chr <= '9') || (chr >= 'A' && chr <= 'F') || (chr >= 'a' && chr <= 'f');
		} else {
			// RGB/A input - allow 0-9 only
			valid = chr >= '0' && chr <= '9';
		}

		if (valid && text.length() < maxLength) {
			char toInsert = focusedInput == 1 ? Character.toUpperCase(chr) : chr;
			text = text.substring(0, cursorPosition) + toInsert + text.substring(cursorPosition);
			cursorPosition++;
			setCurrentInputText(text);
			resetCursorBlink();

			// Auto-apply for immediate feedback
			applyInputTexts();
			return true;
		}

		return false;
	}

	private String getCurrentInputText() {
		return switch (focusedInput) {
			case 1 -> hexInputText;
			case 2 -> rInputText;
			case 3 -> gInputText;
			case 4 -> bInputText;
			case 5 -> aInputText;
			default -> "";
		};
	}

	private void setCurrentInputText(String text) {
		switch (focusedInput) {
			case 1 -> hexInputText = text;
			case 2 -> rInputText = text;
			case 3 -> gInputText = text;
			case 4 -> bInputText = text;
			case 5 -> aInputText = text;
		}
	}

	private void applyInputTexts() {
		try {
			// Apply hex if it's complete
			if (hexInputText.length() == 6 || (includeAlpha && hexInputText.length() == 8)) {
				setColorHex(hexInputText);
				return;
			}

			// Apply RGB values
			int r = rInputText.isEmpty() ? 0 : clamp(Integer.parseInt(rInputText), 0, 255);
			int g = gInputText.isEmpty() ? 0 : clamp(Integer.parseInt(gInputText), 0, 255);
			int b = bInputText.isEmpty() ? 0 : clamp(Integer.parseInt(bInputText), 0, 255);
			int a = aInputText.isEmpty() ? 255 : clamp(Integer.parseInt(aInputText), 0, 255);

			setColorInternal(r, g, b, includeAlpha ? a : alpha);
		} catch (NumberFormatException e) {
			// Invalid input, restore from current color
			updateInputTexts();
		}
	}

	@Override
	public boolean canFocus() {
		return true;
	}

	@Override
	public void onFocusChanged(boolean focused) {
		this.focused = focused;
		if (!focused && expanded) {
			// Don't close on focus loss - let click-outside handle it
		}
	}

	/**
	 * Called when the color changes.
	 */
	protected void onColorChanged() {
		if (script != null && script.onColorChanged() != null && !script.onColorChanged().isnil()) {
			Varargs args = LuaValue.varargsOf(new LuaValue[]{
					LuaBinder.bind(new LuaElement(this)),
					LuaValue.valueOf(red),
					LuaValue.valueOf(green),
					LuaValue.valueOf(blue),
					LuaValue.valueOf(alpha)
			});

			try {
				script.onColorChanged().invoke(args);
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error invoking onColorChanged script for AmbleColorPicker {}:", id(), e);
			}
		}
	}

	/**
	 * Checks if a point is within the expanded popup area.
	 */
	public boolean isInExpandedArea(double mouseX, double mouseY) {
		if (!expanded) return false;

		Rectangle layout = getLayout();
		int popupX = layout.x;
		int popupY = layout.y + layout.height + 2;
		int actualPopupHeight = popupHeight + (includeAlpha ? ALPHA_BAR_HEIGHT + 4 : 0);

		return mouseX >= popupX && mouseX <= popupX + popupWidth &&
				mouseY >= popupY && mouseY <= popupY + actualPopupHeight;
	}

	// ===== Builder =====

	public static Builder colorPickerBuilder() {
		return new Builder();
	}

	public static class Builder extends AbstractBuilder<AmbleColorPicker, Builder> {

		@Override
		protected AmbleColorPicker create() {
			return new AmbleColorPicker();
		}

		@Override
		protected Builder self() {
			return this;
		}

		public Builder color(int r, int g, int b, int a) {
			container.setColorInternal(r, g, b, a);
			return this;
		}

		public Builder color(int r, int g, int b) {
			container.setColorInternal(r, g, b, 255);
			return this;
		}

		public Builder colorHex(String hex) {
			container.setColorHex(hex);
			return this;
		}

		public Builder includeAlpha(boolean includeAlpha) {
			container.setIncludeAlpha(includeAlpha);
			return this;
		}

		public Builder borderCollapsed(Color color) {
			container.setBorderCollapsed(color);
			return this;
		}

		public Builder borderCollapsedHover(Color color) {
			container.setBorderCollapsedHover(color);
			return this;
		}

		public Builder borderExpanded(Color color) {
			container.setBorderExpanded(color);
			return this;
		}

		public Builder backgroundExpanded(Color color) {
			container.setBackgroundExpanded(color);
			return this;
		}

		public Builder popupWidth(int width) {
			container.setPopupWidth(width);
			return this;
		}

		public Builder popupHeight(int height) {
			container.setPopupHeight(height);
			return this;
		}

		public Builder script(LuaScript script) {
			container.setScript(script);
			return this;
		}
	}

	// ===== Parser =====

	/**
	 * Parser for AmbleColorPicker elements.
	 * <p>
	 * This parser handles JSON objects that have the "color_picker" property set to true.
	 * <p>
	 * Supported JSON properties:
	 * <ul>
	 *   <li>{@code color_picker} - Boolean, must be true to create a color picker</li>
	 *   <li>{@code initial_color} - Color array [r,g,b] or [r,g,b,a]</li>
	 *   <li>{@code include_alpha} - Boolean, whether to show alpha slider (default: false)</li>
	 *   <li>{@code border_collapsed} - Color array for collapsed border</li>
	 *   <li>{@code border_collapsed_hover} - Color array for hovered collapsed border</li>
	 *   <li>{@code border_expanded} - Color array for expanded popup border</li>
	 *   <li>{@code background_expanded} - Color array for expanded popup background</li>
	 *   <li>{@code popup_width} - Integer width of popup in pixels</li>
	 *   <li>{@code popup_height} - Integer height of popup in pixels (excluding alpha bar)</li>
	 *   <li>{@code script} - Script ID for event handling</li>
	 * </ul>
	 */
	public static class Parser implements AmbleElementParser {

		@Override
		public @Nullable AmbleContainer parse(JsonObject json, @Nullable Identifier resourceId, AmbleContainer base) {
			if (!json.has("color_picker") || !json.get("color_picker").getAsBoolean()) {
				return null;
			}

			AmbleColorPicker picker = AmbleColorPicker.colorPickerBuilder().build();
			picker.copyFrom(base);

			// Parse initial color
			if (json.has("initial_color")) {
				JsonArray colorArray = json.get("initial_color").getAsJsonArray();
				int r = colorArray.get(0).getAsInt();
				int g = colorArray.get(1).getAsInt();
				int b = colorArray.get(2).getAsInt();
				int a = colorArray.size() > 3 ? colorArray.get(3).getAsInt() : 255;
				picker.setColorInternal(r, g, b, a);
			}

			// Parse include_alpha
			if (json.has("include_alpha")) {
				picker.setIncludeAlpha(json.get("include_alpha").getAsBoolean());
			}

			// Parse border colors
			if (json.has("border_collapsed")) {
				picker.setBorderCollapsed(parseColor(json.get("border_collapsed").getAsJsonArray()));
			}
			if (json.has("border_collapsed_hover")) {
				picker.setBorderCollapsedHover(parseColor(json.get("border_collapsed_hover").getAsJsonArray()));
			}
			if (json.has("border_expanded")) {
				picker.setBorderExpanded(parseColor(json.get("border_expanded").getAsJsonArray()));
			}
			if (json.has("background_expanded")) {
				picker.setBackgroundExpanded(parseColor(json.get("background_expanded").getAsJsonArray()));
			}

			// Parse popup dimensions
			if (json.has("popup_width")) {
				picker.setPopupWidth(json.get("popup_width").getAsInt());
			}
			if (json.has("popup_height")) {
				picker.setPopupHeight(json.get("popup_height").getAsInt());
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
				picker.setScript(script);
			}

			return picker;
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
			return 90;
		}
	}
}

