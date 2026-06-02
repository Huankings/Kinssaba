package org.BsXinQin.kinswathe.mixin.roles.robot;

import dev.doctor4t.wathe.api.bed.BedEffectRegistry;
import dev.doctor4t.wathe.block_entity.TrimmedBedBlockEntity;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.record.GameRecordManager;
import dev.doctor4t.wathe.util.PoisonUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PoisonUtils.class)
public abstract class RobotBedPoisonMixin {

    @Inject(method = "bedPoison", at = @At("HEAD"), cancellable = true)
    private static void kinswathe$cancelRobotBedPoison(ServerPlayerEntity player, CallbackInfo ci) {
        if (!GameWorldComponent.KEY.get(player.getWorld()).isRole(player, KinsWatheRoles.ROBOT)) {
            return;
        }

        // bedPoison 本体会顺手发 overlay 和施加中毒，这里提前截断，避免机器人仍被蝎子中毒。
        TrimmedBedBlockEntity blockEntity = BedEffectRegistry.findTriggeredBedEffect(player.getEntityWorld(), player.getBlockPos());
        if (blockEntity == null) {
            return;
        }

        UUID poisoner = blockEntity.getPoisoner();
        blockEntity.setHasScorpion(false, null);
        if (poisoner != null) {
            // 床蝎子免疫也记一条回放，表示“来自谁放置的蝎子没有生效”。
            NbtCompound extra = new NbtCompound();
            extra.putUuid("poisoner", poisoner);
            GameRecordManager.recordGlobalEvent(player.getServerWorld(), KinsWathe.id("robot_bed_poison_immune"), player, extra);
        }
        ci.cancel();
    }
}
