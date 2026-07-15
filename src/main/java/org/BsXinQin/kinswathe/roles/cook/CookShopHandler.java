package org.BsXinQin.kinswathe.roles.cook;

import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.item.Items;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CookShopHandler {
    private CookShopHandler() {
    }

    public static @NotNull List<ShopEntry> getShopEntries(@NotNull World world) {
        return Util.make(new ArrayList<>(), entries -> {
            entries.add(new ShopEntry(KinsWatheItems.PAN.getDefaultStack(), ConfigWorldComponent.KEY.get(world).CookPanPrice, ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(Items.COOKED_BEEF.getDefaultStack(), 25, ShopEntry.Type.POISON));
            entries.add(new ShopEntry(Items.COOKED_CHICKEN.getDefaultStack(), 25, ShopEntry.Type.POISON));
            entries.add(new ShopEntry(Items.COOKED_PORKCHOP.getDefaultStack(), 25, ShopEntry.Type.POISON));
        });
    }
}
