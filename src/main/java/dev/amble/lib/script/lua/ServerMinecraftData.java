package dev.amble.lib.script.lua;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.AbstractScriptManager;
import dev.amble.lib.script.ServerScriptManager;
import dev.amble.lib.skin.SkinData;
import dev.amble.lib.skin.SkinTracker;
import dev.amble.lib.username.UsernameTracker;
import dev.amble.lib.util.ServerLifecycleHooks;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Server-side implementation of MinecraftData.
 * Provides access to server-only features like broadcasting, player management, etc.
 */
public class ServerMinecraftData extends MinecraftData {
	private MinecraftServer server;
	private ServerWorld world;
	private final ServerPlayerEntity player; // may be null for server-context scripts

	public ServerMinecraftData(MinecraftServer server, ServerWorld world, ServerPlayerEntity player) {
		this.server = server;
		this.world = world;
		this.player = player;
	}

	public ServerMinecraftData(MinecraftServer server, ServerWorld world) {
		this(server, world, null);
	}

	/**
	 * Gets the server, fetching from lifecycle hooks if not set.
	 */
	private MinecraftServer getServer() {
		if (server == null) {
			server = ServerLifecycleHooks.get();
		}
		return server;
	}

	/**
	 * Gets the world, fetching overworld from server if not set.
	 */
	private ServerWorld getServerWorld() {
		if (world == null && getServer() != null) {
			world = getServer().getOverworld();
		}
		return world;
	}

	@Override
	@LuaExpose
	public boolean isClientSide() {
		return false;
	}

	@Override
	protected World getWorld() {
		return getServerWorld();
	}

	@Override
	protected Entity getExecutor() {
		return player;
	}

	// ===== Server-specific methods =====

	@LuaExpose
	public List<String> allPlayerNames() {
		MinecraftServer srv = getServer();
		if (srv == null) return List.of();
		return srv.getPlayerManager().getPlayerList().stream()
				.map(p -> p.getName().getString())
				.collect(Collectors.toList());
	}

	@LuaExpose
	public List<Entity> allPlayers() {
		MinecraftServer srv = getServer();
		if (srv == null) return List.of();
		return srv.getPlayerManager().getPlayerList().stream()
				.map(p -> (Entity) p)
				.collect(Collectors.toList());
	}

	@LuaExpose
	public Entity getPlayerByName(String name) {
		MinecraftServer srv = getServer();
		if (srv == null) return null;
		return srv.getPlayerManager().getPlayer(name);
	}

	@LuaExpose
	public int playerCount() {
		MinecraftServer srv = getServer();
		if (srv == null) return 0;
		return srv.getPlayerManager().getCurrentPlayerCount();
	}

	@LuaExpose
	public int maxPlayers() {
		MinecraftServer srv = getServer();
		if (srv == null) return 0;
		return srv.getPlayerManager().getMaxPlayerCount();
	}

	@LuaExpose
	public void broadcast(String message) {
		MinecraftServer srv = getServer();
		if (srv == null) {
			AmbleKit.LOGGER.warn("Cannot broadcast: server not available");
			return;
		}
		srv.getPlayerManager().broadcast(Text.literal(message), false);
	}

	@LuaExpose
	public void broadcastToPlayer(String playerName, String message, boolean overlay) {
		MinecraftServer srv = getServer();
		if (srv == null) return;
		ServerPlayerEntity target = srv.getPlayerManager().getPlayer(playerName);
		if (target != null) {
			target.sendMessage(Text.literal(message), overlay);
		}
	}

	// ===== Commands & Messages =====

	@Override
	@LuaExpose
	public void runCommand(String command) {
		MinecraftServer srv = getServer();
		if (srv == null) return;
		try {
			String cmd = command.startsWith("/") ? command.substring(1) : command;
			ServerCommandSource source = srv.getCommandSource();
			srv.getCommandManager().executeWithPrefix(source, cmd);
		} catch (Exception e) {
			AmbleKit.LOGGER.error("Error occurred while running server command from lua: '{}'", command, e);
		}
	}

