package dev.amble.lib.script;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.lua.LuaBinder;
import dev.amble.lib.script.lua.MinecraftData;
import lombok.Getter;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for script managers.
 * Provides common functionality for loading, caching, and managing Lua scripts.
 */
public abstract class AbstractScriptManager implements SimpleSynchronousResourceReloadListener {
    
    @Getter
    protected final Map<Identifier, LuaScript> cache = new HashMap<>();
    protected final Map<Identifier, LuaValue> dataCache = new HashMap<>();
    protected final Map<Identifier, LuaValue> moduleCache = new HashMap<>();
    @Getter
    protected final Set<Identifier> enabledScripts = new HashSet<>();
    @Getter
    protected ResourceManager currentResourceManager;

    /**
     * Creates the MinecraftData instance for a script.
     * Subclasses should override to provide client or server-specific data.
     */
    protected abstract MinecraftData createMinecraftData();

    /**
     * Gets the log prefix for this script manager (e.g., "script" or "server script").
     */
    protected abstract String getLogPrefix();

    /**
     * Reloads all scripts from the given resource manager.
     */
    @Override
    public void reload(ResourceManager manager) {
        this.currentResourceManager = manager;

        // Disable all scripts before clearing cache
        for (Identifier id : new HashSet<>(enabledScripts)) {
            disable(id);
        }

        cache.clear();
        dataCache.clear();
        moduleCache.clear();

        // Discover all script files and populate the cache
        manager.findResources("script", id -> id.getPath().endsWith(".lua"))
                .keySet()
                .forEach(id -> {
                    try {
                        load(id, manager);
                    } catch (Exception e) {
                        AmbleKit.LOGGER.error("Failed to load {} {}", getLogPrefix(), id, e);
                    }
                });

        AmbleKit.LOGGER.info("Loaded {} {}s", cache.size(), getLogPrefix());
    }

    /**
     * Loads a script from the resource manager.
     */
    public LuaScript load(Identifier id, ResourceManager manager) {
        return cache.computeIfAbsent(id, key -> {
            try {
                Resource res = manager.getResource(key).orElseThrow();
                Globals globals = JsePlatform.standardGlobals();

                // Add custom require function
                globals.set("require", createRequireFunction(manager));

                // Create and cache the minecraft data for this script
                MinecraftData data = createMinecraftData();
                data.setScriptName(key.toString());
                LuaValue boundData = LuaBinder.bind(data);
                dataCache.put(key, boundData);

                // Inject minecraft global for scripts to use
                globals.set("minecraft", boundData);

                LuaValue chunk = globals.load(
                        new InputStreamReader(res.getInputStream()),
                        key.toString()
                );
                chunk.call();

                LuaScript script = new LuaScript(globals);

                // Call onRegister when script is first loaded into the manager
                if (script.onRegister() != null && !script.onRegister().isnil()) {
                    try {
                        script.onRegister().call(boundData);
                    } catch (Exception e) {
                        AmbleKit.LOGGER.error("Error in onRegister for {} {}", getLogPrefix(), key, e);
                    }
                }

                return script;
            } catch (Exception e) {
                throw new RuntimeException("Failed to load " + getLogPrefix() + " " + key, e);
            }
        });
    }

    /**
     * Creates a custom require function for loading scripts by namespaced ID.
     * Usage in Lua: require("namespace:script_name")
     */
    protected VarArgFunction createRequireFunction(ResourceManager manager) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String moduleName = args.checkjstring(1);

                // Parse the module name as a namespaced identifier
                Identifier moduleId = parseModuleId(moduleName);

                // Check if already loaded
                if (moduleCache.containsKey(moduleId)) {
                    return moduleCache.get(moduleId);
                }

