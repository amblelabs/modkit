package dev.amble.lib.script;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.lua.MinecraftData;
import dev.amble.lib.script.lua.ServerMinecraftData;
import dev.amble.lib.util.ServerLifecycleHooks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.HashSet;

/**
 * Server-side script manager for loading and managing Lua scripts from data packs.
 * Scripts are loaded from data/&lt;namespace&gt;/script/*.lua
 */
public class ServerScriptManager extends AbstractScriptManager {
	private static final ServerScriptManager INSTANCE = new ServerScriptManager();

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
			for (Identifier id : new HashSet<>(enabledScripts)) {
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
	protected MinecraftData createMinecraftData() {
		MinecraftServer server = currentServer != null ? currentServer : ServerLifecycleHooks.get();
		ServerWorld world = server != null ? server.getOverworld() : null;
		return new ServerMinecraftData(server, world);
	}

	@Override
	protected String getLogPrefix() {
		return "server script";
	}

	private void onServerTick(MinecraftServer server) {
		tick();
	}
}
