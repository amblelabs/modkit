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
@Setter
public class AmbleText extends AmbleContainer {
	private Text text;
	private UIAlign textHorizontalAlign = UIAlign.CENTRE;
	private UIAlign textVerticalAlign = UIAlign.CENTRE;
	private boolean shadow = true;

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		TextRenderer tr = MinecraftClient.getInstance().textRenderer;

		// 1. Wrap text first
		List<OrderedText> lines = tr.wrapLines(text, getLayout().width - getPadding() * 2);

		int lineHeight = tr.fontHeight;
		int wrappedHeight = lines.size() * lineHeight;

		int wrappedWidth = 0;
		for (OrderedText line : lines) {
			wrappedWidth = Math.max(wrappedWidth, tr.getWidth(line));
		}

		// 2. Calculate aligned position using WRAPPED size
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

		// 3. Draw
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