	/**
	 * Runs a command as a specific player.
	 */
	@LuaExpose
	public void runCommandAs(String playerName, String command) {
		MinecraftServer srv = getServer();
		if (srv == null) return;
		ServerPlayerEntity target = srv.getPlayerManager().getPlayer(playerName);
		if (target == null) {
			AmbleKit.LOGGER.warn("Cannot run command as '{}': player not found", playerName);
			return;
		}
		try {
			String cmd = command.startsWith("/") ? command.substring(1) : command;
			srv.getCommandManager().executeWithPrefix(target.getCommandSource(), cmd);
		} catch (Exception e) {
			AmbleKit.LOGGER.error("Error occurred while running command as {} from lua: '{}'", playerName, command, e);
		}
	}

	@Override
	@LuaExpose
	public void sendMessage(String message, boolean overlay) {
		if (player != null) {
			player.sendMessage(Text.literal(message), overlay);
		} else {
			// If no specific player, log to console
			AmbleKit.LOGGER.info("[Script Message] {}", message);
		}
	}

	// ===== Server Info =====

	@LuaExpose
	public String serverName() {
		MinecraftServer srv = getServer();
		return srv != null ? srv.getName() : "unknown";
	}

	@LuaExpose
	public int tickCount() {
		MinecraftServer srv = getServer();
		return srv != null ? srv.getTicks() : 0;
	}

	@LuaExpose
	public double serverTps() {
		MinecraftServer srv = getServer();
		if (srv == null) return 20.0;
		// Average TPS calculation based on tick times
		long[] tickTimes = srv.lastTickLengths;
		if (tickTimes == null || tickTimes.length == 0) return 20.0;
		
		long sum = 0;
		for (long tickTime : tickTimes) {
			sum += tickTime;
		}
		double avgTickTime = (double) sum / tickTimes.length / 1_000_000.0; // Convert to milliseconds
		return Math.min(20.0, 1000.0 / avgTickTime);
	}

	@LuaExpose
	public boolean isDedicatedServer() {
		MinecraftServer srv = getServer();
		return srv != null && srv.isDedicated();
	}

	// ===== World Management =====

	@LuaExpose
	public List<String> worldNames() {
		MinecraftServer srv = getServer();
		if (srv == null) return List.of();
		return srv.getWorlds().iterator().hasNext() 
			? srv.getWorldRegistryKeys().stream()
				.map(key -> key.getValue().toString())
				.collect(Collectors.toList())
			: List.of();
	}

	// ===== Skin Management =====

	/**
	 * Gets the UUID for a player by name.
	 * @param playerName the player's name
	 * @return the UUID, or null if player not found
	 */
	private UUID getPlayerUuid(String playerName) {
		MinecraftServer srv = getServer();
		if (srv == null) return null;
		ServerPlayerEntity target = srv.getPlayerManager().getPlayer(playerName);
		return target != null ? target.getUuid() : null;
	}

	/**
	 * Sets a player's skin to match another player's skin (by username).
	 * This performs an async lookup of the skin and applies it when ready.
	 * 
	 * @param playerName the player whose skin to change
	 * @param skinUsername the username to copy the skin from
	 * @return true if the player was found, false otherwise
	 */
	@Override
	@LuaExpose
	public boolean setSkin(String playerName, String skinUsername) {
		UUID uuid = getPlayerUuid(playerName);
		if (uuid == null) {
			AmbleKit.LOGGER.warn("Cannot set skin: player '{}' not found", playerName);
			return false;
		}
		SkinData.usernameUpload(skinUsername, uuid);
		return true;
	}

	/**
	 * Sets a player's skin from a direct URL.
	 * 
	 * @param playerName the player whose skin to change
	 * @param url the URL to the skin image
	 * @param slim true for slim (Alex) arms, false for wide (Steve) arms
	 * @return true if the player was found, false otherwise
	 */
	@Override
	@LuaExpose
	public boolean setSkinUrl(String playerName, String url, boolean slim) {
		UUID uuid = getPlayerUuid(playerName);
		if (uuid == null) {
			AmbleKit.LOGGER.warn("Cannot set skin: player '{}' not found", playerName);
			return false;
		}
		SkinData.url(url, slim).upload(uuid);
		return true;
	}

