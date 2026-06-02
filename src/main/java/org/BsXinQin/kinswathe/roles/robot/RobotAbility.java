package org.BsXinQin.kinswathe.roles.robot;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.jetbrains.annotations.NotNull;

public class RobotAbility {

    public static void register(@NotNull PlayerEntity player) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
        if (gameWorld.isRole(player, KinsWatheRoles.ROBOT) && GameFunctions.isPlayerAliveAndSurvival(player) && ability.cooldown <= 0) {
            int durationTicks = KinsWatheConfig.HANDLER.instance().RobotAbilityDuration * 20;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, durationTicks, 0, false, false, true));
            // 把“夜视什么时候结束”也托管给服务端组件，保证回放一定能落地。
            PlayerEffectComponent.KEY.get(player).setRobotNightVisionTicks(durationTicks, KinsWathe.id("robot_night_vision_end"));
            player.playSoundToPlayer(SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                GameRecordManager.recordSkillUse(serverPlayer, KinsWathe.id("robot_night_vision"), null, null);
            }
            ability.setAbilityCooldown(KinsWatheConfig.HANDLER.instance().RobotAbilityCooldown);
        }
    }
}
