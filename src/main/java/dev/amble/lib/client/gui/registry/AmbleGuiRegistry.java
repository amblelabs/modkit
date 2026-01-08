package dev.amble.lib.client.gui.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.gui.*;
import dev.amble.lib.client.gui.lua.GuiScript;
import dev.amble.lib.client.gui.lua.GuiScriptManager;
import dev.amble.lib.client.gui.lua.LuaBinder;
import dev.amble.lib.register.datapack.DatapackRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.NotImplementedException;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AmbleGuiRegistry extends DatapackRegistry<AmbleContainer> implements SimpleSynchronousResourceReloadListener {
	private static final AmbleGuiRegistry INSTANCE = new AmbleGuiRegistry();

	private AmbleGuiRegistry() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);

		GuiScriptManager.getInstance();
	}

	@Override
	public AmbleContainer fallback() {
		throw new NotImplementedException();
	}

	@Override
	public Identifier getFabricId() {
		return AmbleKit.id("gui");
	}

	public static AmbleContainer parse(JsonObject json) {
		// first parse background
		AmbleDisplayType background;
		if (json.has("background")) {
			background = AmbleDisplayType.parse(json.get("background"));
		} else {
			throw new IllegalStateException("Amble container is missing background data | " + json);
		}

		Rectangle layout = new Rectangle();
		if (json.has("layout") && json.get("layout").isJsonArray()) {
			JsonArray layoutArray = json.get("layout").getAsJsonArray();
			layout.setSize(layoutArray.get(0).getAsInt(), layoutArray.get(1).getAsInt());
		} else {
			throw new IllegalStateException("Amble container is missing layout data | " + json);
		}

		int padding = 0;
		if (json.has("padding")) {
			padding = json.get("padding").getAsInt();
		}

		int spacing = 0;
		if (json.has("spacing")) {
			spacing = json.get("spacing").getAsInt();
		}

		UIAlign horizAlign = UIAlign.START;
		UIAlign vertAlign = UIAlign.START;
		if (json.has("alignment")) {
			if (!json.get("alignment").isJsonArray()) {
				throw new IllegalStateException("UI Alignment must be array [horizontal, vertical] | " + json);
			}

			JsonArray alignmentArray = json.get("alignment").getAsJsonArray();
			String horizAlignKey = alignmentArray.get(0).getAsString();
			String vertAlignKey = alignmentArray.get(1).getAsString();

			if (vertAlignKey.equalsIgnoreCase("center")) {
				vertAlignKey = "centre";
			}

			if (horizAlignKey.equalsIgnoreCase("center")) {
				horizAlignKey = "centre";
			}

			// try parse to enums
			horizAlign = UIAlign.valueOf(horizAlignKey.toUpperCase());
			vertAlign = UIAlign.valueOf(vertAlignKey.toUpperCase());
		}

		boolean shouldPause = false;
		if (json.has("should_pause")) {
			if (!json.get("should_pause").isJsonPrimitive()) {
				throw new IllegalStateException("UI should_pause should be boolean | " + json);
			}

			shouldPause = json.get("should_pause").getAsBoolean();
		}

		List<AmbleElement> children = new ArrayList<>();
		if (json.has("children")) {
			if (!json.get("children").isJsonArray()) {
				throw new IllegalStateException("UI children should be an object array of other ui elements | " + json);
			}

			JsonArray childrenArray = json.get("children").getAsJsonArray();

			for (int i = 0; i < childrenArray.size(); i++) {
				if (!(childrenArray.get(i).isJsonObject())) {
					throw new IllegalStateException("UI child at index " + i + " is invalid, got " + childrenArray.get(i) + " | " + json);
				}

				children.add(parse(childrenArray.get(i).getAsJsonObject()));
			}
		}

		boolean requiresNewRow = false;
		if (json.has("requires_new_row")) {
			if (!json.get("requires_new_row").isJsonPrimitive()) {
				throw new IllegalStateException("UI requires_new_row should be boolean | " + json);
			}
			requiresNewRow = json.get("requires_new_row").getAsBoolean();
		}

		AmbleContainer created = AmbleContainer.builder().background(background).layout(layout).padding(padding).spacing(spacing).horizontalAlign(horizAlign).verticalAlign(vertAlign).children(children).shouldPause(shouldPause).requiresNewRow(requiresNewRow).build();

		if (json.has("id")) {
			String idStr = json.get("id").getAsString();
			created.setIdentifier(new Identifier(idStr));
		}

		if (json.has("text")) {
			String text = json.get("text").getAsString();
			AmbleText ambleText = AmbleText.textBuilder().text(Text.translatable(text)).build();
			ambleText.copyFrom(created);
			created = ambleText;

			UIAlign textHorizAlign = UIAlign.CENTRE;
			UIAlign textVertAlign = UIAlign.CENTRE;
			if (json.has("text_alignment")) {
				if (!json.get("text_alignment").isJsonArray()) {
					throw new IllegalStateException("UI text Alignment must be array [horizontal, vertical] | " + json);
				}

				JsonArray alignmentArray = json.get("text_alignment").getAsJsonArray();
				String horizAlignKey = alignmentArray.get(0).getAsString();
				String vertAlignKey = alignmentArray.get(1).getAsString();

				// try parse to enums
				textHorizAlign = UIAlign.valueOf(horizAlignKey.toUpperCase());
				textVertAlign = UIAlign.valueOf(vertAlignKey.toUpperCase());

				((AmbleText) created).setTextHorizontalAlign(textHorizAlign);
				((AmbleText) created).setTextVerticalAlign(textVertAlign);
			}
		}

		if (json.has("command") || json.has("script") || json.has("hover_background") || json.has("press_background")) {
			AmbleButton button = AmbleButton.buttonBuilder().build();
			button.copyFrom(created);
			created = button;

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
				Identifier scriptId = new Identifier(json.get("script").getAsString()).withPrefixedPath("gui/script/").withSuffixedPath(".lua");
				GuiScript script = GuiScriptManager.load(
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
		}

		return created;
	}

	@Override
	public void reload(ResourceManager manager) {
		clearCache();

		for (Identifier rawId : manager.findResources("gui", filename -> filename.getPath().endsWith(".json")).keySet()) {
			try (InputStream stream = manager.getResource(rawId).get().getInputStream()) {
				String path = rawId.getPath();
				// remove "gui/" prefix and ".json" suffix
				String idPath = path.substring("gui/".length(), path.length() - ".json".length());
				Identifier id = Identifier.of(rawId.getNamespace(), idPath);

				JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
				AmbleContainer model = parse(json);
				model.setIdentifier(id);

				register(model);

				AmbleKit.LOGGER.debug("Loaded AmbleContainer {} {}", id, model);
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error occurred while loading resource json {}", rawId.toString(), e);
			}
		}
	}

	@Override
	public void syncToClient(ServerPlayerEntity player) {
		throw new UnsupportedOperationException("Client-side only registry");
	}

	@Override
	public void readFromServer(PacketByteBuf buf) {
		throw new UnsupportedOperationException("Client-side only registry");
	}

	public static AmbleGuiRegistry getInstance() {
		return INSTANCE;
	}
}
