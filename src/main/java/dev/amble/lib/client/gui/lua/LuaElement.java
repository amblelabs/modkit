package dev.amble.lib.client.gui.lua;

import dev.amble.lib.client.gui.*;
import dev.amble.lib.script.lua.ClientMinecraftData;
import dev.amble.lib.script.lua.LuaExpose;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.ApiStatus;

public final class LuaElement {

	private final AmbleElement element;
	private final ClientMinecraftData minecraftData = new ClientMinecraftData();

	public LuaElement(AmbleElement element) {
		this.element = element;
	}

	@LuaExpose
	public String id() {
		return element.id().toString();
	}

	@LuaExpose
	public int x() {
		return element.getLayout().x;
	}

	@LuaExpose
	public int y() {
		return element.getLayout().y;
	}

	@LuaExpose
	public int width() {
		return element.getLayout().width;
	}

	@LuaExpose
	public int height() {
		return element.getLayout().height;
	}

	@LuaExpose
	public void setPosition(int x, int y) {
		element.setPosition(new Vec2f(x, y));
	}

	@LuaExpose
	public void setDimensions(int width, int height) {
		element.setDimensions(new Vec2f(width, height));
	}

	@LuaExpose
	public void setVisible(boolean visible) {
		element.setVisible(visible);
	}

	@LuaExpose
	public LuaElement parent() {
		return element.getParent() == null
				? null
				: new LuaElement(element.getParent());
	}

	@LuaExpose
	public LuaElement child(int index) {
		if (index < 0 || index >= element.getChildren().size()) return null;
		return new LuaElement(element.getChildren().get(index));
	}

	@LuaExpose
	public int childCount() {
		return element.getChildren().size();
	}

	@LuaExpose
	public void setText(String text) {
		if (element instanceof AmbleText t) {
			t.setText(Text.literal(text));
		}
	}

	@LuaExpose
	public String getText() {
		if (element instanceof AmbleText t) {
			return t.getText().getString();
		}
		return null;
	}

	@LuaExpose
	public void closeScreen() {
		MinecraftClient.getInstance().setScreen(null);
	}

	@LuaExpose
	public ClientMinecraftData minecraft() {
		return minecraftData;
	}

	/**
	 * Returns the underlying AmbleElement wrapped by this LuaElement.
	 * This method is for internal use only and should not be called from Lua scripts.
	 *
	 * @return the wrapped AmbleElement
	 */
	@ApiStatus.Internal
	AmbleElement unwrap() {
		return element;
	}
}
