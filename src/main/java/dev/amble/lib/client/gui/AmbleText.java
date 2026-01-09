package dev.amble.lib.client.gui;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AmbleText extends AmbleContainer {
	@Setter
	private Text text;
	@Setter
	private UIAlign textHorizontalAlign = UIAlign.CENTRE;
	@Setter
	private UIAlign textVerticalAlign = UIAlign.CENTRE;
	@Setter
	private boolean shadow = true;

	// Cache fields for wrapped lines - marked transient to exclude from serialization
	// These are recalculated at runtime based on layout and text content
	private transient List<OrderedText> cachedLines;
	private transient int cachedWidth = -1;
	private transient Text cachedText;

	/**
	 * Sets the text and invalidates the cache.
	 */
	public void setText(Text text) {
		if (this.text != text) {
			this.text = text;
			invalidateTextCache();
		}
	}

	/**
	 * Invalidates the cached wrapped lines, forcing recalculation on next render.
	 */
	public void invalidateTextCache() {
		cachedLines = null;
		cachedWidth = -1;
		cachedText = null;
	}

	@Override
	public void recalcuateLayout() {
		super.recalcuateLayout();
		invalidateTextCache();
	}

	private List<OrderedText> getWrappedLines(TextRenderer tr) {
		int currentWidth = getLayout().width - getPadding() * 2;

		// Recalculate if width changed or text changed
		if (cachedLines == null || cachedWidth != currentWidth || cachedText != text) {
			cachedLines = tr.wrapLines(text, currentWidth);
			cachedWidth = currentWidth;
			cachedText = text;
		}

		return cachedLines;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		if (text == null) return;

		TextRenderer tr = MinecraftClient.getInstance().textRenderer;

		// Use cached wrapped lines
		List<OrderedText> lines = getWrappedLines(tr);

		int lineHeight = tr.fontHeight;
		int wrappedHeight = lines.size() * lineHeight;

		int wrappedWidth = 0;
		for (OrderedText line : lines) {
			wrappedWidth = Math.max(wrappedWidth, tr.getWidth(line));
		}

		// Calculate aligned position using WRAPPED size
		int textX = getLayout().x;
		int textY = getLayout().y;

		switch (textHorizontalAlign) {
			case START -> textX += getPadding();
			case CENTRE -> textX += (getLayout().width - wrappedWidth) / 2;
			case END -> textX += getLayout().width - wrappedWidth - getPadding();
		}

		switch (textVerticalAlign) {
			case START -> textY += getPadding();
			case CENTRE -> textY += (getLayout().height - wrappedHeight) / 2;
			case END -> textY += getLayout().height - wrappedHeight - getPadding();
		}

		drawWrappedLines(context, tr, lines, textX, textY, 0xFFFFFF, shadow);
	}

	public static void drawWrappedLines(
			DrawContext context,
			TextRenderer textRenderer,
			List<OrderedText> lines,
			int x,
			int y,
			int color,
			boolean shadow
	) {
		for (OrderedText line : lines) {
			context.drawText(textRenderer, line, x, y, color, shadow);
			y += textRenderer.fontHeight;
		}
	}

	public static Builder textBuilder() {
		return new Builder();
	}

	public static class Builder extends AbstractBuilder<AmbleText, Builder> {

		@Override
		protected AmbleText create() {
			return new AmbleText();
		}

		@Override
		protected Builder self() {
			return this;
		}

		public Builder text(Text text) {
			container.setText(text);
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

		public Builder shadow(boolean shadow) {
			container.setShadow(shadow);
			return this;
		}
	}
}
