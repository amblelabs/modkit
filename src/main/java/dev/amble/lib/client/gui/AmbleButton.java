package dev.amble.lib.client.gui;


import com.google.gson.JsonObject;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.gui.registry.AmbleElementParser;
import dev.amble.lib.script.lua.LuaBinder;
import dev.amble.lib.client.gui.lua.LuaElement;
import dev.amble.lib.script.LuaScript;
import dev.amble.lib.script.ScriptManager;
import lombok.*;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.awt.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AmbleButton extends AmbleContainer implements Focusable {
	private AmbleDisplayType hoverDisplay;
	private AmbleDisplayType pressDisplay;
	private @Nullable Runnable onClick;
	private @Nullable AmbleDisplayType normalDisplay = null;
	private boolean isClicked = false;
	private @Nullable LuaScript script;
	private boolean focused = false;

	@Override
	public void onRelease(double mouseX, double mouseY, int button) {
		if (onClick != null) {
			onClick.run();
		}
		this.setBackground(
				isHovered(mouseX, mouseY) ? hoverDisplay : getNormalDisplay()
		);
		this.isClicked = false;

		if (script != null && script.onRelease() != null && !script.onRelease().isnil()) {
			Varargs args = LuaValue.varargsOf(new LuaValue[]{
					LuaBinder.bind(new LuaElement(this)),
					LuaValue.valueOf(mouseX),
					LuaValue.valueOf(mouseY),
					LuaValue.valueOf(button)
			});

			try {
				script.onRelease().invoke(args);
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error invoking onRelease script for AmbleButton {}:", id(), e);
			}
		}
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button) {
		this.setBackground(pressDisplay);
		this.isClicked = true;


		if (script != null && script.onClick() != null && !script.onClick().isnil()) {
			Varargs args = LuaValue.varargsOf(new LuaValue[]{
					LuaBinder.bind(new LuaElement(this)),
					LuaValue.valueOf(mouseX),
					LuaValue.valueOf(mouseY),
					LuaValue.valueOf(button)
			});

			try {
				script.onClick().invoke(args);
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error invoking onClick script for AmbleButton {}:", id(), e);
			}
		}
	}

	public void onHover(double mouseX, double mouseY) {
		if (script != null && script.onHover() != null && !script.onHover().isnil()) {
			Varargs args = LuaValue.varargsOf(new LuaValue[]{
					LuaBinder.bind(new LuaElement(this)),
					LuaValue.valueOf(mouseX),
					LuaValue.valueOf(mouseY),
			});

			try {
				script.onHover().invoke(args);
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error invoking onHover script for AmbleButton {}:", id(), e);
			}
		}
	}

	public @Nullable AmbleDisplayType getNormalDisplay() {
		if (normalDisplay == null) {
			normalDisplay = this.getBackground();
		}

		return normalDisplay;
	}

	private boolean displayCalled = false;

	public void setScript(LuaScript script) {
		this.script = script;
		this.displayCalled = false;

		// Call onAttached immediately when script is attached (during JSON parsing)
		if (script.onAttached() != null && !script.onAttached().isnil()) {
			try {
				script.onAttached().call(LuaBinder.bind(new LuaElement(this)));
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error invoking onAttached script for AmbleButton {}:", id(), e);
			}
		}
	}

	/**
	 * Calls onDisplay if it hasn't been called yet.
	 * This is deferred until first render so the GUI tree is fully built.
	 */
	private void ensureDisplayCalled() {
		if (!displayCalled && script != null && script.onDisplay() != null && !script.onDisplay().isnil()) {
			displayCalled = true;
			try {
				script.onDisplay().call(LuaBinder.bind(new LuaElement(this)));
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error invoking onDisplay script for AmbleButton {}:", id(), e);
			}
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		// Call onDisplay on first render when GUI tree is fully built
		ensureDisplayCalled();

		if (isHovered(mouseX, mouseY)) {
			onHover(mouseX, mouseY);
		}

		if (isClicked) {
			setBackground(pressDisplay);
		} else if (isHovered(mouseX, mouseY) || focused) {
			setBackground(hoverDisplay);
		} else {
			setBackground(getNormalDisplay());
		}

		super.render(context, mouseX, mouseY, delta);
	}


	// ===== Focusable interface implementation =====

	@Override
	public boolean canFocus() {
		return isVisible();
	}

	@Override
	public boolean isFocused() {
		return focused;
	}

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	@Override
	public void onFocusChanged(boolean focused) {
		this.focused = focused;
	}

	@Override
	public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
		if (!focused) return false;

		// Enter or Space to activate the button
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
			// Simulate click
			Rectangle layout = getLayout();
			double centerX = layout.x + layout.width / 2.0;
			double centerY = layout.y + layout.height / 2.0;
			onClick(centerX, centerY, 0);
			onRelease(centerX, centerY, 0);
			return true;
		}

		return false;
	}

	@Override
	public boolean onCharTyped(char chr, int modifiers) {
		return false;
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

	/**
	 * Parser for AmbleButton elements.
	 * <p>
	 * This parser handles JSON objects that have button-specific properties:
	 * on_click, script, hover_background, or press_background.
	 */
	public static class Parser implements AmbleElementParser {

		@Override
		public @Nullable AmbleContainer parse(JsonObject json, @Nullable Identifier resourceId, AmbleContainer base) {
			// Check if this is a button (has button-specific properties)
			boolean isButton = json.has("on_click") || json.has("script") || json.has("hover_background") || json.has("press_background");

			if (!isButton) {
				return null;
			}

			// Handle text for button - use AmbleText.Parser to create the text child
			if (json.has("text")) {
				// Create a temporary container to parse text into
				AmbleText textChild = (AmbleText) new AmbleText.Parser().parse(json, resourceId,
						AmbleText.textBuilder()
								.layout(new Rectangle(base.getLayout()))
								.background(new Color(0, 0, 0, 0))
								.build());
				if (textChild != null) {
					base.addChild(textChild);
				}
			}

			AmbleButton button = AmbleButton.buttonBuilder().build();
			button.copyFrom(base);

			if (json.has("on_click")) {
				// todo run actual java methods via reflection
				String clickCommand = json.get("on_click").getAsString();
				button.setOnClick(() -> {
					try {
						String string2 = SharedConstants.stripInvalidChars(clickCommand);
						if (string2.startsWith("/")) {
							if (!MinecraftClient.getInstance().player.networkHandler.sendCommand(string2.substring(1))) {
								AmbleKit.LOGGER.error("Not allowed to run command with signed argument from click event: '{}'", string2);
							}
						} else {
							AmbleKit.LOGGER.error("Failed to run command without '/' prefix from click event: '{}'", string2);
						}
					} catch (Exception e) {
						AmbleKit.LOGGER.error("Error occurred while running command from click event: '{}'", clickCommand, e);
					}
				});
			} else {
				button.setOnClick(() -> {
				});
			}

			if (json.has("script")) {
				Identifier scriptId = new Identifier(json.get("script").getAsString()).withPrefixedPath("script/").withSuffixedPath(".lua");
				LuaScript script = ScriptManager.getInstance().load(
						scriptId,
						MinecraftClient.getInstance().getResourceManager()
				);

				button.setScript(script);
			}

			if (json.has("hover_background")) {
				AmbleDisplayType hoverBg = AmbleDisplayType.parse(json.get("hover_background"));
				button.setHoverDisplay(hoverBg);
			} else {
				button.setHoverDisplay(button.getBackground());
			}

			if (json.has("press_background")) {
				AmbleDisplayType pressBg = AmbleDisplayType.parse(json.get("press_background"));
				button.setPressDisplay(pressBg);
			} else {
				button.setPressDisplay(button.getBackground());
			}

			return button;
		}

		@Override
		public int priority() {
			// Button has higher priority than text since buttons can have text
			return 100;
		}
	}
}
