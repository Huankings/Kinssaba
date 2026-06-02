package org.BsXinQin.kinswathe.packet.items;

import dev.doctor4t.wathe.record.GameRecordManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.jetbrains.annotations.NotNull;

public record PanC2SPacket(int target) implements CustomPayload {

    public static final Identifier PAN_PLAYLOAD_ID = Identifier.of(KinsWathe.MOD_ID, "pan");
    public static final Id<PanC2SPacket> ID = new Id<>(PAN_PLAYLOAD_ID);
    public static final PacketCodec<PacketByteBuf, PanC2SPacket> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, PanC2SPacket::target, PanC2SPacket::new);
    public @NotNull Id<? extends @NotNull CustomPayload> getId() {return ID;}

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<PanC2SPacket> {
        @Override
        public void receive(@NotNull PanC2SPacket payload, ServerPlayNetworking.@NotNull Context context) {
            ServerPlayerEntity player = context.player();
            if (!(player.getServerWorld().getEntityById(payload.target()) instanceof @NotNull PlayerEntity target)) return;
            if (target.distanceTo(player) > 3.0F) return;
            PlayerEffectComponent targetEffect = PlayerEffectComponent.KEY.get(target);
            // 平底锅命中事件单独记一条，供回放显示“用平底锅敲晕了谁”。
            targetEffect.setStunTicks(100, KinsWathe.id("pan_stun_end"));
            if (target instanceof ServerPlayerEntity serverTarget) {
                ItemStack replayStack = player.getMainHandStack().isOf(KinsWatheItems.PAN) ? player.getMainHandStack() : new ItemStack(KinsWatheItems.PAN);
                GameRecordManager.recordItemHit(player, replayStack, serverTarget, null);
            }
            KinsWatheItems.setItemAfterUsing(player, KinsWatheItems.PAN, null);
            target.playSound(SoundEvents.BLOCK_ANVIL_LAND, 0.8f, 0.8f);
            player.swingHand(Hand.MAIN_HAND);
        }
    }
}
