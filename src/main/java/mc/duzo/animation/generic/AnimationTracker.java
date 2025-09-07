package mc.duzo.animation.generic;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import mc.duzo.animation.DuzoAnimationMod;
import mc.duzo.animation.registry.Identifiable;
import mc.duzo.animation.registry.client.TrackerRegistry;

public abstract class AnimationTracker<T extends AnimationHolder> implements Identifiable {
    private final Identifier id;
    protected final HashMap<UUID, T> animations = new HashMap<>();

    protected AnimationTracker(Identifier id) {
        this.id = id;
    }
    @Override
    public Identifier id() {
        return id;
    }

    public T get(LivingEntity entity) {
        UUID uuid = entity.getUuid();

        T anim = animations.get(uuid);

        if (anim != null && anim.isFinished(entity)) {
            clear(uuid);
            return null;
        }

        return anim;
    }
    public void add(UUID uuid, T animation) {
        animations.put(uuid, animation);
    }
    public void clear(UUID uuid) {
        animations.remove(uuid);
    }
    public void play(ServerPlayerEntity player, Identifier animation) {
        DuzoAnimationMod.play(player, this, animation);
    }

    public void onDisconnect() {
        animations.clear();
    }

    public AnimationTracker<T> register() {
        return TrackerRegistry.register(this);
    }
}
