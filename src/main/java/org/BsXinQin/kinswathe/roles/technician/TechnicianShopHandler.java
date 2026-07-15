package org.BsXinQin.kinswathe.roles.technician;

import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class TechnicianShopHandler {
    private TechnicianShopHandler() {
    }

    public static @NotNull List<ShopEntry> getShopEntries(@NotNull World world) {
        ConfigWorldComponent config = ConfigWorldComponent.KEY.get(world);
        return Util.make(new ArrayList<>(), entries -> {
            entries.add(new ShopEntry(KinsWatheItems.WRENCH.getDefaultStack(), config.TechnicianWrenchPrice, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(KinsWatheItems.CAPTURE_DEVICE.getDefaultStack(), config.TechnicianCaptureDevicePrice, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(KinsWatheItems.ICON_POWER_RESTORATION.getDefaultStack(), config.TechnicianPowerRestorationPrice, ShopEntry.Type.TOOL));
        });
    }
}
