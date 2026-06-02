package org.BsXinQin.kinswathe.mixin.roles.drugmaker;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerPoisonComponent.class)
public abstract class DrugmakerGiveCoinsMixin {

    @Shadow @Final @NotNull private PlayerEntity player;
    @Shadow public int poisonTicks;
    @Unique private static final UUID DELUSION_MARKER = UUID.fromString("00000000-0000-0000-dead-c0de00000000");

    @Inject(method = "setDetailedPoisonTicks", at = @At("HEAD"))
    private void giveDrugmakerCoins(int ticks, @Nullable UUID poisoner, @NotNull Identifier source, @Nullable NbtCompound extra, CallbackInfo ci) {
        /*
         * Wathe 真实投毒已经改走 setDetailedPoisonTicks，
         * 所以制毒师“有人中毒就加金币”也必须挂到同一条链路上。
         *
         * 这里只在目标原本没有中毒、这次第一次进入中毒状态时发钱，
         * 避免吹矢续毒或重复注毒把同一名目标反复算作新的中毒事件。
         */
        if (ticks <= 0 || this.poisonTicks > 0 || GameFunctions.isPlayerSpectatingOrCreative(this.player)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
        if (gameWorld.isRole(this.player, KinsWatheRoles.ROBOT) || !(this.player instanceof @NotNull ServerPlayerEntity) || (poisoner != null && (poisoner.equals(DELUSION_MARKER) || (FabricLoader.getInstance().isModLoaded("noellesroles") && gameWorld.isRole(poisoner, KinsWatheRoles.noellesrolesRoles("BARTENDER")))))) return;
        for (ServerPlayerEntity serverPlayer : this.player.getServer().getPlayerManager().getPlayerList()) {
            if (serverPlayer == null) continue;
            if (gameWorld.isRole(serverPlayer, KinsWatheRoles.DRUGMAKER) && GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
                PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(serverPlayer);
                serverPlayer.sendMessage(Text.translatable("tip.kinswathe.drugmaker.poisoned").withColor(KinsWatheRoles.DRUGMAKER.color()), true);
                playerShop.balance += KinsWatheConfig.HANDLER.instance().DrugmakerGetCoins;
                playerShop.sync();
            }
        }
    }
}
