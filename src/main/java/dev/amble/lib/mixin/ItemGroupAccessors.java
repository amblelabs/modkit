package dev.amble.lib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.ItemGroup;

@Mixin(ItemGroup.class)
public interface ItemGroupAccessors {
    @Accessor
    String getTexture();

    @Accessor
    void setTexture(String texture);

    @Accessor
    boolean getScrollbar();

    @Accessor
    void setScrollbar(boolean scrollbar);

    @Accessor
    boolean getRenderName();

    @Accessor
    void setRenderName(boolean renderName);

    @Accessor
    boolean getSpecial();

    @Accessor
    void setSpecial(boolean special);
}
