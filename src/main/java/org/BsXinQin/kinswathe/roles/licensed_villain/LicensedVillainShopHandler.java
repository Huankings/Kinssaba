package org.BsXinQin.kinswathe.roles.licensed_villain;

import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class LicensedVillainShopHandler {
    private LicensedVillainShopHandler() {
    }

    public static @NotNull List<ShopEntry> getShopEntries(@NotNull World world) {
        return Util.make(new ArrayList<>(), entries ->
                entries.add(new ShopEntry(WatheItems.REVOLVER.getDefaultStack(), ConfigWorldComponent.KEY.get(world).LicensedVillainRevolverPrice, ShopEntry.Type.WEAPON)));
    }
}
