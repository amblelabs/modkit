package dev.amble.lib.script.lua;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.util.ServerLifecycleHooks;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;
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
}
