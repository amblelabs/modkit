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
import java.util.Map;

public class ScriptManager implements SimpleSynchronousResourceReloadListener {
	private static final ScriptManager INSTANCE = new ScriptManager();
    private static final Map<Identifier, AmbleScript> CACHE = new HashMap<>();

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
                        globals.get("onExecute")
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to load script " + key, e);
            }
        });
    }

    public static Map<Identifier, AmbleScript> getCache() {
        return CACHE;
    }
}
