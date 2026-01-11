package dev.amble.lib.client.gui;

/**
 * Interface for GUI elements that support mouse drag interactions.
 * <p>
 * Implementing this interface allows elements to receive mouse drag events
 * from the screen's drag handler. This is used by elements like sliders,
 * color pickers, and text inputs for selection dragging.
 */
public interface Draggable {

	/**
	 * Called when the mouse is dragged while this element is the drag target.
	 * <p>
	 * This method is called continuously while the mouse button is held down
	 * and the mouse is being moved.
	 *
	 * @param mouseX the current mouse X position
	 * @param mouseY the current mouse Y position
	 * @param button the mouse button being held (0 = left, 1 = right, 2 = middle)
	 */
	void onMouseDragged(double mouseX, double mouseY, int button);
}