                // Try to load the module
                try {
                    LuaValue result = loadModule(moduleId, manager);
                    moduleCache.put(moduleId, result);
                    return result;
                } catch (Exception e) {
                    throw new LuaError("Failed to require module '" + moduleName + "': " + e.getMessage());
                }
            }
        };
    }

    /**
     * Parses a module name into an Identifier.
     * Supports formats: "namespace:script_name" or just "script_name" (uses default namespace)
     */
    protected Identifier parseModuleId(String moduleName) {
        // Handle namespaced format (namespace:script_name)
        if (moduleName.contains(":")) {
            String[] parts = moduleName.split(":", 2);
            String namespace = parts[0];
            String path = parts[1];

            // Ensure .lua extension and script/ prefix
            if (!path.endsWith(".lua")) {
                path = path + ".lua";
            }
            if (!path.startsWith("script/")) {
                path = "script/" + path;
            }

            return new Identifier(namespace, path);
        } else {
            // Default namespace (minecraft)
            String path = moduleName;
            if (!path.endsWith(".lua")) {
                path = path + ".lua";
            }
            if (!path.startsWith("script/")) {
                path = "script/" + path;
            }
            return new Identifier(path);
        }
    }

    /**
     * Loads a module script and returns its exported value.
     * Modules should return a value (typically a table) to be used by requiring scripts.
     */
    protected LuaValue loadModule(Identifier moduleId, ResourceManager manager) {
        try {
            Resource res = manager.getResource(moduleId).orElseThrow(() ->
                    new RuntimeException("Module not found: " + moduleId));

            Globals globals = JsePlatform.standardGlobals();

            // Add require function to module globals too (for nested requires)
            globals.set("require", createRequireFunction(manager));

            // Create minecraft data for the module
            MinecraftData data = createMinecraftData();
            data.setScriptName(moduleId.toString());
            LuaValue boundData = LuaBinder.bind(data);
            globals.set("minecraft", boundData);

            LuaValue chunk = globals.load(
                    new InputStreamReader(res.getInputStream()),
                    moduleId.toString()
            );

            // Execute the chunk and get the return value
            LuaValue result = chunk.call();

            // If the script doesn't return anything, return the globals table
            // This allows modules to define functions at global scope
            if (result.isnil()) {
                return globals;
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load module " + moduleId, e);
        }
    }

	public boolean isEnabled(Identifier id) {
        return enabledScripts.contains(id);
    }

    public boolean enable(Identifier id) {
        if (enabledScripts.contains(id)) {
            return false; // Already enabled
        }

        LuaScript script = cache.get(id);
        if (script == null) {
            return false;
        }

        enabledScripts.add(id);

        // Call onEnable with minecraft data as first argument
        if (script.onEnable() != null && !script.onEnable().isnil()) {
            try {
                LuaValue data = dataCache.get(id);
                script.onEnable().call(data);
            } catch (Exception e) {
                AmbleKit.LOGGER.error("Error in onEnable for {} {}", getLogPrefix(), id, e);
            }
        }

        AmbleKit.LOGGER.info("Enabled {}: {}", getLogPrefix(), id);
        return true;
    }

    public boolean disable(Identifier id) {
        if (!enabledScripts.contains(id)) {
            return false; // Not enabled
        }

        LuaScript script = cache.get(id);

        // Call onDisable with minecraft data as first argument before removing
        if (script != null && script.onDisable() != null && !script.onDisable().isnil()) {
            try {
                LuaValue data = dataCache.get(id);
                script.onDisable().call(data);
            } catch (Exception e) {
                AmbleKit.LOGGER.error("Error in onDisable for {} {}", getLogPrefix(), id, e);
            }
        }

        enabledScripts.remove(id);
        AmbleKit.LOGGER.info("Disabled {}: {}", getLogPrefix(), id);
        return true;
    }

    public boolean toggle(Identifier id) {
        if (isEnabled(id)) {
            return disable(id);
        } else {
            return enable(id);
        }
    }

    /**
     * Called each tick to update enabled scripts.
     */
    public void tick() {
        for (Identifier id : enabledScripts) {
            LuaScript script = cache.get(id);
            if (script != null && script.onTick() != null && !script.onTick().isnil()) {
                try {
                    LuaValue data = dataCache.get(id);
                    script.onTick().call(data);
                } catch (Exception e) {
                    AmbleKit.LOGGER.error("Error in onTick for {} {}", getLogPrefix(), id, e);
                }
            }
        }
    }

    /**
     * Get the bound minecraft data for a script.
     */
    public LuaValue getScriptData(Identifier id) {
        return dataCache.get(id);
    }
}
