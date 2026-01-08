package dev.amble.lib.client.gui.lua.mc;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.gui.AmbleContainer;
import dev.amble.lib.client.gui.lua.LuaExpose;
import dev.amble.lib.client.gui.registry.AmbleGuiRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MinecraftData {
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	@LuaExpose
	public String username() {
		return mc.getSession().getUsername();
	}

	@LuaExpose
	public void runCommand(String command) {
		try {
			String string2 = SharedConstants.stripInvalidChars(command);
			if (string2.startsWith("/")) {
				if (!mc.player.networkHandler.sendCommand(string2.substring(1))) {
					AmbleKit.LOGGER.error("Not allowed to run command with signed argument from lua: '{}'", string2);
				}
			} else {
				AmbleKit.LOGGER.error("Failed to run command without '/' prefix from lua: '{}'", string2);
			}
		} catch (Exception e) {
			AmbleKit.LOGGER.error("Error occurred while running command from lua: '{}'", command, e);
		}
	}

	@LuaExpose
	public int selectedSlot() {
		return mc.player.getInventory().selectedSlot + 1;
	}

	@LuaExpose
	public void selectSlot(int slot) {
		mc.player.getInventory().selectedSlot = slot - 1;
	}

	@LuaExpose
	public void sendMessage(String message, boolean overlay) {
		mc.player.sendMessage(Text.literal(message), overlay);
	}

	@LuaExpose
	public Entity player() {
		return mc.player;
	}

	@LuaExpose
	public List<Entity> entities() {
		return StreamSupport.stream(mc.world.getEntities().spliterator(), false)
				.collect(Collectors.toList());
	}

	@LuaExpose
	public void dropStack(int slot, boolean entireStack) {
		int selected = selectedSlot();
		swapStack(slot, selected);
		PlayerActionC2SPacket.Action action = entireStack ? PlayerActionC2SPacket.Action.DROP_ALL_ITEMS : PlayerActionC2SPacket.Action.DROP_ITEM;
		ItemStack itemStack = mc.player.getInventory().dropSelectedItem(entireStack);
		mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(action, BlockPos.ORIGIN, Direction.DOWN));
		//swapStack(selected, slot);
	}

	@LuaExpose
	public void swapStack(int fromSlot, int toSlot) {
		ItemStack stack = mc.player.getInventory().getStack(fromSlot - 1);
		mc.player.getInventory().setStack(fromSlot - 1, mc.player.getInventory().getStack(toSlot - 1));
		mc.player.getInventory().setStack(toSlot - 1, stack);

		// todo sync change to server somehow
	}

	// ===== World & Environment =====

	@LuaExpose
	public String dimension() {
		return mc.world.getRegistryKey().getValue().toString();
	}

	@LuaExpose
	public long worldTime() {
		return mc.world.getTimeOfDay();
	}

	@LuaExpose
	public long dayCount() {
		return mc.world.getTimeOfDay() / 24000L;
	}

	@LuaExpose
	public boolean isRaining() {
		return mc.world.isRaining();
	}

	@LuaExpose
	public boolean isThundering() {
		return mc.world.isThundering();
	}

	@LuaExpose
	public String biomeAt(int x, int y, int z) {
		return mc.world.getBiome(new BlockPos(x, y, z)).getKey()
				.map(k -> k.getValue().toString()).orElse("unknown");
	}

	@LuaExpose
	public String blockAt(int x, int y, int z) {
		return Registries.BLOCK.getId(mc.world.getBlockState(new BlockPos(x, y, z)).getBlock()).toString();
	}

	@LuaExpose
	public int lightLevelAt(int x, int y, int z) {
		return mc.world.getLightLevel(new BlockPos(x, y, z));
	}

	// ===== Input =====

	@LuaExpose
	public boolean isKeyPressed(String keyName) {
		return switch (keyName.toLowerCase()) {
			case "forward" -> mc.options.forwardKey.isPressed();
			case "back" -> mc.options.backKey.isPressed();
			case "left" -> mc.options.leftKey.isPressed();
			case "right" -> mc.options.rightKey.isPressed();
			case "jump" -> mc.options.jumpKey.isPressed();
			case "sneak" -> mc.options.sneakKey.isPressed();
			case "sprint" -> mc.options.sprintKey.isPressed();
			case "attack" -> mc.options.attackKey.isPressed();
			case "use" -> mc.options.useKey.isPressed();
			default -> false;
		};
	}

	@LuaExpose
	public String gameMode() {
		return mc.interactionManager.getCurrentGameMode().getName();
	}

	// ===== Audio =====

	@LuaExpose
	public void playSound(String soundId, float volume, float pitch) {
		Identifier id = new Identifier(soundId);
		SoundEvent sound = Registries.SOUND_EVENT.get(id);
		if (sound != null) {
			mc.player.playSound(sound, volume, pitch);
		}
	}

	// ===== Entity Queries =====

	@LuaExpose
	public Entity nearestEntity(double maxDistance) {
		return mc.world.getOtherEntities(mc.player, mc.player.getBoundingBox().expand(maxDistance), e -> true)
				.stream()
				.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)))
				.orElse(null);
	}

	@LuaExpose
	public List<Entity> entitiesInRadius(double radius) {
		return mc.world.getOtherEntities(mc.player, mc.player.getBoundingBox().expand(radius), e -> true);
	}

	@LuaExpose
	public Entity lookingAtEntity() {
		if (mc.crosshairTarget instanceof EntityHitResult hit) {
			return hit.getEntity();
		}
		return null;
	}

	@LuaExpose
	public BlockPos lookingAtBlock() {
		if (mc.crosshairTarget instanceof BlockHitResult hit) {
			return hit.getBlockPos();
		}
		return null;
	}

	// ===== UI & Clipboard =====

	@LuaExpose
	public void displayScreen(String screenId) {
		AmbleContainer screen = AmbleGuiRegistry.getInstance().get(new Identifier(screenId));
		if (screen != null) {
			screen.display();
		} else {
			AmbleKit.LOGGER.warn("Screen '{}' not found in AmbleGuiRegistry", screenId);
		}
	}

	@LuaExpose
	public void closeScreen() {
		mc.setScreen(null);
	}

	@LuaExpose
	public String clipboard() {
		return mc.keyboard.getClipboard();
	}

	@LuaExpose
	public void setClipboard(String text) {
		mc.keyboard.setClipboard(text);
	}

	@LuaExpose
	public int windowWidth() {
		return mc.getWindow().getScaledWidth();
	}

	@LuaExpose
	public int windowHeight() {
		return mc.getWindow().getScaledHeight();
	}
}
