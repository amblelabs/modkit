package dev.amble.lib.client.gui.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.gui.*;
import dev.amble.lib.script.ScriptManager;
import dev.amble.lib.register.datapack.DatapackRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.NotImplementedException;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO: Consider removing dependency on DatapackRegistry - see team discussion
public class AmbleGuiRegistry extends DatapackRegistry<AmbleContainer> implements SimpleSynchronousResourceReloadListener {
	private static final AmbleGuiRegistry INSTANCE = new AmbleGuiRegistry();
	private final List<AmbleElementParser> parsers = new CopyOnWriteArrayList<>();

	private AmbleGuiRegistry() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);

		// Register default parsers
		registerParser(new AmbleButton.Parser());
		registerParser(new AmbleText.Parser());
	}

	/**
	 * Registers a custom element parser.
	 * <p>
	 * Parsers are called in order of priority (highest first) when parsing JSON.
	 * If a parser returns null, the next parser is tried. If all custom parsers
	 * return null, the default parsing logic is used.
	 *
	 * @param parser the parser to register
	 */
	public void registerParser(AmbleElementParser parser) {
		parsers.add(parser);
		parsers.sort(Comparator.comparingInt(AmbleElementParser::priority).reversed());
	}

	/**
	 * Unregisters a custom element parser.
	 *
	 * @param parser the parser to unregister
	 * @return true if the parser was found and removed
	 */
	public boolean unregisterParser(AmbleElementParser parser) {
		return parsers.remove(parser);
	}

	/**
	 * Initializes the GUI registry and related systems.
	 * Should be called during client initialization.
	 */
	public static void init() {
		getInstance();
		ScriptManager.getInstance();
	}

	@Override
	public AmbleContainer fallback() {
		throw new NotImplementedException();
	}

	@Override
	public Identifier getFabricId() {
		return AmbleKit.id("gui");
	}

	/**
	 * Parses a JSON object into an AmbleContainer.
	 *
	 * @param json the JSON object to parse
	 * @return the parsed AmbleContainer
	 * @throws IllegalStateException if required fields are missing or invalid
	 */
	public static AmbleContainer parse(JsonObject json) {
		return parse(json, null);
	}

	/**
	 * Parses a JSON object into an AmbleContainer.
	 * <p>
	 * This method first parses the base container properties, then checks all
	 * registered custom parsers in order of priority. If a parser returns a
	 * non-null result, that result is returned. Otherwise, the base container is returned.
	 *
	 * @param json the JSON object to parse
	 * @param resourceId the identifier of the resource being parsed (for error context), may be null
	 * @return the parsed AmbleContainer
	 * @throws IllegalStateException if required fields are missing or invalid
	 */
	public static AmbleContainer parse(JsonObject json, Identifier resourceId) {
		// First parse the base container with all standard properties
		AmbleContainer base = parseBase(json, resourceId);

		// Check custom parsers, passing the base container
		for (AmbleElementParser parser : INSTANCE.parsers) {
			AmbleContainer result = parser.parse(json, resourceId, base);
			if (result != null) {
				return result;
			}
		}

		// No parser handled it, return the base container
		return base;
	}

	/**
	 * Parses the base AmbleContainer properties from JSON.
	 * <p>
	 * This method parses all standard container properties (layout, background,
	 * padding, spacing, alignment, children, etc.) and returns a base AmbleContainer.
	 * Custom parsers can then copy this state to their custom element types.
	 *
	 * @param json the JSON object to parse
	 * @param resourceId the identifier of the resource being parsed (for error context), may be null
	 * @return the parsed base AmbleContainer
	 * @throws IllegalStateException if required fields are missing or invalid
	 */
	public static AmbleContainer parseBase(JsonObject json, Identifier resourceId) {
		String context = resourceId != null ? " (resource: " + resourceId + ")" : "";

		// first parse background
		AmbleDisplayType background;
		if (json.has("background")) {
			background = AmbleDisplayType.parse(json.get("background"));
		} else {
			throw new IllegalStateException("Amble container is missing background data" + context);
		}

		Rectangle layout = new Rectangle();
		if (json.has("layout") && json.get("layout").isJsonArray()) {
			JsonArray layoutArray = json.get("layout").getAsJsonArray();
			if (layoutArray.size() < 2) {
				throw new IllegalStateException("Amble container layout must have at least 2 elements (width, height)" + context);
			}
			layout.setSize(layoutArray.get(0).getAsInt(), layoutArray.get(1).getAsInt());
		} else {
			throw new IllegalStateException("Amble container is missing layout data" + context);
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
				throw new IllegalStateException("UI Alignment must be array [horizontal, vertical]" + context);
			}

			JsonArray alignmentArray = json.get("alignment").getAsJsonArray();
			if (alignmentArray.size() < 2) {
				throw new IllegalStateException("UI Alignment array must have at least 2 elements" + context);
			}
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
				throw new IllegalStateException("UI should_pause should be boolean" + context);
			}

			shouldPause = json.get("should_pause").getAsBoolean();
		}

		List<AmbleElement> children = new ArrayList<>();
		if (json.has("children")) {
			if (!json.get("children").isJsonArray()) {
				throw new IllegalStateException("UI children should be an object array of other ui elements" + context);
			}

			JsonArray childrenArray = json.get("children").getAsJsonArray();

			for (int i = 0; i < childrenArray.size(); i++) {
				if (!(childrenArray.get(i).isJsonObject())) {
					throw new IllegalStateException("UI child at index " + i + " is invalid, got " + childrenArray.get(i) + context);
				}

				children.add(parse(childrenArray.get(i).getAsJsonObject(), resourceId));
			}
		}

		boolean requiresNewRow = false;
		if (json.has("requires_new_row")) {
			if (!json.get("requires_new_row").isJsonPrimitive()) {
				throw new IllegalStateException("UI requires_new_row should be boolean" + context);
			}
			requiresNewRow = json.get("requires_new_row").getAsBoolean();
		}

		AmbleContainer created = AmbleContainer.builder().background(background).layout(layout).padding(padding).spacing(spacing).horizontalAlign(horizAlign).verticalAlign(vertAlign).children(children).shouldPause(shouldPause).requiresNewRow(requiresNewRow).build();

		if (json.has("id")) {
			String idStr = json.get("id").getAsString();
			Identifier parsedId = Identifier.tryParse(idStr);
			if (parsedId == null) {
				throw new IllegalStateException("Invalid identifier '" + idStr + "'" + context);
			}
			created.setIdentifier(parsedId);
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
				AmbleContainer model = parse(json, id);
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
