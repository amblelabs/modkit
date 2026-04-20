package dev.amble.lib.script;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.lua.ClientMinecraftData;
import dev.amble.lib.script.lua.MinecraftData;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

/**
 * Client-side script manager for loading and managing Lua scripts from asset packs.
 * Scripts are loaded from assets/&lt;namespace&gt;/script/*.lua
 */
public class ScriptManager extends AbstractScriptManager {
	private static final ScriptManager INSTANCE = new ScriptManager();

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
	protected MinecraftData createMinecraftData() {
		return new ClientMinecraftData();
	}

	@Override
	protected String getLogPrefix() {
		return "script";
	}
}
