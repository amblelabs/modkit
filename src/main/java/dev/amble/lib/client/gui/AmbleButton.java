package dev.amble.lib.client.gui;


import lombok.*;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AmbleButton extends AmbleContainer {
	private AmbleDisplayType hoverDisplay;
	private AmbleDisplayType pressDisplay;
	private Runnable onClick;
	private @Nullable AmbleDisplayType normalDisplay = null;
	private boolean isClicked = false;


	@Override
	public void onRelease(double mouseX, double mouseY, int button) {
		onClick.run();
		this.setBackground(
				isHovered(mouseX, mouseY) ? hoverDisplay : getNormalDisplay()
		);
		this.isClicked = false;
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button) {
		this.setBackground(pressDisplay);
		this.isClicked = true;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (isClicked) {
			setBackground(pressDisplay);
		} else if (isHovered(mouseX, mouseY)) {
			setBackground(hoverDisplay);
		} else {
			setBackground(getNormalDisplay());
		}

		super.render(context, mouseX, mouseY, delta);
	}

	public @Nullable AmbleDisplayType getNormalDisplay() {
		if (normalDisplay == null) {
			normalDisplay = this.getBackground();
		}

		return normalDisplay;
	}

	public static Builder buttonBuilder() {
		return new Builder();
	}

	public static class Builder extends AbstractBuilder<AmbleButton, Builder> {

		@Override
		protected AmbleButton create() {
			return new AmbleButton();
		}

		@Override
		protected Builder self() {
			return this;
		}

		public Builder hoverDisplay(AmbleDisplayType hoverDisplay) {
			container.setHoverDisplay(hoverDisplay);
			return this;
		}

		public Builder hoverDisplay(Color hoverColor) {
			container.setHoverDisplay(AmbleDisplayType.color(hoverColor));
			return this;
		}

		public Builder hoverDisplay(AmbleDisplayType.TextureData hoverTexture) {
			container.setHoverDisplay(AmbleDisplayType.texture(hoverTexture));
			return this;
		}

		public Builder pressDisplay(AmbleDisplayType pressDisplay) {
			container.setPressDisplay(pressDisplay);
			return this;
		}

		public Builder pressDisplay(Color pressColor) {
			container.setPressDisplay(AmbleDisplayType.color(pressColor));
			return this;
		}

		public Builder pressDisplay(AmbleDisplayType.TextureData pressTexture) {
			container.setPressDisplay(AmbleDisplayType.texture(pressTexture));
			return this;
		}

		public Builder onClick(Runnable onClick) {
			container.setOnClick(onClick);
			return this;
		}
	}
}
