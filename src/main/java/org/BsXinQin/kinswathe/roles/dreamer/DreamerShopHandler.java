package org.BsXinQin.kinswathe.roles.dreamer;

import dev.doctor4t.wathe.util.ShopEntry;
import org.BsXinQin.kinswathe.KinsWatheShops;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class DreamerShopHandler {
    private DreamerShopHandler() {
    }

    public static @NotNull List<ShopEntry> getShopEntries() {
        return KinsWatheShops.getKillerNeutralRolesShop();
    }
}
