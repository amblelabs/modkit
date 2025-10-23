package dev.amble.lib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

import dev.amble.lib.item.AItem;
import dev.amble.lib.item.AItemSettings;

@Mixin(Item.class)
public class ItemMixin implements AItem {

    @Unique
    private ItemGroup amble$group;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(Item.Settings settings, CallbackInfo ci) {
        if (settings instanceof AItemSettings ais)
            this.amble$group = ais.group();
    }

    @Override
    public ItemGroup amble$group() {
        return this.amble$group;
    }
}
