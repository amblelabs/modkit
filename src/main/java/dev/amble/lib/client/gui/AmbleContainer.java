package dev.amble.lib.client.gui;

import dev.amble.lib.AmbleKit;
import lombok.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AmbleContainer implements AmbleElement {
	@Setter
	private boolean visible = true;

	@Setter
	private Rectangle layout;

	@Setter
	private Rectangle preferredLayout;

	@Setter
	@Nullable
	private AmbleElement parent = null;

	@Setter
	private int padding;

	@Setter
	private int spacing;

	@Setter
	private UIAlign horizontalAlign = UIAlign.START;

	@Setter
	private UIAlign verticalAlign = UIAlign.START;

	@Setter
	private boolean requiresNewRow = false;

	@Setter
	private Text title = Text.empty();

	@Setter
	private AmbleDisplayType background = AmbleDisplayType.color(Color.WHITE);

	@Setter
	private boolean shouldPause = false;

	@Setter
	private Identifier identifier;

	private @Nullable Screen convertedScreen = null;
	private final List<AmbleElement> children = new ArrayList<>();

	@Override
	public boolean requiresNewRow() {
		return requiresNewRow;
	}

	@Override
	public Identifier id() {
		if (identifier == null) {
			if (parent != null) {
				identifier = parent.id().withPath(parent.id().getPath() + "/" + System.identityHashCode(this));
			} else {
				identifier = AmbleKit.id("container/" + System.identityHashCode(this));
				AmbleKit.LOGGER.error("GUI element missing identifier, no parent found to derive from. Generated id: {}", identifier);
			}
		}

		return identifier;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.background.render(context, getLayout());

		AmbleElement.super.render(context, mouseX, mouseY, delta);
	}

	public Rectangle getLayout() {
		if (layout == null) return getPreferredLayout();

		return layout;
	}

	public Rectangle getPreferredLayout() {
		if (preferredLayout == null) return layout != null ? layout : fallbackLayout();
		return preferredLayout;
	}

	private static final Rectangle FALLBACK_LAYOUT = new Rectangle(0, 0, 100, 100);
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	protected Rectangle fallbackLayout() {
		AmbleKit.LOGGER.error("GUI element {} is missing layout data, using fallback layout", id());

		return new Rectangle(FALLBACK_LAYOUT);
	}

	public Screen toScreen() {
		if (this.convertedScreen == null) {
			this.convertedScreen = createScreen();
		}
		return this.convertedScreen;
	}

	protected Screen createScreen() {
		return new AmbleScreen(this);
	}

	public void display() {
		AmbleContainer primary = AmbleContainer.primaryContainer();
		primary.addChild(this);
		primary.setShouldPause(shouldPause);
		Screen screen = primary.toScreen();
		MinecraftClient.getInstance().setScreen(screen);
	}

	public void copyFrom(AmbleContainer other) {
		this.visible = other.visible;
		this.layout = other.layout;
		this.preferredLayout = other.preferredLayout;
		this.parent = other.parent;
		this.padding = other.padding;
		this.spacing = other.spacing;
		this.horizontalAlign = other.horizontalAlign;
		this.verticalAlign = other.verticalAlign;
		this.requiresNewRow = other.requiresNewRow;
		this.background = other.background;
		this.identifier = other.identifier;
		this.title = other.title;
		this.shouldPause = other.shouldPause;
		this.children.forEach(e -> e.setParent(null));
		this.children.clear();
		other.children.forEach(this::addChild);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static AmbleContainer primaryContainer() {
		return AmbleContainer.builder()
				.layout(new Rectangle(0, 0,
						MinecraftClient.getInstance().getWindow().getScaledWidth(),
						MinecraftClient.getInstance().getWindow().getScaledHeight()))
				.background(TRANSPARENT)
				.build();
	}

	public static class AmbleScreen extends Screen {
		public final AmbleContainer source;
		private @Nullable AmbleElement focusedElement = null;
		private long lastClickTime = 0;
		private double lastClickX = 0;
		private double lastClickY = 0;

		public AmbleScreen(AmbleContainer source) {
			super(source.getTitle());
			this.source = source;
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, float delta) {
			super.render(context, mouseX, mouseY, delta);

			source.render(context, mouseX, mouseY, delta);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			// Find if we clicked on a focusable element
			AmbleElement clickedFocusable = findFocusableAt(source, mouseX, mouseY);

			// Update focus
			if (clickedFocusable != focusedElement) {
				if (focusedElement != null) {
					if (focusedElement instanceof Focusable focusable) {
						focusable.setFocused(false);
						focusable.onFocusChanged(false);
					} else {
						focusedElement.onFocusChanged(false);
					}
				}
				focusedElement = clickedFocusable;
				if (focusedElement != null) {
					if (focusedElement instanceof Focusable focusable) {
						focusable.setFocused(true);
						focusable.onFocusChanged(true);
					} else {
						focusedElement.onFocusChanged(true);
					}
				}
			}

			source.onClick((int) mouseX, (int) mouseY, button);
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			source.onRelease((int) mouseX, (int) mouseY, button);
			return super.mouseReleased(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (focusedElement instanceof AmbleTextInput textInput) {
				textInput.onMouseDragged(mouseX, mouseY, button);
				return true;
			}
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			// Handle Tab for focus navigation
			if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_TAB) {
				cycleFocus(hasShiftDown());
				return true;
			}

			// Delegate to focused element - prefer Focusable interface
			if (focusedElement != null) {
				boolean handled = focusedElement instanceof Focusable focusable
						? focusable.onKeyPressed(keyCode, scanCode, modifiers)
						: focusedElement.onKeyPressed(keyCode, scanCode, modifiers);
				if (handled) return true;
			}

			return super.keyPressed(keyCode, scanCode, modifiers);
		}

		@Override
		public boolean charTyped(char chr, int modifiers) {
			// Delegate to focused element - prefer Focusable interface
			if (focusedElement != null) {
				boolean handled = focusedElement instanceof Focusable focusable
						? focusable.onCharTyped(chr, modifiers)
						: focusedElement.onCharTyped(chr, modifiers);
				if (handled) return true;
			}

			return super.charTyped(chr, modifiers);
		}

		/**
		 * Cycles focus to the next/previous focusable element.
		 */
		private void cycleFocus(boolean reverse) {
			java.util.List<AmbleElement> focusable = new java.util.ArrayList<>();
			source.findFocusableElements(focusable);

			if (focusable.isEmpty()) return;

			int currentIndex = focusedElement != null ? focusable.indexOf(focusedElement) : -1;

			int nextIndex;
			if (reverse) {
				nextIndex = currentIndex <= 0 ? focusable.size() - 1 : currentIndex - 1;
			} else {
				nextIndex = currentIndex >= focusable.size() - 1 ? 0 : currentIndex + 1;
			}

			// Remove focus from current element
			if (focusedElement != null) {
				if (focusedElement instanceof Focusable focusableElement) {
					focusableElement.setFocused(false);
					focusableElement.onFocusChanged(false);
				} else {
					focusedElement.onFocusChanged(false);
				}
			}

			// Set focus to new element
			focusedElement = focusable.get(nextIndex);
			if (focusedElement instanceof Focusable focusableElement) {
				focusableElement.setFocused(true);
				focusableElement.onFocusChanged(true);
			} else {
				focusedElement.onFocusChanged(true);
			}
		}

		/**
		 * Finds the topmost focusable element at the given coordinates.
		 */
		private @Nullable AmbleElement findFocusableAt(AmbleElement element, double mouseX, double mouseY) {
			// Check children first (reverse order for proper z-order)
			java.util.List<AmbleElement> children = element.getChildren();
			for (int i = children.size() - 1; i >= 0; i--) {
				AmbleElement child = children.get(i);
				if (child.isVisible() && child.isHovered(mouseX, mouseY)) {
					AmbleElement found = findFocusableAt(child, mouseX, mouseY);
					if (found != null) return found;
				}
			}

			// Check this element - prefer Focusable interface
			boolean canFocus = element instanceof Focusable focusable
					? focusable.canFocus()
					: element.canFocus();

			if (canFocus && element.isHovered(mouseX, mouseY)) {
				return element;
			}

			return null;
		}

		@Override
		public boolean shouldPause() {
			return source.isShouldPause();
		}
	}

	public static class Builder extends AbstractBuilder<AmbleContainer, Builder> {
		@Override
		protected AmbleContainer create() {
			return new AmbleContainer();
		}

		@Override
		protected Builder self() {
			return this;
		}
	}

	public static abstract class AbstractBuilder<T extends AmbleContainer, B extends AbstractBuilder<T, B>> {
		protected final T container = create();

		protected abstract T create();
		protected abstract B self();

		public B padding(int padding) {
			container.setPadding(padding);
			return self();
		}

		public B spacing(int spacing) {
			container.setSpacing(spacing);
			return self();
		}

		public B horizontalAlign(UIAlign align) {
			container.setHorizontalAlign(align);
			return self();
		}

		public B verticalAlign(UIAlign align) {
			container.setVerticalAlign(align);
			return self();
		}

		public B layout(Rectangle layout) {
			container.setPreferredLayout(layout);
			container.setLayout(layout);
			return self();
		}

		public B background(AmbleDisplayType background) {
			container.setBackground(background);
			return self();
		}

		public B background(Color color) {
			container.setBackground(AmbleDisplayType.color(color));
			return self();
		}

		public B background(AmbleDisplayType.TextureData texture) {
			container.setBackground(AmbleDisplayType.texture(texture));
			return self();
		}

		public B title(Text title) {
			container.setTitle(title);
			return self();
		}

		public B requiresNewRow(boolean requiresNewRow) {
			container.setRequiresNewRow(requiresNewRow);
			return self();
		}

		public B visible(boolean visible) {
			container.setVisible(visible);
			return self();
		}

		public B children(List<AmbleElement> children) {
			for (AmbleElement child : children) {
				container.addChild(child);
			}
			return self();
		}

		public B shouldPause(boolean shouldPause) {
			container.setShouldPause(shouldPause);
			return self();
		}

		public T build() {
			return container;
		}
	}
}