	/**
	 * Changes a player's arm model (slim or wide) without changing the skin texture.
	 * 
	 * @param playerName the player whose arm model to change
	 * @param slim true for slim (Alex) arms, false for wide (Steve) arms
	 * @return true if successful, false if player not found or has no custom skin
	 */
	@Override
	@LuaExpose
	public boolean setSkinSlim(String playerName, boolean slim) {
		UUID uuid = getPlayerUuid(playerName);
		if (uuid == null) {
			AmbleKit.LOGGER.warn("Cannot set skin slim: player '{}' not found", playerName);
			return false;
		}
		SkinData existingSkin = SkinTracker.getInstance().get(uuid);
		if (existingSkin == null) {
			AmbleKit.LOGGER.warn("Cannot set skin slim: player '{}' has no custom skin", playerName);
			return false;
		}
		SkinTracker.getInstance().putSynced(uuid, existingSkin.withSlim(slim));
		return true;
	}

	/**
	 * Clears a player's custom skin, restoring their original skin.
	 * 
	 * @param playerName the player whose skin to clear
	 * @return true if the player was found, false otherwise
	 */
	@Override
	@LuaExpose
	public boolean clearSkin(String playerName) {
		UUID uuid = getPlayerUuid(playerName);
		if (uuid == null) {
			AmbleKit.LOGGER.warn("Cannot clear skin: player '{}' not found", playerName);
			return false;
		}
		SkinTracker.getInstance().removeSynced(uuid);
		return true;
	}

	/**
	 * Checks if a player has a custom skin applied.
	 * 
	 * @param playerName the player to check
	 * @return true if the player has a custom skin, false otherwise
	 */
	@Override
	@LuaExpose
	public boolean hasSkin(String playerName) {
		UUID uuid = getPlayerUuid(playerName);
		if (uuid == null) return false;
		return SkinTracker.getInstance().containsKey(uuid);
	}

	/**
	 * Sets a skin by UUID string.
	 * This performs an async lookup of the skin and applies it when ready.
	 * 
	 * @param uuidString the UUID of the entity whose skin to change
	 * @param skinUsername the username to copy the skin from
	 * @return true if the UUID was valid, false otherwise
	 */
	@LuaExpose
	public boolean setSkinByUuid(String uuidString, String skinUsername) {
		UUID uuid = parseUuid(uuidString);
		if (uuid == null) {
			return false;
		}
		SkinData.usernameUpload(skinUsername, uuid);
		return true;
	}

	/**
	 * Sets a skin from a URL by UUID string.
	 * 
	 * @param uuidString the UUID of the entity whose skin to change
	 * @param url the URL to the skin image
	 * @param slim true for slim (Alex) arms, false for wide (Steve) arms
	 * @return true if the UUID was valid, false otherwise
	 */
	@LuaExpose
	public boolean setSkinUrlByUuid(String uuidString, String url, boolean slim) {
		UUID uuid = parseUuid(uuidString);
		if (uuid == null) {
			return false;
		}
		SkinData.url(url, slim).upload(uuid);
		return true;
	}

	/**
	 * Clears a custom skin by UUID string.
	 * 
	 * @param uuidString the UUID of the entity whose skin to clear
	 * @return true if the UUID was valid, false otherwise
	 */
	@LuaExpose
	public boolean clearSkinByUuid(String uuidString) {
		UUID uuid = parseUuid(uuidString);
		if (uuid == null) {
			return false;
		}
		SkinTracker.getInstance().removeSynced(uuid);
		return true;
	}

	// ===== Username/Nametag Management =====

	/**
	 * Sets a player's display name (nametag).
	 * Supports Minecraft formatting codes (§ prefix).
	 *
	 * @param playerName the player whose name to change
	 * @param displayName the new display name (supports § formatting codes)
	 * @return true if the player was found, false otherwise
	 */
	@Override
	@LuaExpose
	public boolean setUsername(String playerName, String displayName) {
		UUID uuid = getPlayerUuid(playerName);
		if (uuid == null) {
			AmbleKit.LOGGER.warn("Cannot set username: player '{}' not found", playerName);
			return false;
		}
		Text text = Text.of(displayName);
		UsernameTracker.getInstance().putSynced(uuid, text);
		return true;
	}

