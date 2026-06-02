package org.BsXinQin.kinswathe.items;

import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.jetbrains.annotations.NotNull;

public class PillItem extends Item {

    public PillItem(@NotNull Settings settings) {super(settings);}

    @Override
    public @NotNull TypedActionResult<@NotNull ItemStack> use(@NotNull World world, @NotNull PlayerEntity player, @NotNull Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
        if (!world.isClient) {
            KinsWatheItems.setItemAfterUsing(player, this, hand);
            // 药丸现在不再反射调用 noellesroles 的酒保护盾，而是直接走本模组自己的医师护盾。
            // 这样回放和挡伤逻辑都统一，不会再被外部职业实现方式影响。
            PhysicianComponent playerPhysician = PhysicianComponent.KEY.get(player);
            playerPhysician.giveArmor();
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                GameRecordManager.recordItemUse(serverPlayer, KinsWathe.id("pill"), null, null);
            }
            player.playSoundToPlayer(SoundEvents.ITEM_HONEY_BOTTLE_DRINK, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        return TypedActionResult.success(stack, false);
    }
}
