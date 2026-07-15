package org.BsXinQin.kinswathe.roles.kidnapper;

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
 * 绑匪杀手商店修改器。
 *
 * <p>绑匪旧商店基本等于默认杀手商店：额外加入迷药，并去掉撬棍。
 * 因此这里也只做这两个差异，而不复制整套默认商店。</p>
 */
public final class KidnapperShopHandler {
    private KidnapperShopHandler() {
    }

    public static void modifyShop(@NotNull ShopContext context, @NotNull List<ShopEntry> entries) {
        if (context.role() != KinsWatheRoles.KIDNAPPER) {
            return;
        }

        ConfigWorldComponent config = ConfigWorldComponent.KEY.get(context.player().getWorld());

        // 旧绑匪商店不出售撬棍，其他默认武器、毒物和工具继续保留。
        KinsWatheShops.removeItem(entries, WatheItems.CROWBAR);

        /*
         * 迷药插在疯魔模式后、毒物区前，保持旧商店“杀手爆发道具 -> 绑匪毒物 -> 普通毒物”的顺序。
         */
        KinsWatheShops.insertAfterItem(entries, WatheItems.PSYCHO_MODE, new ShopEntry(
                KinsWatheItems.KNOCKOUT_DRUG.getDefaultStack(),
                config.KidnapperKnockoutDrugPrice,
                ShopEntry.Type.POISON
        ));
    }
}