	/**
	 * Sets a player's display name using a JSON Text component.
	 * Allows for rich text formatting (colors, styles, hover events, etc.)
	 *
	 * @param playerName the player whose name to change
	 * @param jsonText the display name as a JSON text component
	 * @return true if the player was found and JSON was valid, false otherwise
	 */
	@Override
	@LuaExpose
	public boolean setUsernameJson(String playerName, String jsonText) {
		UUID uuid = getPlayerUuid(playerName);
		if (uuid == null) {
			AmbleKit.LOGGER.warn("Cannot set username: player '{}' not found", playerName);
			return false;
		}
		try {
			Text text = Text.Serializer.fromJson(jsonText);
			if (text == null) {
				AmbleKit.LOGGER.warn("Cannot set username: invalid JSON text '{}'", jsonText);
				return false;
			}
			UsernameTracker.getInstance().putSynced(uuid, text);
			return true;
		} catch (Exception e) {
			AmbleKit.LOGGER.warn("Cannot set username: failed to parse JSON text '{}': {}", jsonText, e.getMessage());
			return false;
		}
	}

	/**
	 * Clears a player's custom display name, restoring their original nametag.
	 *
	 * @param playerName the player whose name to clear
	 * @return true if the player was found, false otherwise
	 */
	@Override
	@LuaExpose
	public boolean clearUsername(String playerName) {
		UUID uuid = getPlayerUuid(playerName);
		if (uuid == null) {
			AmbleKit.LOGGER.warn("Cannot clear username: player '{}' not found", playerName);
			return false;
		}
		UsernameTracker.getInstance().removeSynced(uuid);
		return true;
	}

	/**
	 * Checks if a player has a custom display name applied.
	 *
	 * @param playerName the player to check
	 * @return true if the player has a custom display name, false otherwise
	 */
	@Override
	@LuaExpose
	public boolean hasUsername(String playerName) {
		UUID uuid = getPlayerUuid(playerName);
		if (uuid == null) return false;
		return UsernameTracker.getInstance().containsKey(uuid);
	}

	/**
	 * Gets a player's current custom display name.
	 *
	 * @param playerName the player to check
	 * @return the custom display name as a string, or null if none set
	 */
	@Override
	@LuaExpose
	public String getUsername(String playerName) {
		UUID uuid = getPlayerUuid(playerName);
		if (uuid == null) return null;
		Text text = UsernameTracker.getInstance().get(uuid);
		return text != null ? text.getString() : null;
	}

	/**
	 * Sets a display name by UUID string.
	 *
	 * @param uuidString the UUID of the entity whose name to change
	 * @param displayName the new display name (supports § formatting codes)
	 * @return true if the UUID was valid, false otherwise
	 */
	@LuaExpose
	public boolean setUsernameByUuid(String uuidString, String displayName) {
		UUID uuid = parseUuid(uuidString);
		if (uuid == null) {
			return false;
		}
		Text text = Text.of(displayName);
		UsernameTracker.getInstance().putSynced(uuid, text);
		return true;
	}

	/**
	 * Sets a display name by UUID string using a JSON Text component.
	 *
	 * @param uuidString the UUID of the entity whose name to change
	 * @param jsonText the display name as a JSON text component
	 * @return true if the UUID was valid and JSON was valid, false otherwise
	 */
	@LuaExpose
	public boolean setUsernameJsonByUuid(String uuidString, String jsonText) {
		UUID uuid = parseUuid(uuidString);
		if (uuid == null) {
			return false;
		}
		try {
			Text text = Text.Serializer.fromJson(jsonText);
			if (text == null) {
				AmbleKit.LOGGER.warn("Cannot set username: invalid JSON text '{}'", jsonText);
				return false;
			}
			UsernameTracker.getInstance().putSynced(uuid, text);
			return true;
		} catch (Exception e) {
			AmbleKit.LOGGER.warn("Cannot set username: failed to parse JSON text '{}': {}", jsonText, e.getMessage());
			return false;
		}
	}

	/**
	 * Clears a custom display name by UUID string.
	 *
	 * @param uuidString the UUID of the entity whose name to clear
	 * @return true if the UUID was valid, false otherwise
	 */
	@LuaExpose
	public boolean clearUsernameByUuid(String uuidString) {
		UUID uuid = parseUuid(uuidString);
		if (uuid == null) {
			return false;
		}
		UsernameTracker.getInstance().removeSynced(uuid);
		return true;
	}

	// ===== Cross-script function calling =====

	@Override
	protected AbstractScriptManager getScriptManager() {
		return ServerScriptManager.getInstance();
	}
}
