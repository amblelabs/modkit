package dev.amble.lib.client.gui;

import com.google.gson.JsonElement;
import dev.amble.lib.api.Identifiable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public record AmbleDisplayType(@Nullable Color color, @Nullable TextureData texture) {
	public AmbleDisplayType {
		if (color == null && texture == null) {
			throw new IllegalArgumentException("Either color or texture must be provided");
		}
	}

	public void render(DrawContext context, Rectangle layout) {
		if (color != null) {
			context.fill(layout.x, layout.y, layout.x + layout.width, layout.y + layout.height,
					color.getRGB());
		} else if (texture != null) {
			texture.render(context, layout);
		}
	}

	public static AmbleDisplayType color(Color color) {
		return new AmbleDisplayType(color, null);
	}

	public static AmbleDisplayType texture(TextureData identifier) {
		return new AmbleDisplayType(null, identifier);
	}

	public static AmbleDisplayType parse(JsonElement element) {
		if (element.isJsonArray()) {
			// parse 3 element array as RGB color, 4th element optional alpha
			var arr = element.getAsJsonArray();
			int r = arr.get(0).getAsInt();
			int g = arr.get(1).getAsInt();
			int b = arr.get(2).getAsInt();
			int a = arr.size() > 3 ? arr.get(3).getAsInt() : 255;
			return AmbleDisplayType.color(new Color(r, g, b, a));
		} else if (element.isJsonObject()) {
			var obj = element.getAsJsonObject();
			Identifier texture = new Identifier(obj.get("texture").getAsString());
			int u = obj.get("u").getAsInt();
			int v = obj.get("v").getAsInt();
			int regionWidth = obj.get("regionWidth").getAsInt();
			int regionHeight = obj.get("regionHeight").getAsInt();
			int textureWidth = obj.get("textureWidth").getAsInt();
			int textureHeight = obj.get("textureHeight").getAsInt();
			return AmbleDisplayType.texture(new TextureData(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight));
		}

		throw new IllegalArgumentException("Invalid AmbleDisplayType JSON element");
	}

	public record TextureData(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		public void render(DrawContext context, Rectangle layout) {
			context.drawTexture(texture, layout.x, layout.y, layout.width, layout.height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
		}
	}
}
