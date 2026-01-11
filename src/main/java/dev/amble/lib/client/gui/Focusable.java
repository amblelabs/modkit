package dev.amble.lib.client.gui;

/**
 * Interface for GUI elements that can receive keyboard focus.
 * <p>
 * Implementing this interface allows elements to:
 * <ul>
 *   <li>Receive keyboard events when focused</li>
 *   <li>Participate in Tab navigation</li>
 *   <li>Respond to focus gain/loss events</li>
 * </ul>
 * <p>
 * Elements that implement this interface should also handle rendering
 * visual feedback to indicate their focused state (e.g., highlighted border).
 */
public interface Focusable {

	/**
	 * Returns whether this element can currently receive keyboard focus.
	 * <p>
	 * An element may return false if it's disabled, invisible, or otherwise
	 * not ready to accept input.
	 *
	 * @return true if this element can receive focus
	 */
	boolean canFocus();

	/**
	 * Returns whether this element currently has keyboard focus.
	 *
	 * @return true if this element is focused
	 */
	boolean isFocused();

	/**
	 * Sets the focused state of this element.
	 * <p>
	 * This is typically called by the screen's focus management system.
	 * Implementations should update their visual state accordingly.
	 *
	 * @param focused true to give focus, false to remove focus
	 */
	void setFocused(boolean focused);

	/**
	 * Called when this element gains or loses focus.
	 * <p>
	 * This callback allows elements to perform additional actions when
	 * focus changes, such as starting/stopping cursor blink animations
	 * or clearing selections.
	 *
	 * @param focused true if gaining focus, false if losing focus
	 */
	void onFocusChanged(boolean focused);

	/**
	 * Called when a key is pressed while this element has focus.
	 *
	 * @param keyCode the GLFW key code
	 * @param scanCode the platform-specific scan code
	 * @param modifiers bitfield of modifier keys (shift, ctrl, alt)
	 * @return true if the key event was handled and should not propagate
	 */
	boolean onKeyPressed(int keyCode, int scanCode, int modifiers);

	/**
	 * Called when a character is typed while this element has focus.
	 * <p>
	 * This is called for printable characters after key press events.
	 *
	 * @param chr the typed character
	 * @param modifiers bitfield of modifier keys
	 * @return true if the character was handled and should not propagate
	 */
	boolean onCharTyped(char chr, int modifiers);
}

