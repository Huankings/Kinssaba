package org.BsXinQin.kinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.shop.RoleShopProvider;
import dev.doctor4t.wathe.api.shop.ShopApi;
import dev.doctor4t.wathe.api.shop.ShopPurchaseContext;
import dev.doctor4t.wathe.api.shop.ShopPurchaseResult;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.roles.licensed_villain.LicensedVillainShopHandler;
import org.BsXinQin.kinswathe.roles.technician.TechnicianShopHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * kinssaba 职业商店统一注册入口。
 *
 * <p>旧实现是每个职业各写一份 PlayerShopComponent mixin 和一份 LimitedInventoryScreen mixin。
 * 现在统一注册到 Wathe ShopApi：职业 handler 仍负责“卖什么”，Wathe 负责“怎么显示、怎么买、怎么扣钱”。</p>
 */
public final class KinsWatheShopBootstrap {
    private KinsWatheShopBootstrap() {
    }

    public static void init() {
        register(KinsWatheRoles.TECHNICIAN, player -> TechnicianShopHandler.getShopEntries(player.getWorld()));
        register(KinsWatheRoles.LICENSED_VILLAIN, player -> LicensedVillainShopHandler.getShopEntries(player.getWorld()));
    }

    private static void register(@NotNull Role role, @NotNull EntriesProvider entriesProvider) {
        ShopApi.registerRoleShop(role, new RoleShopProvider() {
            @Override
            public @NotNull List<ShopEntry> getShopEntries(@NotNull PlayerEntity player) {
                List<ShopEntry> entries = entriesProvider.getEntries(player);
                return entries == null ? List.of() : entries;
            }

            @Override
            public @NotNull ShopPurchaseResult purchase(@NotNull ShopPurchaseContext context) {
                /*
                 * 所有 kinssaba 职业商店都走同一个交付入口。
                 * 特殊图标会在那里执行能力，普通商品则发到玩家背包；公共结算继续由 Wathe 统一处理。
                 */
                return KinsWatheShops.purchase(context);
            }
        });
    }

    @FunctionalInterface
    private interface EntriesProvider {
        List<ShopEntry> getEntries(@NotNull PlayerEntity player);
    }
}
