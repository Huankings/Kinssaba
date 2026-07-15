package org.BsXinQin.kinswathe.roles.hunter;

import dev.doctor4t.wathe.api.shop.ShopContext;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.KinsWatheShops;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 追猎者杀手商店修改器。
 *
 * <p>追猎者保留大部分默认杀手商品，只新增猎刀、提高普通刀价格，
 * 并移除默认毒物，和旧追猎者商店列表保持一致。</p>
 */
public final class HunterShopHandler {
    private HunterShopHandler() {
    }

    public static void modifyShop(@NotNull ShopContext context, @NotNull List<ShopEntry> entries) {
        if (context.role() != KinsWatheRoles.HUNTER) {
            return;
        }

        // 旧追猎者商店没有毒药瓶和蝎子；其余默认杀手武器/工具继续保留。
        KinsWatheShops.removeItem(entries, WatheItems.POISON_VIAL);
        KinsWatheShops.removeItem(entries, WatheItems.SCORPION);

        // 猎刀插在普通刀前，表示追猎者的首选武器。
        KinsWatheShops.insertBeforeItem(entries, WatheItems.KNIFE, new ShopEntry(
                KinsWatheItems.HUNTING_KNIFE.getDefaultStack(),
                KinsWatheShops.getDefaultPrice(WatheItems.KNIFE, 100),
                ShopEntry.Type.WEAPON
        ));

        // 普通刀保留可购买，但价格变成默认价的 7/4。
        KinsWatheShops.replaceItem(entries, WatheItems.KNIFE, new ShopEntry(
                WatheItems.KNIFE.getDefaultStack(),
                KinsWatheShops.getDefaultPrice(WatheItems.KNIFE, 100) * 7 / 4,
                ShopEntry.Type.WEAPON
        ));
    }
}
