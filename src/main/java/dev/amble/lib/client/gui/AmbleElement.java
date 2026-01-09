package dev.amble.lib.client.gui;

import dev.amble.lib.api.Identifiable;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public interface AmbleElement extends Drawable, Identifiable {
	default Vec2f getPosition() {
		Rectangle layout = getLayout();
		return new Vec2f(layout.x, layout.y);
	}
	default void setPosition(Vec2f position) {
		Rectangle layout = getLayout();
		layout.x = (int) position.x;
		layout.y = (int) position.y;
		setPreferredLayout(layout);

		recalcuateLayout();

		if (getParent() != null) {
			getParent().recalcuateLayout();
		}
	}

	default void setDimensions(Vec2f dimensions) {
		Rectangle layout = getLayout();
		layout.width = (int) dimensions.x;
		layout.height = (int) dimensions.y;
		setPreferredLayout(layout);

		recalcuateLayout();

		if (getParent() != null) {
			getParent().recalcuateLayout();
		}
	}

	boolean isVisible();
	void setVisible(boolean visible);

	Rectangle getLayout();
	void setLayout(Rectangle layout);

	Rectangle getPreferredLayout();
	void setPreferredLayout(Rectangle preferredLayout);

	@Nullable AmbleElement getParent();
	void setParent(@Nullable AmbleElement parent);

	int getPadding();
	void setPadding(int padding);

	int getSpacing();
	void setSpacing(int spacing);

	UIAlign getHorizontalAlign();
	void setHorizontalAlign(UIAlign align);

	UIAlign getVerticalAlign();
	void setVerticalAlign(UIAlign align);

	boolean requiresNewRow();
	void setRequiresNewRow(boolean requiresNewRow);

	List<AmbleElement> getChildren();

	default void addChild(AmbleElement child) {
		if (!getChildren().contains(child)) {
			getChildren().add(child);
			child.setParent(this);

			recalcuateLayout();
		}
	}

	private IntIntPair layoutRow(
			List<AmbleElement> row,
			int startX,
			int maxWidth,
			int cursorY,
			int rowHeight
	) {
		if (row.isEmpty()) return IntIntImmutablePair.of(cursorY, rowHeight);

		int rowWidth = row.stream()
				.mapToInt(e -> e.getPreferredLayout().width)
				.sum()
				+ getSpacing() * (row.size() - 1);

		int offsetX = switch (row.get(0).getHorizontalAlign()) {
			case CENTRE -> (maxWidth - rowWidth) / 2;
			case END -> maxWidth - rowWidth;
			default -> 0;
		};

		int x = startX + offsetX;
		boolean singleElementFullCenter =
				row.size() == 1 &&
						row.get(0).getVerticalAlign() == UIAlign.CENTRE;

		int innerHeight = getLayout().height - getPadding() * 2;

		for (AmbleElement e : row) {
			int y;

			if (singleElementFullCenter) {
				y = getLayout().y + getPadding()
						+ (innerHeight - e.getPreferredLayout().height) / 2;
			} else {
				y = cursorY;

				if (e.getVerticalAlign() == UIAlign.CENTRE)
					y += (rowHeight - e.getPreferredLayout().height) / 2;
				else if (e.getVerticalAlign() == UIAlign.END)
					y += rowHeight - e.getPreferredLayout().height;
			}

			e.setLayout(new Rectangle(
					x, y,
					e.getPreferredLayout().width,
					e.getPreferredLayout().height
			));

			e.recalcuateLayout();
			x += e.getPreferredLayout().width + getSpacing();
		}

		cursorY += rowHeight + getSpacing();
		row.clear();
		rowHeight = 0;
		return IntIntImmutablePair.of(cursorY, rowHeight);
	}

	default void recalcuateLayout() {

		int startX = getLayout().x + getPadding();
		int maxWidth = getLayout().width - getPadding() * 2;

		int cursorX = startX;
		int cursorY = getLayout().y + getPadding();
		int rowHeight = 0;

		List<AmbleElement> row = new ArrayList<>();

		for (AmbleElement child : getChildren()) {
			int w = child.getPreferredLayout().width;
			int h = child.getPreferredLayout().height;

			if (cursorX + w > startX + maxWidth || child.requiresNewRow()) {
				IntIntPair result = layoutRow(
						row,
						startX,
						maxWidth,
						cursorY,
						rowHeight
				);
				cursorY = result.leftInt();
				rowHeight = result.rightInt();
				cursorX = startX;
			}

			row.add(child);
			child.recalcuateLayout();

			cursorX += w + getSpacing();
			rowHeight = Math.max(rowHeight, h);
		}

		if (!row.isEmpty()) {
			layoutRow(
					row,
					startX,
					maxWidth,
					cursorY,
					rowHeight
			);
		}
	}

	@Override
	default void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (!isVisible()) return;

		for (AmbleElement child : getChildren()) {
			if (!child.isVisible()) continue;

			child.render(context, mouseX, mouseY, delta);
		}
	}

	default boolean isHovered(double mouseX, double mouseY) {
		Rectangle layout = getLayout();
		return mouseX >= layout.x && mouseX <= layout.x + layout.width &&
				mouseY >= layout.y && mouseY <= layout.y + layout.height;
	}

	default void onClick(double mouseX, double mouseY, int button) {
		for (AmbleElement child : getChildren()) {
			if (!child.isVisible()) continue;

			if (child.isHovered(mouseX, mouseY)) {
				child.onClick(mouseX, mouseY, button);
			}
		}
	}

	default void onRelease(double mouseX, double mouseY, int button) {
		for (AmbleElement child : getChildren()) {
			if (!child.isVisible()) continue;

			if (child.isHovered(mouseX, mouseY)) {
				child.onRelease(mouseX, mouseY, button);
			}
		}
	}

	default Identifier toMcssFile() {
		return this.id().withPrefixedPath("gui/").withSuffixedPath(".json");
	}
}
