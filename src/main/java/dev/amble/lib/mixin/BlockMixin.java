package dev.amble.lib.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.amble.lib.block.ABlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;appendProperties(Lnet/minecraft/state/StateManager$Builder;)V"))
    public void init(AbstractBlock.Settings settings, CallbackInfo ci, @Local StateManager.Builder<Block, BlockState> builder) {
        if (settings instanceof ABlockSettings abs) builder.add(abs.properties());
    }
}
