package dev.amble.lib.client.gui.lua;

import dev.amble.lib.AmbleKit;
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

public class GuiScriptManager implements SimpleSynchronousResourceReloadListener {
	private static final GuiScriptManager INSTANCE = new GuiScriptManager();
    private static final Map<Identifier, GuiScript> CACHE = new HashMap<>();

	private GuiScriptManager() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
				.registerReloadListener(this);
	}

	public static GuiScriptManager getInstance() {
		return INSTANCE;
	}

    @Override
    public Identifier getFabricId() {
        return AmbleKit.id("gui_scripts");
    }

    @Override
    public void reload(ResourceManager manager) {
        CACHE.clear();
    }

    public static GuiScript load(Identifier id, ResourceManager manager) {
        return CACHE.computeIfAbsent(id, key -> {
            try {
                Resource res = manager.getResource(key).orElseThrow();
                Globals globals = JsePlatform.standardGlobals();

                LuaValue chunk = globals.load(
                        new InputStreamReader(res.getInputStream()),
                        key.toString()
                );
                chunk.call();

                return new GuiScript(
                        globals.get("onInit"),
                        globals.get("onClick"),
                        globals.get("onRelease"),
                        globals.get("onHover")
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to load GUI script " + key, e);
            }
        });
    }
}
