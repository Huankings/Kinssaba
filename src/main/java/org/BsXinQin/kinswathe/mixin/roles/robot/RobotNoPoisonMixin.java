package org.BsXinQin.kinswathe.mixin.roles.robot;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.record.GameRecordManager;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerPoisonComponent.class)
public class RobotNoPoisonMixin {

    @Shadow @Final @NotNull private PlayerEntity player;

    @Inject(method = "setDetailedPoisonTicks", at = @At("HEAD"), cancellable = true)
    private void noRobotPoison(int ticks, UUID poisoner, Identifier source, NbtCompound extra, @NotNull CallbackInfo ci) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
        // 真毒和床蝎子都属于“真实中毒”，机器人直接免疫；幻觉试剂不会走这里，所以不受影响。
        if (gameWorld.isRole(this.player, KinsWatheRoles.ROBOT)
                && (GameConstants.DeathReasons.POISON.equals(source) || GameConstants.DeathReasons.BED_POISON.equals(source))) {
            if (this.player instanceof ServerPlayerEntity serverPlayer && GameConstants.DeathReasons.POISON.equals(source)) {
                // 真实投毒时记录免疫回放，便于展示“无法被带毒物品中毒”。
                NbtCompound replayData = extra == null ? new NbtCompound() : extra.copy();
                if (poisoner != null) {
                    replayData.putUuid("poisoner", poisoner);
                }
                GameRecordManager.recordGlobalEvent(serverPlayer.getServerWorld(), KinsWathe.id("robot_poison_immune"), serverPlayer, replayData);
            }
            ci.cancel();
        }
    }
}
