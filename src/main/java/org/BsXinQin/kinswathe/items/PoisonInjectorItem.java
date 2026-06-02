package org.BsXinQin.kinswathe.items;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
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
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;

public class PoisonInjectorItem extends Item {

    public PoisonInjectorItem(@NotNull Settings settings) {super(settings);}
    private static final Random random = new Random();

    @Override
    public ActionResult useOnEntity(ItemStack stack, @NotNull PlayerEntity player, @NotNull LivingEntity entity, Hand hand) {
        if (player.getItemCooldownManager().isCoolingDown(this)) return ActionResult.FAIL;
        if (!player.getWorld().isClient && entity instanceof @NotNull PlayerEntity targetPlayer) {
            KinsWatheItems.setItemAfterUsing(player, this, null);
            PlayerPoisonComponent targetPoison = PlayerPoisonComponent.KEY.get(targetPlayer);
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
            // 这里把投毒用的真实物品快照塞进 poisonData，
            // 目的是让后续 death / shield replay 都能正确显示“是谁用什么毒具造成的”。
            NbtCompound poisonData = player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer
                    ? GameFunctions.createReplayItemData(serverPlayer.getServerWorld(), stack)
                    : null;
            if (gameWorld.isRole(targetPlayer, KinsWatheRoles.ROBOT)) {
                // 机器人不会中毒，但依旧记录“尝试投毒失败”。
                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer
                        && targetPlayer instanceof net.minecraft.server.network.ServerPlayerEntity serverTarget) {
                    NbtCompound extra = new NbtCompound();
                    extra.putBoolean("robot_failed", true);
                    GameRecordManager.recordItemUse(serverPlayer, KinsWathe.id("poison_injector"), serverTarget, extra);
                }
                player.sendMessage(Text.translatable("tip.kinswathe.drugmaker.poison_failed").withColor(Color.RED.getRGB()), true);
                player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                return ActionResult.SUCCESS;
            }
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer
                    && targetPlayer instanceof net.minecraft.server.network.ServerPlayerEntity serverTarget) {
                GameRecordManager.recordItemUse(serverPlayer, KinsWathe.id("poison_injector"), serverTarget, null);
            }
            if (targetPoison.poisonTicks > 0) {
                // 目标已经中毒时，毒液注射器不是“重新上毒”，而是直接触发致死。
                GameFunctions.killPlayer(targetPlayer, true, player, GameConstants.DeathReasons.POISON, poisonData);
                player.playSoundToPlayer(SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            } else {
                int poisonTicks = PlayerPoisonComponent.clampTime.getLeft() + random.nextInt(PlayerPoisonComponent.clampTime.getRight() - PlayerPoisonComponent.clampTime.getLeft());
                targetPoison.setDetailedPoisonTicks(poisonTicks, player.getUuid(), GameConstants.DeathReasons.POISON, poisonData);
                player.playSoundToPlayer(SoundEvents.ENTITY_SPIDER_DEATH, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
