package org.BsXinQin.kinswathe.items;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.jetbrains.annotations.NotNull;

public class SulfuricAcidBarrelItem extends Item {

    public SulfuricAcidBarrelItem(@NotNull Settings settings) {super(settings);}

    @Override
    public ActionResult useOnEntity(ItemStack stack, @NotNull PlayerEntity player, @NotNull LivingEntity entity, Hand hand) {
        if (player.getItemCooldownManager().isCoolingDown(this)) return ActionResult.FAIL;
        if (!player.getWorld().isClient && entity instanceof @NotNull PlayerBodyEntity playerBody) {
            // 溶解尸体的回放需要带上尸体归属者，方便显示“溶解了谁的尸体”。
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                NbtCompound extra = new NbtCompound();
                extra.putUuid("body_owner", playerBody.getPlayerUuid());
                GameRecordManager.recordItemUse(serverPlayer, KinsWathe.id("sulfuric_acid_barrel"), null, extra);
            }
            KinsWatheItems.setItemAfterUsing(player, this, null);
            playerBody.discard();
            player.getWorld().playSound(null, playerBody.getX(), playerBody.getY() + .1f, playerBody.getZ(), SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.PLAYERS, 1.0f, 0.5f);
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
            PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
            if (gameWorld.isRole(player, KinsWatheRoles.CLEANER)) {
                playerShop.addToBalance(KinsWatheConfig.HANDLER.instance().CleanerGetCoins);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
