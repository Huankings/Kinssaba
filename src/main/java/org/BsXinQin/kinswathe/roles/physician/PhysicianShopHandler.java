package org.BsXinQin.kinswathe.roles.physician;

import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class PhysicianShopHandler {
    private PhysicianShopHandler() {
    }

    public static @NotNull List<ShopEntry> getShopEntries(@NotNull World world) {
        return Util.make(new ArrayList<>(), entries ->
                entries.add(new ShopEntry(KinsWatheItems.PILL.getDefaultStack(), ConfigWorldComponent.KEY.get(world).PhysicianPillPrice, ShopEntry.Type.POISON)));
    }
}
