package org.BsXinQin.kinswathe.roles.hunter;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.jetbrains.annotations.NotNull;

public class HunterAbility {

    public static void register(@NotNull PlayerEntity player) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
        PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
        if (gameWorld.isRole(player, KinsWatheRoles.HUNTER) && GameFunctions.isPlayerAliveAndSurvival(player) && ability.cooldown <= 0) {
            int price = KinsWatheConfig.HANDLER.instance().HunterAbilityPrice;
            if (playerShop.balance < price) return;
            playerShop.balance -= price;
            playerShop.sync();
            player.getItemCooldownManager().set(WatheItems.KNIFE, 0);
            player.getItemCooldownManager().set(KinsWatheItems.HUNTING_KNIFE, 0);
            player.playSoundToPlayer(SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                NbtCompound extra = new NbtCompound();
                extra.putInt("price", price);
                GameRecordManager.recordSkillUse(serverPlayer, KinsWathe.id("hunter_refresh"), null, extra);
            }
            ability.setAbilityCooldown(KinsWatheConfig.HANDLER.instance().HunterAbilityCooldown);
        }
    }
}
