package org.BsXinQin.kinswathe.roles.bellringer;

import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.jetbrains.annotations.NotNull;

public class BellringerAbility {

    public static void register(@NotNull PlayerEntity player) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
        GameTimeComponent time = GameTimeComponent.KEY.get(player.getWorld());
        PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
        if (gameWorld.isRole(player, KinsWatheRoles.BELLRINGER) && GameFunctions.isPlayerAliveAndSurvival(player) && ability.cooldown <= 0) {
            int price = KinsWatheConfig.HANDLER.instance().BellringerAbilityPrice;
            int reduceSeconds = KinsWatheConfig.HANDLER.instance().BellringerReduceSeconds;
            if (playerShop.balance < price) return;
            playerShop.balance -= price;
            playerShop.sync();
            time.setTime(Math.max(0, time.getTime() - reduceSeconds * 20));
            player.playSoundToPlayer(SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                NbtCompound extra = new NbtCompound();
                extra.putInt("seconds", reduceSeconds);
                extra.putInt("price", price);
                GameRecordManager.recordSkillUse(serverPlayer, KinsWathe.id("bellringer_reduce_time"), null, extra);
            }
            ability.setAbilityCooldown(KinsWatheConfig.HANDLER.instance().BellringerAbilityCooldown);
        }
    }
}
