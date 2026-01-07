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

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AmbleText extends AmbleContainer {
	private Text text;
	private UIAlign textHorizontalAlign = UIAlign.CENTRE;
	private UIAlign textVerticalAlign = UIAlign.CENTRE;

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		// Calculate text position based on alignment
		int textX = getLayout().x;
		int textY = getLayout().y;
		int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(text);
		int textHeight = MinecraftClient.getInstance().textRenderer.fontHeight;
		switch (textHorizontalAlign) {
			case START -> textX += getPadding();
			case CENTRE -> textX += (getLayout().width - textWidth) / 2;
			case END -> textX += getLayout().width - textWidth - getPadding();
		}
		switch (textVerticalAlign) {
			case START -> textY += getPadding();
			case CENTRE -> textY += (getLayout().height - textHeight) / 2;
			case END -> textY += getLayout().height - textHeight - getPadding();
		}

		// Draw the text
		drawTextWrappedWithShadow(
				context,
				MinecraftClient.getInstance().textRenderer,
				text,
				textX, textY,
				getLayout().width,
				0xFFFFFF
		);
	}

	public static void drawTextWrappedWithShadow(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int width, int color) {
		for (OrderedText orderedText : textRenderer.wrapLines(text, width)) {
			context.drawText(textRenderer, orderedText, x, y, color, true);
			y += 9;
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
	}
}
