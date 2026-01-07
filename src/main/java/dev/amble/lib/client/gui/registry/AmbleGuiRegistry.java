package dev.amble.lib.client.gui.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.gui.*;
import dev.amble.lib.register.datapack.DatapackRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
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
			throw new IllegalStateException("Amble container is missing background data");
		}

		Rectangle layout = new Rectangle();
		if (json.has("layout") && json.get("layout").isJsonArray()) {
			var layoutArray = json.get("layout").getAsJsonArray();
			layout.setSize(layoutArray.get(0).getAsInt(), layoutArray.get(1).getAsInt());
		} else {
			throw new IllegalStateException("Amble container is missing layout data");
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
				throw new IllegalStateException("UI Alignment must be array [horizontal, vertical]");
			}

			var alignmentArray = json.get("alignment").getAsJsonArray();
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

		List<AmbleElement> children = new ArrayList<>();
		if (json.has("children")) {
			if (!json.get("children").isJsonArray()) {
				throw new IllegalStateException("UI children should be an object array of other ui elements");
			}

			var childrenArray = json.get("children").getAsJsonArray();

			for (int i = 0; i < childrenArray.size(); i++) {
				if (!(childrenArray.get(i).isJsonObject())) {
					throw new IllegalStateException("UI child at index " + i + " is invalid, got " + childrenArray.get(i));
				}

				children.add(parse(childrenArray.get(i).getAsJsonObject()));
			}
		}

		boolean requiresNewRow = false;
		if (json.has("requires_new_row")) {
			if (!json.get("requires_new_row").isJsonPrimitive()) {
				throw new IllegalStateException("UI requires_new_row should be boolean");
			}
			requiresNewRow = json.get("requires_new_row").getAsBoolean();
		}

		AmbleContainer created = AmbleContainer.builder().background(background).layout(layout).preferredLayout(layout).padding(padding).spacing(spacing).horizontalAlign(horizAlign).verticalAlign(vertAlign).children(children).requiresNewRow(requiresNewRow).build();

		// TODO - buttons
		if (json.has("text")) {
			String text = json.get("text").getAsString();
			created = AmbleText.of(created, Text.translatable(text));

			UIAlign textHorizAlign = UIAlign.CENTRE;
			UIAlign textVertAlign = UIAlign.CENTRE;
			if (json.has("text_alignment")) {
				if (!json.get("text_alignment").isJsonArray()) {
					throw new IllegalStateException("UI text Alignment must be array [horizontal, vertical]");
				}

				var alignmentArray = json.get("text_alignment").getAsJsonArray();
				String horizAlignKey = alignmentArray.get(0).getAsString();
				String vertAlignKey = alignmentArray.get(1).getAsString();

				// try parse to enums
				textHorizAlign = UIAlign.valueOf(horizAlignKey.toUpperCase());
				textVertAlign = UIAlign.valueOf(vertAlignKey.toUpperCase());

				((AmbleText) created).setTextHorizontalAlign(textHorizAlign);
				((AmbleText) created).setTextVerticalAlign(textVertAlign);
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
				AmbleKit.LOGGER.error("Error occurred while loading resource registry {}", rawId.toString(), e);
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
