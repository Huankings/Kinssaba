package org.BsXinQin.kinswathe.client.mixin.roles.dreamer;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.KinsWatheShops;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class DreamerShopMixin extends LimitedHandledScreen<PlayerScreenHandler> {

    @Shadow @Final @NotNull public ClientPlayerEntity player;
    public DreamerShopMixin(@NotNull PlayerScreenHandler handler, @NotNull PlayerInventory inventory, @NotNull Text title) {super(handler, inventory, title);}

    @Inject(method = "init", at = @At("HEAD"))
    public void getShop(CallbackInfo ci) {
        if (!FabricLoader.getInstance().isModLoaded("noellesroles")) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
        if (gameWorld.isRole(this.player, KinsWatheRoles.DREAMER)) {
            List<ShopEntry> entries = KinsWatheShops.getKillerNeutralRolesShop();
            if (entries.isEmpty()) {
                // 如果 noellesroles 那边的共享伪装商店暂时不可用，这里直接不渲染，
                // 至少可以避免客户端打开界面时因为空引用而崩溃。
                return;
            }
            int apart = 36;
            int x = this.width / 2 - (entries.size()) * apart / 2 + 9;
            int shouldBeY = (this.height - 32) / 2;
            int y = shouldBeY - 46;
            for(int i = 0; i < entries.size(); ++i) {
                addDrawableChild(new LimitedInventoryScreen.StoreItemWidget((LimitedInventoryScreen) (Object) this, x + apart * i, y, entries.get(i), i));
            }
        }
    }
}
