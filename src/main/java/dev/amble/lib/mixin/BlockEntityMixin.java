package dev.amble.lib.mixin;

import dev.amble.lib.animation.AnimatedInstance;
import dev.amble.lib.animation.AnimationTracker;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clears tracked animation state for any animated block entity when it is removed
 * (broken or unloaded).
 *
 * <p>This hooks the vanilla {@link BlockEntity} rather than {@code ABlockEntity} on
 * purpose: a block entity's animation UUID is derived from its world + position
 * ({@code AnimatedBlockEntity#getUuid}), so a block placed where an animated one was
 * broken would otherwise inherit the stale animation (#68), and an animation would
 * survive chunk unload/reload (#45). Keying off {@link AnimatedInstance} means this
 * also covers downstream block entities that implement {@code AnimatedBlockEntity}
 * directly on a vanilla {@code BlockEntity} (e.g. addon cover/monitor blocks), not
 * just {@code ABlockEntity} subclasses.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {

    @Inject(method = "markRemoved", at = @At("HEAD"))
    private void amblekit$clearAnimationOnRemove(CallbackInfo ci) {
        if (!((Object) this instanceof AnimatedInstance animated))
            return;

        // Only the server holds the authoritative tracker and drives the client sync.
        World world = ((BlockEntity) (Object) this).getWorld();
        if (world == null || world.isClient)
            return;

        // markRemoved fires constantly on chunk unload, so only sync a removal when
        // this instance actually has a tracked animation - avoids pointless packets.
        AnimationTracker tracker = AnimationTracker.getInstance();
        if (tracker.get(animated) != null)
            tracker.remove(animated);
    }
}
