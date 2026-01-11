package dev.amble.lib.script.lua;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.script.AbstractScriptManager;
import dev.amble.lib.script.LuaScript;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Abstract base class for Minecraft data exposed to Lua scripts.
 * Contains methods that work on both client and server sides.
 */
public abstract class MinecraftData {

	private String scriptName = null;

	/**
	 * Sets the name of the script using this data, for logging purposes.
	 *
	 * @param scriptName the script name or identifier
	 */
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	/**
	 * Gets the log prefix including the script name if available.
	 */
	private String getLogPrefix() {
		return scriptName != null ? "[Script: " + scriptName + "]" : "[Script]";
	}

	/**
	 * @return true if this is client-side data, false if server-side
	 */
	@LuaExpose
	public abstract boolean isClientSide();

	/**
	 * @return the world this data operates on
	 */
	protected abstract World getWorld();

	/**
	 * Returns the entity that is executing this script context.
	 * On client, this is typically the local player.
	 * On server, this may be the player who ran a command, or null for server-initiated scripts.
	 *
	 * @return the executor entity, or null if not applicable
	 */
	protected abstract Entity getExecutor();

	// ===== World & Environment =====

	@LuaExpose
	public String dimension() {
		World world = getWorld();
		return world != null ? world.getRegistryKey().getValue().toString() : "unknown";
	}

	@LuaExpose
	public long worldTime() {
		World world = getWorld();
		return world != null ? world.getTimeOfDay() : 0;
	}

	@LuaExpose
	public long dayCount() {
		World world = getWorld();
		return world != null ? world.getTimeOfDay() / 24000L : 0;
	}

	@LuaExpose
	public boolean isRaining() {
		World world = getWorld();
		return world != null && world.isRaining();
	}

	@LuaExpose
	public boolean isThundering() {
		World world = getWorld();
		return world != null && world.isThundering();
	}

	@LuaExpose
	public String biomeAt(int x, int y, int z) {
		World world = getWorld();
		if (world == null) return "unknown";
		return world.getBiome(new BlockPos(x, y, z)).getKey()
				.map(k -> k.getValue().toString()).orElse("unknown");
	}

	@LuaExpose
	public String blockAt(int x, int y, int z) {
		World world = getWorld();
		if (world == null) return "minecraft:air";
		return Registries.BLOCK.getId(world.getBlockState(new BlockPos(x, y, z)).getBlock()).toString();
	}

	@LuaExpose
	public int lightLevelAt(int x, int y, int z) {
		World world = getWorld();
		return world != null ? world.getLightLevel(new BlockPos(x, y, z)) : 0;
	}

	// ===== Player & Entity =====

	@LuaExpose
	public Entity player() {
		return getExecutor();
	}

	@LuaExpose
	public List<Entity> entities() {
		World world = getWorld();
		if (world == null) return List.of();
		
		if (world instanceof ServerWorld serverWorld) {
			return StreamSupport.stream(serverWorld.iterateEntities().spliterator(), false)
					.collect(Collectors.toList());
		}
		return List.of();
	}

