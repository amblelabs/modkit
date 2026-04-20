package dev.amble.lib.script.lua;

import lombok.AllArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

	// ===== Additional Methods =====

	@LuaExpose
	public double maxHealth() {
		return entity instanceof LivingEntity le ? le.getMaxHealth() : -1;
	}

	@LuaExpose
	public double distanceTo(double x, double y, double z) {
		return entity.getPos().distanceTo(new Vec3d(x, y, z));
	}

	@LuaExpose
	public Vec3d velocity() {
		return entity.getVelocity();
	}

	@LuaExpose
	public float yaw() {
		return entity.getYaw();
	}

	@LuaExpose
	public float pitch() {
		return entity.getPitch();
	}

	@LuaExpose
	public boolean isAlive() {
		return entity.isAlive();
	}

	@LuaExpose
	public boolean isSneaking() {
		return entity.isSneaking();
	}

	@LuaExpose
	public boolean isSprinting() {
		return entity.isSprinting();
	}

	@LuaExpose
	public boolean isOnFire() {
		return entity.isOnFire();
	}

	@LuaExpose
	public boolean isInvisible() {
		return entity.isInvisible();
	}

	@LuaExpose
	public boolean isGlowing() {
		return entity.isGlowing();
	}

	@LuaExpose
	public boolean isTouchingWater() {
		return entity.isTouchingWater();
	}

	@LuaExpose
	public List<String> effects() {
		if (entity instanceof LivingEntity le) {
			return le.getStatusEffects().stream()
					.map(e -> Registries.STATUS_EFFECT.getId(e.getEffectType()).toString())
					.collect(Collectors.toList());
		}
		return List.of();
	}

	@LuaExpose
	public float saturation() {
		if (entity instanceof PlayerEntity player) {
			return player.getHungerManager().getSaturationLevel();
		}
		return -1;
	}

	@LuaExpose
	public int armorValue() {
		return entity instanceof LivingEntity le ? le.getArmor() : 0;
	}

	@LuaExpose
	public boolean hasEffect(String effectId) {
		if (entity instanceof LivingEntity le) {
			StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier(effectId));
			return effect != null && le.hasStatusEffect(effect);
		}
		return false;
	}

	// ===== Player-Specific Methods =====

	@LuaExpose
	public int experienceLevel() {
		if (entity instanceof PlayerEntity player) {
			return player.experienceLevel;
		}
		return -1;
	}

	@LuaExpose
	public float experienceProgress() {
		if (entity instanceof PlayerEntity player) {
			return player.experienceProgress;
		}
		return -1;
	}

	@LuaExpose
	public int totalExperience() {
		if (entity instanceof PlayerEntity player) {
			return player.totalExperience;
		}
		return -1;
	}

	@LuaExpose
	public boolean isOnGround() {
		return entity.isOnGround();
	}

	@LuaExpose
	public boolean isSwimming() {
		return entity.isSwimming();
	}

	@LuaExpose
	public boolean isFlying() {
		if (entity instanceof PlayerEntity player) {
			return player.getAbilities().flying;
		}
		return false;
	}
}
