package dev.amble.lib.script;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.lua.LuaBinder;
import dev.amble.lib.script.lua.ServerMinecraftData;
import dev.amble.lib.util.ServerLifecycleHooks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages server-side Lua scripts loaded from the data folder.
 * Scripts are loaded from data/&lt;namespace&gt;/script/*.lua
 */
public class ServerScriptManager implements SimpleSynchronousResourceReloadListener {
	private static final ServerScriptManager INSTANCE = new ServerScriptManager();
	private static final Map<Identifier, AmbleScript> CACHE = new HashMap<>();
	private static final Map<Identifier, LuaValue> DATA_CACHE = new HashMap<>();
	private static final Set<Identifier> ENABLED_SCRIPTS = new HashSet<>();
	
	private MinecraftServer currentServer;
	private boolean initialized = false;

	private ServerScriptManager() {
	}

	public static ServerScriptManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Initialize the server script manager. Should be called from main mod initializer.
	 */
	public void init() {
		if (initialized) return;
		initialized = true;

		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
				.registerReloadListener(this);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			this.currentServer = server;
			AmbleKit.LOGGER.info("Server script manager ready");
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			// Disable all scripts before server stops
			for (Identifier id : new HashSet<>(ENABLED_SCRIPTS)) {
				disable(id);
			}
			this.currentServer = null;
		});

		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
	}

	@Override
	public Identifier getFabricId() {
		return AmbleKit.id("server_scripts");
	}

	@Override
	public void reload(ResourceManager manager) {
		// Disable all scripts before clearing cache
		for (Identifier id : new HashSet<>(ENABLED_SCRIPTS)) {
			disable(id);
		}

		CACHE.clear();
		DATA_CACHE.clear();

		// Discover all script files and populate the cache
		manager.findResources("script", id -> id.getPath().endsWith(".lua"))
				.keySet()
				.forEach(id -> {
					try {
						load(id, manager);
					} catch (Exception e) {
						AmbleKit.LOGGER.error("Failed to load server script {}", id, e);
					}
				});

		AmbleKit.LOGGER.info("Loaded {} server scripts", CACHE.size());
	}

	public AmbleScript load(Identifier id, ResourceManager manager) {
		return CACHE.computeIfAbsent(id, key -> {
			try {
				Resource res = manager.getResource(key).orElseThrow();
				Globals globals = JsePlatform.standardGlobals();

				// Create server minecraft data - world will be set when script is enabled/executed
				MinecraftServer server = currentServer != null ? currentServer : ServerLifecycleHooks.get();
				ServerWorld world = server != null ? server.getOverworld() : null;
				ServerMinecraftData data = new ServerMinecraftData(server, world);
				LuaValue boundData = LuaBinder.bind(data);
				DATA_CACHE.put(key, boundData);

				// Inject minecraft global for scripts to use (backward compatibility)
				globals.set("minecraft", boundData);

				LuaValue chunk = globals.load(
						new InputStreamReader(res.getInputStream()),
						key.toString()
				);
				chunk.call();

				return new AmbleScript(
						globals.get("onInit"),
						globals.get("onClick"),
						globals.get("onRelease"),
						globals.get("onHover"),
						globals.get("onExecute"),
						globals.get("onEnable"),
						globals.get("onTick"),
						globals.get("onDisable")
				);
			} catch (Exception e) {
				throw new RuntimeException("Failed to load server script " + key, e);
			}
		});
	}

	public static Map<Identifier, AmbleScript> getCache() {
		return CACHE;
	}

	public static Set<Identifier> getEnabledScripts() {
		return ENABLED_SCRIPTS;
	}

	public static boolean isEnabled(Identifier id) {
		return ENABLED_SCRIPTS.contains(id);
	}

	public static boolean enable(Identifier id) {
		if (ENABLED_SCRIPTS.contains(id)) {
			return false; // Already enabled
		}

		AmbleScript script = CACHE.get(id);
		if (script == null) {
			return false;
		}

		ENABLED_SCRIPTS.add(id);

		// Call onEnable with minecraft data as first argument
		if (script.onEnable() != null && !script.onEnable().isnil()) {
			try {
				LuaValue data = DATA_CACHE.get(id);
				script.onEnable().call(data);
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error in onEnable for server script {}", id, e);
			}
		}

		AmbleKit.LOGGER.info("Enabled server script: {}", id);
		return true;
	}

	public static boolean disable(Identifier id) {
		if (!ENABLED_SCRIPTS.contains(id)) {
			return false; // Not enabled
		}

		AmbleScript script = CACHE.get(id);

		// Call onDisable with minecraft data as first argument before removing
		if (script != null && script.onDisable() != null && !script.onDisable().isnil()) {
			try {
				LuaValue data = DATA_CACHE.get(id);
				script.onDisable().call(data);
			} catch (Exception e) {
				AmbleKit.LOGGER.error("Error in onDisable for server script {}", id, e);
			}
		}

		ENABLED_SCRIPTS.remove(id);
		AmbleKit.LOGGER.info("Disabled server script: {}", id);
		return true;
	}

	public static boolean toggle(Identifier id) {
		if (isEnabled(id)) {
			return disable(id);
		} else {
			return enable(id);
		}
	}

	private void onServerTick(MinecraftServer server) {
		for (Identifier id : ENABLED_SCRIPTS) {
			AmbleScript script = CACHE.get(id);
			if (script != null && script.onTick() != null && !script.onTick().isnil()) {
				try {
					LuaValue data = DATA_CACHE.get(id);
					script.onTick().call(data);
				} catch (Exception e) {
					AmbleKit.LOGGER.error("Error in onTick for server script {}", id, e);
				}
			}
		}
	}

	/**
	 * Get the bound minecraft data for a script.
	 */
	public static LuaValue getScriptData(Identifier id) {
		return DATA_CACHE.get(id);
	}
}
