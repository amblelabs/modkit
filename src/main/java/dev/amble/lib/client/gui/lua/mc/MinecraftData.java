package dev.amble.lib.client.gui.lua.mc;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.gui.lua.LuaExpose;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.UUID;
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
}
