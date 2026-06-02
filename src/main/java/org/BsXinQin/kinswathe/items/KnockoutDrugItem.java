package org.BsXinQin.kinswathe.items;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.roles.kidnapper.KidnapperComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class KnockoutDrugItem extends Item {

    public KnockoutDrugItem(@NotNull Settings settings) {super(settings);}

    @Override
    public ActionResult useOnEntity(ItemStack stack, @NotNull PlayerEntity player, @NotNull LivingEntity entity, @NotNull Hand hand) {
        if (player.getItemCooldownManager().isCoolingDown(this)) return ActionResult.FAIL;
        if (player.isSneaking()) return ActionResult.FAIL;
        if (!player.getWorld().isClient && entity instanceof @NotNull PlayerEntity targetPlayer) {
            KinsWatheItems.setItemAfterUsing(player, this, hand);
            KidnapperComponent playerControlled = KidnapperComponent.KEY.get(targetPlayer);
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
            // 机器人免疫迷药时，仍然要记录一条“失败事件”，方便回放里看出这次尝试并不是正常迷晕。
            if (gameWorld.isRole(targetPlayer, KinsWatheRoles.ROBOT)) {
                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer
                        && targetPlayer instanceof net.minecraft.server.network.ServerPlayerEntity serverTarget) {
                    NbtCompound extra = new NbtCompound();
                    extra.putBoolean("robot_failed", true);
                    GameRecordManager.recordItemUse(serverPlayer, KinsWathe.id("knockout_drug"), serverTarget, extra);
                }
                player.sendMessage(Text.translatable("tip.kinswathe.kidnapper.daze_failed").withColor(Color.RED.getRGB()), true);
                player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                return ActionResult.SUCCESS;
            }
            if (playerControlled.controlTicks <= 0) {
                playerControlled.startControl(player);
                // 迷晕并劫持成功时，记录一次标准使用事件。
                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer
                        && targetPlayer instanceof net.minecraft.server.network.ServerPlayerEntity serverTarget) {
                    GameRecordManager.recordItemUse(serverPlayer, KinsWathe.id("knockout_drug"), serverTarget, null);
                }
                player.playSoundToPlayer(SoundEvents.ENTITY_SHEEP_AMBIENT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                targetPlayer.playSoundToPlayer(SoundEvents.ENTITY_SHEEP_AMBIENT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
