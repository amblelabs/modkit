package dev.amble.lib.client.gui.lua.mc;

import dev.amble.lib.client.gui.lua.LuaExpose;
import lombok.AllArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.luaj.vm2.LuaValue;

@AllArgsConstructor
public class LuaItemStack {
	public final ItemStack stack;

	@LuaExpose
	public int count() {
		return stack.getCount();
	}

	@LuaExpose
	public String name() {
		return stack.getName().getString();
	}

	@LuaExpose
	public String id() {
		return Registries.ITEM.getId(stack.getItem()).toString();
	}
}
