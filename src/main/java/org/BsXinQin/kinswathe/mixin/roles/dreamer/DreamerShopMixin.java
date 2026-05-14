package org.BsXinQin.kinswathe.mixin.roles.dreamer;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.KinsWatheShops;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerShopComponent.class)
public abstract class DreamerShopMixin {

    @Shadow public int balance;
    @Shadow public abstract void sync();
    @Shadow @Final @NotNull private PlayerEntity player;

    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    void tryBuy(int index, @NotNull CallbackInfo ci) {
        if (!FabricLoader.getInstance().isModLoaded("noellesroles")) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
        if (gameWorld.isRole(this.player, KinsWatheRoles.DREAMER)) {
            // 梦者商店依赖 noellesroles 提供的 FRAMING_ROLES_SHOP。
            // 这里先兜底判空/判空列表，避免未来对方重构后读取失败时直接崩。
            java.util.List<ShopEntry> entries = KinsWatheShops.getKillerNeutralRolesShop();
            if (entries.isEmpty()) {
                return;
            }
            if (index < 0 || index >= entries.size()) return;
            ShopEntry entry = entries.get(index);
            if (KinsWatheShops.handlePurchase(this.player, this.balance, entry.stack().getItem(), entry.price())) {
                this.balance -= entry.price();
                this.sync();
            }
            ci.cancel();
        }
    }
}
