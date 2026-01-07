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

	public static AmbleButton of(AmbleContainer container, AmbleDisplayType hoverColor, AmbleDisplayType pressColor, Runnable onClick) {
		AmbleButton button = new AmbleButton();
		button.setPosition(container.getPosition());
		button.setVisible(container.isVisible());
		button.setLayout(container.getLayout());
		button.setPreferredLayout(container.getPreferredLayout());
		button.setParent(container.getParent());
		button.setPadding(container.getPadding());
		button.setSpacing(container.getSpacing());
		button.setHorizontalAlign(container.getHorizontalAlign());
		button.setVerticalAlign(container.getVerticalAlign());
		button.setRequiresNewRow(container.requiresNewRow());
		button.setBackground(container.getBackground());

		button.hoverDisplay = hoverColor;
		button.pressDisplay = pressColor;
		button.onClick = onClick;

		return button;
	}

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
}
