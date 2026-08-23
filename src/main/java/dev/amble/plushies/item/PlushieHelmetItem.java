package dev.amble.plushies.item; // 请根据实际情况调整包名

import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class PlushieHelmetItem extends BlockItem {

    public PlushieHelmetItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        // 头盔槽为空时直接装备
        if (user.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            user.equipStack(EquipmentSlot.HEAD, stack.copy());
            stack.setCount(0);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
                    SoundCategory.PLAYERS, 1.0f, 1.0f);
            return TypedActionResult.success(stack);
        }
        // 头盔槽被占用时，执行原 BlockItem 的放置逻辑
        return super.use(world, user, hand);
    }
}