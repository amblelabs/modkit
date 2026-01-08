package dev.amble.lib.script;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.lua.ClientMinecraftData;
import dev.amble.lib.script.lua.LuaBinder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScriptManager implements SimpleSynchronousResourceReloadListener {
	private static final ScriptManager INSTANCE = new ScriptManager();
    private static final Map<Identifier, AmbleScript> CACHE = new HashMap<>();
    private static final Map<Identifier, LuaValue> DATA_CACHE = new HashMap<>();
    private static final Set<Identifier> ENABLED_SCRIPTS = new HashSet<>();

	private ScriptManager() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
				.registerReloadListener(this);
	}

	public static ScriptManager getInstance() {
		return INSTANCE;
	}

    @Override
    public Identifier getFabricId() {
        return AmbleKit.id("scripts");
    }

    @Override
    public void reload(ResourceManager manager) {
        // Disable all scripts before clearing cache
        for (Identifier id : new HashSet<>(ENABLED_SCRIPTS)) {
            disable(id);
        }
        
        CACHE.clear();
        DATA_CACHE.clear();
        
        // Discover all script files and populate the cache for suggestions
        manager.findResources("script", id -> id.getPath().endsWith(".lua"))
                .keySet()
                .forEach(id -> {
                    try {
                        load(id, manager);
                    } catch (Exception e) {
                        AmbleKit.LOGGER.error("Failed to load script {}", id, e);
                    }
                });
        
        AmbleKit.LOGGER.info("Loaded {} scripts", CACHE.size());
    }

    public static AmbleScript load(Identifier id, ResourceManager manager) {
        return CACHE.computeIfAbsent(id, key -> {
            try {
                Resource res = manager.getResource(key).orElseThrow();
                Globals globals = JsePlatform.standardGlobals();

                // Create and cache the minecraft data for this script
                ClientMinecraftData data = new ClientMinecraftData();
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
                throw new RuntimeException("Failed to load script " + key, e);
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
                AmbleKit.LOGGER.error("Error in onEnable for script {}", id, e);
            }
        }
        
        AmbleKit.LOGGER.info("Enabled script: {}", id);
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
                AmbleKit.LOGGER.error("Error in onDisable for script {}", id, e);
            }
        }
        
        ENABLED_SCRIPTS.remove(id);
        AmbleKit.LOGGER.info("Disabled script: {}", id);
        return true;
    }

    public static boolean toggle(Identifier id) {
        if (isEnabled(id)) {
            return disable(id);
        } else {
            return enable(id);
        }
    }

    public static void tick() {
        for (Identifier id : ENABLED_SCRIPTS) {
            AmbleScript script = CACHE.get(id);
            if (script != null && script.onTick() != null && !script.onTick().isnil()) {
                try {
                    LuaValue data = DATA_CACHE.get(id);
                    script.onTick().call(data);
                } catch (Exception e) {
                    AmbleKit.LOGGER.error("Error in onTick for script {}", id, e);
                    // Optionally disable the script on error
                    // disable(id);
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
