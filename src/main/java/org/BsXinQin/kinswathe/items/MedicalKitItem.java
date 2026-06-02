package org.BsXinQin.kinswathe.items;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.framing.DelusionPlayerComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class MedicalKitItem extends Item {

    public MedicalKitItem(@NotNull Settings settings) {super(settings);}

    @Override
    public ActionResult useOnEntity(ItemStack stack, @NotNull PlayerEntity player, @NotNull LivingEntity entity, Hand hand) {
        if (player.getItemCooldownManager().isCoolingDown(this)) return ActionResult.FAIL;
        if (!player.getWorld().isClient && entity instanceof @NotNull PlayerEntity targetPlayer) {
            WorldModifierComponent modifier = WorldModifierComponent.KEY.get(player.getWorld());
            PlayerPoisonComponent targetPoison = PlayerPoisonComponent.KEY.get(targetPlayer);
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
            PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
            // 医疗箱现在同时兼容两种状态：
            // 1. wathe 真中毒；
            // 2. noellesroles 的幻觉试剂。
            // 这样医师不会只会解毒，却无法解除幻觉。
            boolean hasDelusion = FabricLoader.getInstance().isModLoaded("noellesroles")
                    && DelusionPlayerComponent.KEY.get(targetPlayer).isActive();
            if (targetPoison.poisonTicks > 0 || hasDelusion) {
                KinsWatheItems.setItemAfterUsing(player, this, null);
                if (targetPoison.poisonTicks > 0) {
                    targetPoison.reset();
                }
                if (hasDelusion) {
                    DelusionPlayerComponent.KEY.get(targetPlayer).reset();
                }
                // 回放里写成“用医疗箱解除了异常状态”，不再区分具体是毒还是幻觉。
                targetPlayer.sendMessage(Text.translatable("tip.kinswathe.physician.medical_kit").withColor(Color.GREEN.getRGB()), true);
                targetPlayer.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 1.0f, 1.0f);
                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer
                        && targetPlayer instanceof net.minecraft.server.network.ServerPlayerEntity serverTarget) {
                    GameRecordManager.recordItemUse(serverPlayer, KinsWathe.id("medical_kit"), serverTarget, null);
                }
                if (gameWorld.isRole(player, KinsWatheRoles.PHYSICIAN)) {
                    if (modifier.isModifier(player, KinsWatheRoles.TASKMASTER)) playerShop.balance += 75;
                    else playerShop.balance += 50;
                    playerShop.sync();
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
