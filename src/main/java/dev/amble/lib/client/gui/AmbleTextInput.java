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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.awt.*;

/**
 * A text input element that allows users to type and edit text.
 * <p>
 * Features:
 * <ul>
 *   <li>Full keyboard navigation (arrows, home, end, with ctrl for word jump)</li>
 *   <li>Text selection via shift+arrows or mouse drag</li>
 *   <li>Double-click to select word</li>
 *   <li>Copy/Cut/Paste/Select All (Ctrl+C/X/V/A)</li>
 *   <li>Horizontal scrolling for long text</li>
 *   <li>Customizable colors for text, placeholder, selection, and borders</li>
 *   <li>Placeholder text when empty</li>
 *   <li>Max length limit</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
public class AmbleTextInput extends AmbleContainer implements Focusable {
	private static final int CURSOR_BLINK_RATE = 530; // ms
	private static final int DOUBLE_CLICK_TIME = 250; // ms

	// Text content
	private String text = "";

	@Setter
	private String placeholder = "";

	@Setter
	private int maxLength = Integer.MAX_VALUE;

	@Setter
	private boolean editable = true;

	// Cursor and selection
	private int cursorPosition = 0;
	private int selectionStart = 0;
	private int selectionEnd = 0;

	// Focus state
	@Setter
	private boolean focused = false;

	// Scroll offset for long text
	private int scrollOffset = 0;

	// Cursor blink timing
	private long lastCursorBlink = 0;
	private boolean cursorVisible = true;

	// Double-click detection
	private long lastClickTime = 0;
	private int lastClickX = 0;

	// Text alignment
	@Setter
	private UIAlign textHorizontalAlign = UIAlign.START;
	@Setter
	private UIAlign textVerticalAlign = UIAlign.CENTRE;

	// Customizable colors
	@Setter
	private Color textColor = new Color(255, 255, 255);
	@Setter
	private Color placeholderColor = new Color(128, 128, 128);
	@Setter
	private Color selectionColor = new Color(0, 120, 215, 128);
	@Setter
	private Color borderColor = new Color(160, 160, 160);
	@Setter
	private Color focusedBorderColor = new Color(80, 160, 255);
	@Setter
	private Color cursorColor = new Color(255, 255, 255);

	// Script support
	@Setter
	private @Nullable LuaScript script;

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		// Render background
		getBackground().render(context, getLayout());

		Rectangle layout = getLayout();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		// Draw border
		Color border = focused ? focusedBorderColor : borderColor;
		drawBorder(context, layout, border);

		// Calculate text area with padding
		int textX = layout.x + getPadding() + 2;
		int textY = layout.y + (layout.height - textRenderer.fontHeight) / 2;
		// Enable scissor to clip text within bounds
		context.enableScissor(layout.x + getPadding(), layout.y, layout.x + layout.width - getPadding(), layout.y + layout.height);

		if (text.isEmpty() && !focused) {
			// Draw placeholder
			context.drawText(textRenderer, placeholder, textX - scrollOffset, textY, placeholderColor.getRGB(), false);
		} else {
			// Draw selection highlight
			if (hasSelection() && focused) {
				drawSelection(context, textRenderer, textX, textY);
			}

			// Draw text
			context.drawText(textRenderer, text, textX - scrollOffset, textY, textColor.getRGB(), false);

			// Draw cursor
			if (focused && editable) {
				updateCursorBlink();
				if (cursorVisible) {
					drawCursor(context, textRenderer, textX, textY);
				}
			}
		}

		context.disableScissor();

		// Render children (if any)
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

	private void drawSelection(DrawContext context, TextRenderer textRenderer, int textX, int textY) {
		int selStart = Math.min(selectionStart, selectionEnd);
		int selEnd = Math.max(selectionStart, selectionEnd);

		String beforeSelection = text.substring(0, selStart);
		String selection = text.substring(selStart, selEnd);

		int startX = textX - scrollOffset + textRenderer.getWidth(beforeSelection);
		int endX = startX + textRenderer.getWidth(selection);

		context.fill(startX, textY - 1, endX, textY + textRenderer.fontHeight + 1, selectionColor.getRGB());
	}

	private void drawCursor(DrawContext context, TextRenderer textRenderer, int textX, int textY) {
		String beforeCursor = text.substring(0, cursorPosition);
		int cursorX = textX - scrollOffset + textRenderer.getWidth(beforeCursor);

		context.fill(cursorX, textY - 1, cursorX + 1, textY + textRenderer.fontHeight + 1, cursorColor.getRGB());
	}

	private void updateCursorBlink() {
		long now = System.currentTimeMillis();
		if (now - lastCursorBlink > CURSOR_BLINK_RATE) {
			cursorVisible = !cursorVisible;
			lastCursorBlink = now;
		}
	}

	private void resetCursorBlink() {
		cursorVisible = true;
		lastCursorBlink = System.currentTimeMillis();
	}

	/**
	 * Ensures the cursor is visible within the text field by adjusting scroll offset.
	 */
	private void ensureCursorVisible() {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		Rectangle layout = getLayout();
		int availableWidth = layout.width - getPadding() * 2 - 4;

		String beforeCursor = text.substring(0, cursorPosition);
		int cursorX = textRenderer.getWidth(beforeCursor);

		// Scroll left if cursor is before visible area
		if (cursorX < scrollOffset) {
			scrollOffset = cursorX;
		}

		// Scroll right if cursor is after visible area
		if (cursorX > scrollOffset + availableWidth) {
			scrollOffset = cursorX - availableWidth;
		}

		// Ensure scroll offset is never negative
		scrollOffset = Math.max(0, scrollOffset);

		// Ensure we don't scroll past the end of text
		int textWidth = textRenderer.getWidth(text);
		if (textWidth <= availableWidth) {
			scrollOffset = 0;
		} else {
			scrollOffset = Math.min(scrollOffset, textWidth - availableWidth);
		}
	}

	/**
	 * Sets the text content and clamps cursor/selection positions to valid bounds.
	 */
	public void setText(String text) {
		this.text = text != null ? text : "";
		// Clamp cursor and selection to valid bounds
		cursorPosition = Math.max(0, Math.min(cursorPosition, this.text.length()));
		selectionStart = Math.max(0, Math.min(selectionStart, this.text.length()));
		selectionEnd = Math.max(0, Math.min(selectionEnd, this.text.length()));
	}

	// ===== Selection helpers =====

	public boolean hasSelection() {
		return selectionStart != selectionEnd;
	}

	public String getSelectedText() {
		if (!hasSelection()) return "";
		int start = Math.min(selectionStart, selectionEnd);
		int end = Math.max(selectionStart, selectionEnd);
		return text.substring(start, end);
	}

	public void clearSelection() {
		selectionStart = cursorPosition;
		selectionEnd = cursorPosition;
	}

	public void selectAll() {
		selectionStart = 0;
		selectionEnd = text.length();
		cursorPosition = text.length();
	}

	public void setSelection(int start, int end) {
		selectionStart = Math.max(0, Math.min(start, text.length()));
		selectionEnd = Math.max(0, Math.min(end, text.length()));
		cursorPosition = selectionEnd;
	}

	public void setCursorPosition(int position) {
		this.cursorPosition = Math.max(0, Math.min(position, text.length()));
		resetCursorBlink();
		ensureCursorVisible();
	}

	// ===== Text manipulation =====

	public void insertText(String insert) {
		if (!editable) return;

		// Delete selection first if any
		if (hasSelection()) {
			deleteSelection();
		}

		// Check max length
		int availableSpace = maxLength - text.length();
		if (availableSpace <= 0) return;

		if (insert.length() > availableSpace) {
			insert = insert.substring(0, availableSpace);
		}

		// Filter out invalid characters
		StringBuilder filtered = new StringBuilder();
		for (char c : insert.toCharArray()) {
			if (isValidChar(c)) {
				filtered.append(c);
			}
		}
		insert = filtered.toString();

		if (insert.isEmpty()) return;

		// Insert at cursor
		String before = text.substring(0, cursorPosition);
		String after = text.substring(cursorPosition);
		text = before + insert + after;
		cursorPosition += insert.length();
		clearSelection();
		ensureCursorVisible();
		onTextChanged();
	}

	private boolean isValidChar(char c) {
		// Allow printable characters
		return c >= 32 && c != 127;
	}

	public void deleteSelection() {
		if (!hasSelection() || !editable) return;

		int start = Math.min(selectionStart, selectionEnd);
		int end = Math.max(selectionStart, selectionEnd);

		String before = text.substring(0, start);
		String after = text.substring(end);
		text = before + after;
		cursorPosition = start;
		clearSelection();
		ensureCursorVisible();
		onTextChanged();
	}

	public void deleteCharBefore() {
		if (!editable) return;

		if (hasSelection()) {
			deleteSelection();
			return;
		}

		if (cursorPosition > 0) {
			String before = text.substring(0, cursorPosition - 1);
			String after = text.substring(cursorPosition);
			text = before + after;
			cursorPosition--;
			ensureCursorVisible();
			onTextChanged();
		}
	}

	public void deleteCharAfter() {
		if (!editable) return;

		if (hasSelection()) {
			deleteSelection();
			return;
		}

		if (cursorPosition < text.length()) {
			String before = text.substring(0, cursorPosition);
			String after = text.substring(cursorPosition + 1);
			text = before + after;
			ensureCursorVisible();
			onTextChanged();
		}
	}

	public void deleteWordBefore() {
		if (!editable) return;

		if (hasSelection()) {
			deleteSelection();
			return;
		}

		int wordStart = findWordStart(cursorPosition);
		if (wordStart < cursorPosition) {
			String before = text.substring(0, wordStart);
			String after = text.substring(cursorPosition);
			text = before + after;
			cursorPosition = wordStart;
			ensureCursorVisible();
			onTextChanged();
		}
	}

	public void deleteWordAfter() {
		if (!editable) return;

		if (hasSelection()) {
			deleteSelection();
			return;
		}

		int wordEnd = findWordEnd(cursorPosition);
		if (wordEnd > cursorPosition) {
			String before = text.substring(0, cursorPosition);
			String after = text.substring(wordEnd);
			text = before + after;
			ensureCursorVisible();
			onTextChanged();
		}
	}

	// ===== Word navigation helpers =====

	private int findWordStart(int position) {
		if (position <= 0) return 0;

		int i = position - 1;

		// Skip any whitespace before the word
		while (i > 0 && Character.isWhitespace(text.charAt(i))) {
			i--;
		}

		// Find start of word
		while (i > 0 && !Character.isWhitespace(text.charAt(i - 1))) {
			i--;
		}

		return i;
	}

	private int findWordEnd(int position) {
		if (position >= text.length()) return text.length();

		int i = position;

		// Skip current word
		while (i < text.length() && !Character.isWhitespace(text.charAt(i))) {
			i++;
		}

		// Skip whitespace after word
		while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
			i++;
		}

		return i;
	}

	/**
	 * Selects the word at the given cursor position.
	 */
	public void selectWordAt(int position) {
		if (text.isEmpty()) return;

		position = Math.max(0, Math.min(position, text.length() - 1));

		// If position is at whitespace, just position cursor there
		if (Character.isWhitespace(text.charAt(position))) {
			cursorPosition = position;
			clearSelection();
			return;
		}

		// Find word boundaries
		int start = position;
		while (start > 0 && !Character.isWhitespace(text.charAt(start - 1))) {
			start--;
		}

		int end = position;
		while (end < text.length() && !Character.isWhitespace(text.charAt(end))) {
			end++;
		}

		selectionStart = start;
		selectionEnd = end;
		cursorPosition = end;
		ensureCursorVisible();
	}

	// ===== Clipboard =====

	public void copy() {
		if (hasSelection()) {
			MinecraftClient.getInstance().keyboard.setClipboard(getSelectedText());
		}
	}

	public void cut() {
		if (hasSelection() && editable) {
			copy();
			deleteSelection();
		}
	}

	public void paste() {
		if (!editable) return;
		String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
		if (clipboard != null && !clipboard.isEmpty()) {
			// Remove newlines
			clipboard = clipboard.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
			insertText(clipboard);
		}
	}

	// ===== Cursor movement =====

	public void moveCursorLeft(boolean selecting, boolean wordJump) {
		int newPos;
		if (wordJump) {
			newPos = findWordStart(cursorPosition);
		} else {
			newPos = Math.max(0, cursorPosition - 1);
		}

		if (selecting) {
			if (!hasSelection()) {
				selectionStart = cursorPosition;
			}
			selectionEnd = newPos;
		} else {
			if (hasSelection()) {
				newPos = Math.min(selectionStart, selectionEnd);
			}
			clearSelection();
		}

		cursorPosition = newPos;
		resetCursorBlink();
		ensureCursorVisible();
	}

	public void moveCursorRight(boolean selecting, boolean wordJump) {
		int newPos;
		if (wordJump) {
			newPos = findWordEnd(cursorPosition);
		} else {
			newPos = Math.min(text.length(), cursorPosition + 1);
		}

		if (selecting) {
			if (!hasSelection()) {
				selectionStart = cursorPosition;
			}
			selectionEnd = newPos;
		} else {
			if (hasSelection()) {
				newPos = Math.max(selectionStart, selectionEnd);
			}
			clearSelection();
		}

		cursorPosition = newPos;
		resetCursorBlink();
		ensureCursorVisible();
	}

	public void moveCursorToStart(boolean selecting) {
		if (selecting) {
			if (!hasSelection()) {
				selectionStart = cursorPosition;
			}
			selectionEnd = 0;
		} else {
			clearSelection();
		}

		cursorPosition = 0;
		resetCursorBlink();
		ensureCursorVisible();
	}

	public void moveCursorToEnd(boolean selecting) {
		if (selecting) {
			if (!hasSelection()) {
				selectionStart = cursorPosition;
			}
			selectionEnd = text.length();
		} else {
			clearSelection();
		}

		cursorPosition = text.length();
		resetCursorBlink();
		ensureCursorVisible();
	}

	// ===== Focus handling (Focusable interface) =====

	@Override
	public boolean canFocus() {
		return editable && isVisible();
	}

	@Override
	public void onFocusChanged(boolean focused) {
		this.focused = focused;
		if (!focused) {
			clearSelection();
		} else {
			resetCursorBlink();
		}
	}

	// ===== Mouse handling =====

	@Override
	public void onClick(double mouseX, double mouseY, int button) {
		if (!isHovered(mouseX, mouseY) || button != 0) {
			super.onClick(mouseX, mouseY, button);
			return;
		}

		long now = System.currentTimeMillis();
		int clickX = (int) mouseX;

		// Double-click detection
		if (now - lastClickTime < DOUBLE_CLICK_TIME && Math.abs(clickX - lastClickX) < 5) {
			// Double-click: select word
			int charIndex = getCharIndexAtX((int) mouseX);
			selectWordAt(charIndex);
			lastClickTime = 0; // Reset to prevent triple-click issues
		} else {
			// Single click: position cursor
			int charIndex = getCharIndexAtX((int) mouseX);
			cursorPosition = charIndex;

			boolean shiftHeld = Screen.hasShiftDown();
			if (shiftHeld && focused) {
				// Extend selection
				if (!hasSelection()) {
					selectionStart = cursorPosition;
				}
				selectionEnd = charIndex;
				cursorPosition = charIndex;
			} else {
				clearSelection();
			}

			resetCursorBlink();
			ensureCursorVisible();
			lastClickTime = now;
			lastClickX = clickX;
		}

		// Invoke script onClick if present
		if (script != null && script.onClick() != null && !script.onClick().isnil()) {
			try {
				Varargs args = LuaValue.varargsOf(new LuaValue[]{
						LuaBinder.bind(new LuaElement(this)),
						LuaValue.valueOf(mouseX),
						LuaValue.valueOf(mouseY),
						LuaValue.valueOf(button)
				});
				script.onClick().invoke(args);
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error invoking onClick script for AmbleTextInput {}:", id(), e);
			}
		}

		super.onClick(mouseX, mouseY, button);
	}

	/**
	 * Handles mouse drag for selection.
	 */
	public void onMouseDragged(double mouseX, double mouseY, int button) {
		if (!focused || button != 0) return;

		int charIndex = getCharIndexAtX((int) mouseX);

		if (!hasSelection()) {
			selectionStart = cursorPosition;
		}
		selectionEnd = charIndex;
		cursorPosition = charIndex;

		resetCursorBlink();
		ensureCursorVisible();
	}

	/**
	 * Gets the character index at the given screen X coordinate.
	 */
	private int getCharIndexAtX(int screenX) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		Rectangle layout = getLayout();

		int relativeX = screenX - layout.x - getPadding() - 2 + scrollOffset;

		if (relativeX <= 0) return 0;
		if (text.isEmpty()) return 0;

		// Binary search for the character position
		int textWidth = textRenderer.getWidth(text);
		if (relativeX >= textWidth) return text.length();

		// Linear search (could optimize with binary search for very long text)
		for (int i = 0; i <= text.length(); i++) {
			int width = textRenderer.getWidth(text.substring(0, i));
			if (width > relativeX) {
				// Check if closer to previous or current character
				if (i > 0) {
					int prevWidth = textRenderer.getWidth(text.substring(0, i - 1));
					if (relativeX - prevWidth < width - relativeX) {
						return i - 1;
					}
				}
				return i;
			}
		}

		return text.length();
	}

	// ===== Keyboard handling =====

	@Override
	public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
		if (!focused || !editable) return false;

		boolean ctrl = Screen.hasControlDown();
		boolean shift = Screen.hasShiftDown();

		switch (keyCode) {
			case GLFW.GLFW_KEY_LEFT:
				moveCursorLeft(shift, ctrl);
				return true;

			case GLFW.GLFW_KEY_RIGHT:
				moveCursorRight(shift, ctrl);
				return true;

			case GLFW.GLFW_KEY_HOME:
				moveCursorToStart(shift);
				return true;

			case GLFW.GLFW_KEY_END:
				moveCursorToEnd(shift);
				return true;

			case GLFW.GLFW_KEY_BACKSPACE:
				if (ctrl) {
					deleteWordBefore();
				} else {
					deleteCharBefore();
				}
				return true;

			case GLFW.GLFW_KEY_DELETE:
				if (ctrl) {
					deleteWordAfter();
				} else {
					deleteCharAfter();
				}
				return true;

			case GLFW.GLFW_KEY_A:
				if (ctrl) {
					selectAll();
					return true;
				}
				break;

			case GLFW.GLFW_KEY_C:
				if (ctrl) {
					copy();
					return true;
				}
				break;

			case GLFW.GLFW_KEY_X:
				if (ctrl) {
					cut();
					return true;
				}
				break;

			case GLFW.GLFW_KEY_V:
				if (ctrl) {
					paste();
					return true;
				}
				break;
		}

		return false;
	}

	@Override
	public boolean onCharTyped(char chr, int modifiers) {
		if (!focused || !editable) return false;

		if (isValidChar(chr)) {
			insertText(String.valueOf(chr));
			return true;
		}

		return false;
	}

	// ===== Text change callback =====

	protected void onTextChanged() {
		// Future: could invoke an onTextChanged callback in the script
	}

	// ===== Builder =====

	public static Builder textInputBuilder() {
		return new Builder();
	}

	public static class Builder extends AbstractBuilder<AmbleTextInput, Builder> {

		@Override
		protected AmbleTextInput create() {
			return new AmbleTextInput();
		}

		@Override
		protected Builder self() {
			return this;
		}

		public Builder text(String text) {
			container.setText(text);
			return this;
		}

		public Builder placeholder(String placeholder) {
			container.setPlaceholder(placeholder);
			return this;
		}

		public Builder maxLength(int maxLength) {
			container.setMaxLength(maxLength);
			return this;
		}

		public Builder editable(boolean editable) {
			container.setEditable(editable);
			return this;
		}

		public Builder textColor(Color color) {
			container.setTextColor(color);
			return this;
		}

		public Builder placeholderColor(Color color) {
			container.setPlaceholderColor(color);
			return this;
		}

		public Builder selectionColor(Color color) {
			container.setSelectionColor(color);
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

		public Builder cursorColor(Color color) {
			container.setCursorColor(color);
			return this;
		}

		public Builder textHorizontalAlign(UIAlign align) {
			container.setTextHorizontalAlign(align);
			return this;
		}

		public Builder textVerticalAlign(UIAlign align) {
			container.setTextVerticalAlign(align);
			return this;
		}
	}

	// ===== Parser =====

	/**
	 * Parser for AmbleTextInput elements.
	 * <p>
	 * This parser handles JSON objects that have the "text_input" property set to true.
	 * <p>
	 * Supported JSON properties:
	 * <ul>
	 *   <li>{@code text_input} - Boolean, must be true to create a text input</li>
	 *   <li>{@code placeholder} - String placeholder text when empty</li>
	 *   <li>{@code max_length} - Integer maximum character count</li>
	 *   <li>{@code editable} - Boolean, whether text can be edited (default: true)</li>
	 *   <li>{@code text} - String initial text content</li>
	 *   <li>{@code text_alignment} - Array [horizontal, vertical] alignment</li>
	 *   <li>{@code text_color} - Color array [r,g,b] or [r,g,b,a]</li>
	 *   <li>{@code placeholder_color} - Color array</li>
	 *   <li>{@code selection_color} - Color array</li>
	 *   <li>{@code border_color} - Color array</li>
	 *   <li>{@code focused_border_color} - Color array</li>
	 *   <li>{@code cursor_color} - Color array</li>
	 *   <li>{@code script} - Script ID for event handling</li>
	 * </ul>
	 */
	public static class Parser implements AmbleElementParser {

		@Override
		public @Nullable AmbleContainer parse(JsonObject json, @Nullable Identifier resourceId, AmbleContainer base) {
			if (!json.has("text_input") || !json.get("text_input").getAsBoolean()) {
				return null;
			}

			AmbleTextInput input = AmbleTextInput.textInputBuilder().build();
			input.copyFrom(base);

			// Parse placeholder
			if (json.has("placeholder")) {
				input.setPlaceholder(json.get("placeholder").getAsString());
			}

			// Parse max length
			if (json.has("max_length")) {
				input.setMaxLength(json.get("max_length").getAsInt());
			}

			// Parse editable
			if (json.has("editable")) {
				input.setEditable(json.get("editable").getAsBoolean());
			}

			// Parse initial text
			if (json.has("text")) {
				input.setText(json.get("text").getAsString());
			}

			// Parse text alignment
			if (json.has("text_alignment")) {
				JsonArray alignArray = json.get("text_alignment").getAsJsonArray();
				if (alignArray.size() >= 2) {
					String hAlign = alignArray.get(0).getAsString().toUpperCase();
					String vAlign = alignArray.get(1).getAsString().toUpperCase();
					if (hAlign.equals("CENTER")) hAlign = "CENTRE";
					if (vAlign.equals("CENTER")) vAlign = "CENTRE";
					input.setTextHorizontalAlign(UIAlign.valueOf(hAlign));
					input.setTextVerticalAlign(UIAlign.valueOf(vAlign));
				}
			}

			// Parse colors
			if (json.has("text_color")) {
				input.setTextColor(parseColor(json.get("text_color").getAsJsonArray()));
			}
			if (json.has("placeholder_color")) {
				input.setPlaceholderColor(parseColor(json.get("placeholder_color").getAsJsonArray()));
			}
			if (json.has("selection_color")) {
				input.setSelectionColor(parseColor(json.get("selection_color").getAsJsonArray()));
			}
			if (json.has("border_color")) {
				input.setBorderColor(parseColor(json.get("border_color").getAsJsonArray()));
			}
			if (json.has("focused_border_color")) {
				input.setFocusedBorderColor(parseColor(json.get("focused_border_color").getAsJsonArray()));
			}
			if (json.has("cursor_color")) {
				input.setCursorColor(parseColor(json.get("cursor_color").getAsJsonArray()));
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
				input.setScript(script);
			}

			return input;
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
			// Higher than entity display (75), lower than button (100)
			return 80;
		}
	}
}

