package dev.amble.lib.script.lua;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.gui.AmbleContainer;
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
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Client-side implementation of MinecraftData.
 * Provides access to client-only features like input, GUI, clipboard, etc.
 */
public class ClientMinecraftData extends MinecraftData {
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	@Override
	@LuaExpose
	public boolean isClientSide() {
		return true;
	}

	@Override
	protected World getWorld() {
		return mc.world;
	}

	@Override
	protected Entity getExecutor() {
		return mc.player;
	}

	// ===== Client-specific entity methods =====

	@Override
	@LuaExpose
	public List<Entity> entities() {
		if (mc.world == null) return List.of();
		return StreamSupport.stream(mc.world.getEntities().spliterator(), false)
				.collect(Collectors.toList());
	}

	// ===== Session & Identity =====

	@LuaExpose
	public String username() {
		return mc.getSession().getUsername();
	}

	// ===== Inventory =====

	@LuaExpose
	public int selectedSlot() {
		return mc.player != null ? mc.player.getInventory().selectedSlot + 1 : 0;
	}

	@LuaExpose
	public void selectSlot(int slot) {
		if (mc.player != null) {
			mc.player.getInventory().selectedSlot = slot - 1;
		}
	}

	@LuaExpose
	public void dropStack(int slot, boolean entireStack) {
		if (mc.player == null) return;
		int selected = selectedSlot();
		swapStack(slot, selected);
		PlayerActionC2SPacket.Action action = entireStack ? PlayerActionC2SPacket.Action.DROP_ALL_ITEMS : PlayerActionC2SPacket.Action.DROP_ITEM;
		ItemStack itemStack = mc.player.getInventory().dropSelectedItem(entireStack);
		mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(action, BlockPos.ORIGIN, Direction.DOWN));
	}

	@LuaExpose
	public void swapStack(int fromSlot, int toSlot) {
		if (mc.player == null) return;
		ItemStack stack = mc.player.getInventory().getStack(fromSlot - 1);
		mc.player.getInventory().setStack(fromSlot - 1, mc.player.getInventory().getStack(toSlot - 1));
		mc.player.getInventory().setStack(toSlot - 1, stack);
	}

	// ===== Commands & Messages =====

	@Override
	@LuaExpose
	public void runCommand(String command) {
		if (mc.player == null) return;
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

	@Override
	@LuaExpose
	public void sendMessage(String message, boolean overlay) {
		if (mc.player != null) {
			mc.player.sendMessage(Text.literal(message), overlay);
		}
	}

	// ===== Input =====

	@LuaExpose
	public boolean isKeyPressed(String keyName) {
		if (mc.options == null) return false;
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
		return mc.interactionManager != null ? mc.interactionManager.getCurrentGameMode().getName() : "unknown";
	}

	// ===== Audio =====

	@LuaExpose
	public void playSound(String soundId, float volume, float pitch) {
		if (mc.player == null) return;
		Identifier id = new Identifier(soundId);
		SoundEvent sound = Registries.SOUND_EVENT.get(id);
		if (sound != null) {
			mc.player.playSound(sound, volume, pitch);
		}
	}

	// ===== Entity Queries =====

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
		return mc.keyboard != null ? mc.keyboard.getClipboard() : "";
	}

	@LuaExpose
	public void setClipboard(String text) {
		if (mc.keyboard != null) {
			mc.keyboard.setClipboard(text);
		}
	}

	@LuaExpose
	public int windowWidth() {
		return mc.getWindow() != null ? mc.getWindow().getScaledWidth() : 0;
	}

	@LuaExpose
	public int windowHeight() {
		return mc.getWindow() != null ? mc.getWindow().getScaledHeight() : 0;
	}
}
