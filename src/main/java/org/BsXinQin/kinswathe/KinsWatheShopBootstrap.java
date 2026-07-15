package org.BsXinQin.kinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.shop.RoleShopProvider;
import dev.doctor4t.wathe.api.shop.ShopApi;
import dev.doctor4t.wathe.api.shop.ShopPurchaseContext;
import dev.doctor4t.wathe.api.shop.ShopPurchaseResult;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.roles.cook.CookShopHandler;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerShopHandler;
import org.BsXinQin.kinswathe.roles.drugmaker.DrugmakerShopHandler;
import org.BsXinQin.kinswathe.roles.hacker.HackerShopHandler;
import org.BsXinQin.kinswathe.roles.hunter.HunterShopHandler;
import org.BsXinQin.kinswathe.roles.kidnapper.KidnapperShopHandler;
import org.BsXinQin.kinswathe.roles.licensed_villain.LicensedVillainShopHandler;
import org.BsXinQin.kinswathe.roles.physician.PhysicianShopHandler;
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
        /*
         * 这三个杀手职业只修改默认杀手商店：删减商品、插入专属物品、调整价格。
         * 不注册 RoleShopProvider，才能让 Wathe 先生成默认杀手商店，再交给 ShopModifier 做差异化处理。
         */
        ShopApi.registerShopModifier(KinsWathe.id("drugmaker_shop"), ShopApi.DEFAULT_PRIORITY, DrugmakerShopHandler::modifyShop);
        ShopApi.registerShopModifier(KinsWathe.id("hunter_shop"), ShopApi.DEFAULT_PRIORITY, HunterShopHandler::modifyShop);
        ShopApi.registerShopModifier(KinsWathe.id("kidnapper_shop"), ShopApi.DEFAULT_PRIORITY, KidnapperShopHandler::modifyShop);

        register(KinsWatheRoles.HACKER, player -> HackerShopHandler.getShopEntries(player.getWorld()));
        register(KinsWatheRoles.TECHNICIAN, player -> TechnicianShopHandler.getShopEntries(player.getWorld()));
        register(KinsWatheRoles.COOK, player -> CookShopHandler.getShopEntries(player.getWorld()));
        register(KinsWatheRoles.PHYSICIAN, player -> PhysicianShopHandler.getShopEntries(player.getWorld()));
        register(KinsWatheRoles.LICENSED_VILLAIN, player -> LicensedVillainShopHandler.getShopEntries(player.getWorld()));

        /*
         * 梦者复用 NoellesRoles 的伪装商店。KinsWatheShops 内部已经做了 noellesroles
         * 未加载时的空列表兜底，所以这里可以直接注册，不需要再写客户端/服务端双份判空 mixin。
         */
        register(KinsWatheRoles.DREAMER, player -> DreamerShopHandler.getShopEntries());
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
