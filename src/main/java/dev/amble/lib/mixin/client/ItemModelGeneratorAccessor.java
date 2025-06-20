package dev.amble.lib.mixin.client;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.util.Identifier;

@Mixin(ItemModelGenerator.class)
public interface ItemModelGeneratorAccessor {
    @Accessor
    BiConsumer<Identifier, Supplier<JsonElement>> getWriter();
}
