package dev.amble.lib.script.lua;

import dev.amble.lib.AmbleKit;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
}