	@LuaExpose
	public Entity nearestEntity(double maxDistance) {
		World world = getWorld();
		Entity executor = getExecutor();
		if (world == null || executor == null) return null;
		
		return world.getOtherEntities(executor, executor.getBoundingBox().expand(maxDistance), e -> true)
				.stream()
				.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(executor)))
				.orElse(null);
	}

	@LuaExpose
	public List<Entity> entitiesInRadius(double radius) {
		World world = getWorld();
		Entity executor = getExecutor();
		if (world == null || executor == null) return List.of();
		
		return world.getOtherEntities(executor, executor.getBoundingBox().expand(radius), e -> true);
	}

	// ===== Audio (shared implementation) =====

	@LuaExpose
	public void playSoundAt(String soundId, double x, double y, double z, float volume, float pitch) {
		World world = getWorld();
		if (world == null) return;
		
		Identifier id = new Identifier(soundId);
		SoundEvent sound = Registries.SOUND_EVENT.get(id);
		if (sound != null) {
			world.playSound(null, x, y, z, sound, SoundCategory.MASTER, volume, pitch);
		}
	}

	// ===== Commands =====

	/**
	 * Runs a command. Implementation differs between client and server.
	 * On client: sends command through player network handler
	 * On server: executes command with server permissions
	 */
	@LuaExpose
	public abstract void runCommand(String command);

	/**
	 * Sends a message to the player. Implementation differs between client and server.
	 */
	@LuaExpose
	public abstract void sendMessage(String message, boolean overlay);

	/**
	 * Logs a message to the console.
	 */
	@LuaExpose
	public void log(String message) {
		AmbleKit.LOGGER.info("{} {}", getLogPrefix(), message);
	}

	@LuaExpose
	public void logWarn(String message) {
		AmbleKit.LOGGER.warn("{} {}", getLogPrefix(), message);
	}

	@LuaExpose
	public void logError(String message) {
		AmbleKit.LOGGER.error("{} {}", getLogPrefix(), message);
	}

	// ===== Cross-script function calling =====

	/**
	 * Gets the script manager for this side.
	 */
	protected abstract AbstractScriptManager getScriptManager();

	/**
	 * Converts a user-friendly script ID to the internal identifier format.
	 * Handles both "modid:scriptname" and full "modid:script/scriptname.lua" formats.
	 */
	private Identifier toFullScriptId(String scriptId) {
		Identifier id = new Identifier(scriptId);
		String path = id.getPath();
		if (!path.startsWith("script/")) {
			path = "script/" + path;
		}
		if (!path.endsWith(".lua")) {
			path = path + ".lua";
		}
		return new Identifier(id.getNamespace(), path);
	}

	/**
	 * Converts a full internal script identifier to display format.
	 * Removes the "script/" prefix and ".lua" suffix.
	 */
	private String toDisplayId(Identifier id) {
		String path = id.getPath();
		if (path.startsWith("script/")) {
			path = path.substring(7);
		}
		if (path.endsWith(".lua")) {
			path = path.substring(0, path.length() - 4);
		}
		return id.getNamespace() + ":" + path;
	}

	/**
	 * Gets the identifiers of all available scripts.
	 *
	 * @return list of script identifiers in "modid:scriptname" format
	 */
	@LuaExpose
	public List<String> availableScripts() {
		return getScriptManager().getCache().keySet().stream()
				.map(this::toDisplayId)
				.collect(Collectors.toList());
	}

	/**
	 * Calls a function from another script.
	 *
	 * @param scriptId the script identifier (e.g., "modid:scriptname")
	 * @param functionName the name of the function to call
	 * @param args the arguments to pass to the function
	 * @return the result of the function call, or nil if the function doesn't exist
	 */
	@LuaExpose
	public Object callScript(String scriptId, String functionName, Object... args) {
		Identifier fullId = toFullScriptId(scriptId);
		LuaScript script = getScriptManager().getCache().get(fullId);
		if (script == null) {
			logWarn("Cannot call function '" + functionName + "': script '" + scriptId + "' not found");
			return null;
		}

		LuaValue function = script.globals().get(functionName);
		if (function.isnil()) {
			logWarn("Function '" + functionName + "' not found in script '" + scriptId + "'");
			return null;
		}

		try {
			// Convert Java args to Lua values
			LuaValue[] luaArgs = new LuaValue[args.length];
			for (int i = 0; i < args.length; i++) {
				luaArgs[i] = LuaBinder.coerceResult(args[i]);
			}

			Varargs result = function.invoke(LuaValue.varargsOf(luaArgs));
			return result.arg1();
		} catch (Exception e) {
			logError("Error calling function '" + functionName + "' in script '" + scriptId + "': " + e.getMessage());
			return null;
		}
	}

	/**
	 * Gets a global variable from another script.
	 *
	 * @param scriptId the script identifier (e.g., "modid:scriptname")
	 * @param variableName the name of the global variable to get
	 * @return the value of the variable, or nil if it doesn't exist
	 */
	@LuaExpose
	public Object getScriptGlobal(String scriptId, String variableName) {
		Identifier fullId = toFullScriptId(scriptId);
		LuaScript script = getScriptManager().getCache().get(fullId);
		if (script == null) {
			logWarn("Cannot get global '" + variableName + "': script '" + scriptId + "' not found");
			return null;
		}

		return script.globals().get(variableName);
	}

	/**
	 * Sets a global variable in another script.
	 *
	 * @param scriptId the script identifier (e.g., "modid:scriptname")
	 * @param variableName the name of the global variable to set
	 * @param value the value to set
	 */
	@LuaExpose
	public void setScriptGlobal(String scriptId, String variableName, Object value) {
		Identifier fullId = toFullScriptId(scriptId);
		LuaScript script = getScriptManager().getCache().get(fullId);
		if (script == null) {
			logWarn("Cannot set global '" + variableName + "': script '" + scriptId + "' not found");
			return;
		}

		script.globals().set(variableName, LuaBinder.coerceResult(value));
	}
}
