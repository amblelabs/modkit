package dev.amble.plushies.item; // 根据你的包结构调整

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
        // 检查头盔槽是否为空
        ItemStack headStack = user.getInventory().armor.get(EquipmentSlot.HEAD.getEntitySlotId());
        if (headStack.isEmpty()) {
            // 将手上的物品复制一份放入头盔槽，并移除手上的物品
            user.getInventory().armor.set(EquipmentSlot.HEAD.getEntitySlotId(), stack.copy());
            stack.setCount(0);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
                    SoundCategory.PLAYERS, 1.0f, 1.0f);
            return TypedActionResult.success(stack);
        }
        // 槽位已被占用，执行原逻辑（即尝试放置方块）
        return super.use(world, user, hand);
    }
}
