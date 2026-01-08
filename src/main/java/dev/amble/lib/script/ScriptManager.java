package dev.amble.lib.script;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.gui.lua.LuaBinder;
import dev.amble.lib.client.gui.lua.mc.MinecraftData;
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

                // Inject minecraft global for scripts to use
                globals.set("minecraft", LuaBinder.bind(new MinecraftData()));

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
        
        // Call onEnable
        if (script.onEnable() != null && !script.onEnable().isnil()) {
            try {
                script.onEnable().call();
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
        
        // Call onDisable before removing
        if (script != null && script.onDisable() != null && !script.onDisable().isnil()) {
            try {
                script.onDisable().call();
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
                    script.onTick().call();
                } catch (Exception e) {
                    AmbleKit.LOGGER.error("Error in onTick for script {}", id, e);
                    // Optionally disable the script on error
                    // disable(id);
                }
            }
        }
    }
}
