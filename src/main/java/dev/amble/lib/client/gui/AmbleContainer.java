package dev.amble.lib.client.gui;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.AmbleKitClient;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmbleContainer implements AmbleElement {
	@Setter
	@Builder.Default
	private boolean visible = true;

	@Setter
	private Rectangle layout;

	@Setter
	private Rectangle preferredLayout;

	@Setter
	@Nullable
	@Builder.Default
	private AmbleElement parent = null;

	@Setter
	private int padding;

	@Setter
	private int spacing;

	@Setter
	@Builder.Default
	private UIAlign horizontalAlign = UIAlign.START;

	@Setter
	@Builder.Default
	private UIAlign verticalAlign = UIAlign.START;

	@Setter
	@Builder.Default
	private boolean requiresNewRow = false;

	@Setter
	@Builder.Default
	private Text title = Text.empty();

	@Setter
	@Builder.Default
	public AmbleDisplayType background = AmbleDisplayType.color(Color.WHITE);

	@Setter
	private Identifier identifier;

	@Builder.Default
	private @Nullable Screen convertedScreen = null;
	@Builder.Default
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
		if (preferredLayout == null) return layout != null ? layout : new Rectangle(0, 0, 100, 100);
		return preferredLayout;
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

	public static AmbleContainer primaryContainer() {
		return AmbleContainer.builder()
				.preferredLayout(new Rectangle(0, 0,
						net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledWidth(),
						net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledHeight()))
				.background(AmbleDisplayType.color(new Color(0, 0, 0, 0)))
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
}
