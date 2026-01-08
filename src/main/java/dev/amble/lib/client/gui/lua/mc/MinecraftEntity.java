package dev.amble.lib.client.gui.lua.mc;

import dev.amble.lib.client.gui.lua.LuaExpose;
import lombok.AllArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class MinecraftEntity {
	public final Entity entity;

	@LuaExpose
	public String name() {
		return entity.getName().getString();
	}

	@LuaExpose
	public String type() {
		return Registries.ENTITY_TYPE.getId(entity.getType()).toString();
	}

	@LuaExpose
	public String uuid() {
		return entity.getUuid().toString();
	}

	@LuaExpose
	public boolean isPlayer() {
		return entity.isPlayer();
	}

	@LuaExpose
	public Vec3d position() {
		return entity.getPos();
	}

	@LuaExpose
	public BlockPos blockPosition() {
		return entity.getBlockPos();
	}

	@LuaExpose
	public double health() {
		return entity instanceof LivingEntity livingEntity ? livingEntity.getHealth() : -1;
	}

	@LuaExpose
	public int age() {
		return entity.age;
	}

	@LuaExpose
	public List<ItemStack> inventory() {
		if (entity instanceof PlayerEntity player) {
			List<ItemStack> combined = new ArrayList<>(player.getInventory().main);
			combined.addAll(player.getInventory().armor);
			combined.addAll(player.getInventory().offHand);
			return combined;
		}

		Iterable<ItemStack> hands = entity.getHandItems();
		Iterable<ItemStack> armor = entity.getArmorItems();
		// Combine both iterables into a single list
		List<ItemStack> combined = new ArrayList<>();
		for (ItemStack item : hands) {
			combined.add(item);
		}
		for (ItemStack item : armor) {
			combined.add(item);
		}

		return combined;
	}

	@LuaExpose
	public int foodLevel() {
		if (entity instanceof PlayerEntity player) {
			return player.getHungerManager().getFoodLevel();
		}
		return -1;
	}
}
