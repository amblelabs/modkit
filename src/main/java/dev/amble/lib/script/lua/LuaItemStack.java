package dev.amble.lib.script.lua;

import lombok.AllArgsConstructor;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Lua wrapper for Minecraft ItemStack.
 * Provides access to item properties and NBT data from Lua scripts.
 */
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

	// ===== Additional Methods =====

	@LuaExpose
	public int maxCount() {
		return stack.getMaxCount();
	}

	@LuaExpose
	public int maxDamage() {
		return stack.getMaxDamage();
	}

	@LuaExpose
	public int damage() {
		return stack.getDamage();
	}

	@LuaExpose
	public boolean isDamageable() {
		return stack.isDamageable();
	}

	@LuaExpose
	public boolean hasEnchantments() {
		return stack.hasEnchantments();
	}

	@LuaExpose
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@LuaExpose
	public boolean isStackable() {
		return stack.isStackable();
	}

	@LuaExpose
	public float durabilityPercent() {
		if (!stack.isDamageable()) return 1.0f;
		return 1.0f - ((float) stack.getDamage() / (float) stack.getMaxDamage());
	}

	@LuaExpose
	public boolean hasCustomName() {
		return stack.hasCustomName();
	}

	@LuaExpose
	public boolean isFood() {
		return stack.isFood();
	}

	@LuaExpose
	public String rarity() {
		return stack.getRarity().name().toLowerCase();
	}

	@LuaExpose
	public List<String> enchantments() {
		List<String> result = new ArrayList<>();
		EnchantmentHelper.get(stack).forEach((enchantment, level) -> {
			String enchantId = Registries.ENCHANTMENT.getId(enchantment).toString();
			result.add(enchantId + ":" + level);
		});
		return result;
	}

	@LuaExpose
	public boolean hasNbt() {
		return stack.hasNbt();
	}

	/**
	 * Returns the NBT data as a Lua table for structured access.
	 * @return Lua table representation of the NBT data, or NIL if no NBT
	 */
	@LuaExpose
	public LuaValue nbt() {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null) {
			return LuaValue.NIL;
		}
		return LuaBinder.coerceNbtCompound(nbt);
	}

	/**
	 * Returns the NBT data as a string (for debugging/display purposes).
	 * @return String representation of the NBT data
	 * @deprecated Use nbt() for structured access
	 */
	@Deprecated
	@LuaExpose
	public String nbtString() {
		NbtCompound nbt = stack.getNbt();
		return nbt != null ? nbt.toString() : "";
	}
}
