package org.BsXinQin.kinswathe.mixin.modifiers.magnate;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MurderGameMode.class)
public abstract class MagnatePassiveIncomeMixin {

    @Inject(method = "tickServerGameLoop", at = @At("TAIL"))
    public void setMagnatePassiveIncome(@NotNull ServerWorld serverWorld, @NotNull GameWorldComponent gameWorld, CallbackInfo ci) {
        if (!gameWorld.isRunning()) return;
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            WorldModifierComponent modifier = WorldModifierComponent.KEY.get(serverWorld);
            PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
            if (modifier.isModifier(player, KinsWatheRoles.MAGNATE)) {
                Role role = gameWorld.getRole(player);
                /*
                 * 富豪的“双倍被动收入”本质上是：在主模组那次结算之后，再额外补发一次同样的被动收入。
                 *
                 * 这里必须继续复用 Wathe 统一的“阵营上限 + 补差额”逻辑，
                 * 否则富豪第二次发钱会绕过上限，导致最终金币超过配置值。
                 * 因此处理方式是：
                 * 1. 先取出这一次原本应得的基础被动收入；
                 * 2. 再按玩家当前阵营和当前余额，计算还能补多少；
                 * 3. 只有实际还能加钱时才同步。
                 */
                int basePassiveIncome = GameConstants.PASSIVE_MONEY_TICKER.apply(serverWorld.getTime());
                int extraIncome = GameConstants.getPassiveMoneyAmount(role == null ? null : role.getFaction(), playerShop.balance, basePassiveIncome);
                if (extraIncome > 0) {
                    playerShop.balance += extraIncome;
                    playerShop.sync();
                }
            }
        }
    }
}
