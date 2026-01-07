package dev.amble.lib.client.gui;

import dev.amble.lib.AmbleKit;
import lombok.*;
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
	public AmbleDisplayType background = AmbleDisplayType.color(Color.WHITE);

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
				identifier = new Identifier(parent.id().getNamespace(),
						parent.id().getPath() + "/" + System.identityHashCode(this));
			} else {
				identifier = new Identifier("amble",
						"container/" + System.identityHashCode(this));
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

	protected Rectangle fallbackLayout() {
		AmbleKit.LOGGER.error("GUI element {} is missing layout data, using fallback layout", id());

		return new Rectangle(0, 0, 100, 100);
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
		var primary = AmbleContainer.primaryContainer();
		primary.addChild(this);
		Screen screen = primary.toScreen();
		net.minecraft.client.MinecraftClient.getInstance().setScreen(screen);
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
	}

	public static Builder builder() {
		return new Builder();
	}

	public static AmbleContainer primaryContainer() {
		return AmbleContainer.builder()
				.layout(new Rectangle(0, 0,
						net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledWidth(),
						net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledHeight()))
				.background(new Color(0, 0, 0, 0))
				.build();
	}

	public static class AmbleScreen extends Screen {
		public final AmbleContainer source;

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
			source.onClick((int) mouseX, (int) mouseY, button);
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			source.onRelease((int) mouseX, (int) mouseY, button);
			return super.mouseReleased(mouseX, mouseY, button);
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

		public T build() {
			return container;
		}
	}
}
