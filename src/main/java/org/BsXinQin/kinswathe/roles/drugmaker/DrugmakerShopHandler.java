package org.BsXinQin.kinswathe.roles.drugmaker;

import dev.doctor4t.wathe.api.shop.ShopContext;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.KinsWatheShops;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 制毒师杀手商店修改器。
 *
 * <p>这里不再构造一整张制毒师商店，而是拿 Wathe 默认杀手商店做局部修改：
 * 插入制毒师专属武器、移除不允许购买的杀手爆发道具，并调整毒物价格。</p>
 */
public final class DrugmakerShopHandler {
    private DrugmakerShopHandler() {
    }

    public static void modifyShop(@NotNull ShopContext context, @NotNull List<ShopEntry> entries) {
        if (context.role() != KinsWatheRoles.DRUGMAKER) {
            return;
        }

        ConfigWorldComponent config = ConfigWorldComponent.KEY.get(context.player().getWorld());

        /*
         * 制毒师旧商店没有左轮、手雷、疯魔模式。
         * 用移除默认项的方式实现，后续 Wathe 默认商店如果新增工具类商品，制毒师仍会自然继承。
         */
        KinsWatheShops.removeItem(entries, WatheItems.REVOLVER);
        KinsWatheShops.removeItem(entries, WatheItems.GRENADE);
        KinsWatheShops.removeItem(entries, WatheItems.PSYCHO_MODE);

        /*
         * 专属武器插在刀前面，最终顺序是：
         * POISON_INJECTOR -> BLOWGUN -> KNIFE。
         */
        KinsWatheShops.insertBeforeItem(entries, WatheItems.KNIFE, new ShopEntry(
                KinsWatheItems.POISON_INJECTOR.getDefaultStack(),
                config.DrugmakerPoisonInjectorPrice,
                ShopEntry.Type.WEAPON
        ));
        KinsWatheShops.insertBeforeItem(entries, WatheItems.KNIFE, new ShopEntry(
                KinsWatheItems.BLOWGUN.getDefaultStack(),
                config.DrugmakerBlowgunPrice,
                ShopEntry.Type.WEAPON
        ));

        // 刀更贵，毒物更便宜；价格都基于 Wathe 当前默认价格计算。
        KinsWatheShops.replaceItem(entries, WatheItems.KNIFE, new ShopEntry(
                WatheItems.KNIFE.getDefaultStack(),
                KinsWatheShops.getDefaultPrice(WatheItems.KNIFE, 100) * 2,
                ShopEntry.Type.WEAPON
        ));
        KinsWatheShops.replaceItem(entries, WatheItems.POISON_VIAL, new ShopEntry(
                WatheItems.POISON_VIAL.getDefaultStack(),
                KinsWatheShops.getDefaultPrice(WatheItems.POISON_VIAL, 70) / 2,
                ShopEntry.Type.POISON
        ));
        KinsWatheShops.replaceItem(entries, WatheItems.SCORPION, new ShopEntry(
                WatheItems.SCORPION.getDefaultStack(),
                KinsWatheShops.getDefaultPrice(WatheItems.SCORPION, 40) / 2,
                ShopEntry.Type.POISON
        ));
    }
}
