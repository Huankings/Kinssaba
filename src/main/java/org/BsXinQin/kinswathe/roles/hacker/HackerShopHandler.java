package org.BsXinQin.kinswathe.roles.hacker;

import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheShops;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class HackerShopHandler {
    private HackerShopHandler() {
    }

    public static @NotNull List<ShopEntry> getShopEntries(@NotNull World world) {
        if (!KinsWatheConfig.HANDLER.instance().HackerHasShop) {
            return List.of();
        }

        ConfigWorldComponent config = ConfigWorldComponent.KEY.get(world);
        return Util.make(new ArrayList<>(), entries -> {
            entries.add(new ShopEntry(WatheItems.LOCKPICK.getDefaultStack(), KinsWatheShops.getDefaultPrice(WatheItems.LOCKPICK, 50), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.BLACKOUT.getDefaultStack(), KinsWatheShops.getDefaultPrice(WatheItems.BLACKOUT, 250), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(KinsWatheItems.ICON_WEAPON_COOLDOWN_REFRESH.getDefaultStack(), config.HackerRefreshWeaponCooldownPrice, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(KinsWatheItems.ICON_ABILITY_COOLDOWN_REFRESH.getDefaultStack(), config.HackerRefreshAbilityCooldownPrice, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(KinsWatheItems.ICON_POTION_EFFECT_REFRESH.getDefaultStack(), config.HackerRefreshPotionEffectPrice, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.FIRECRACKER.getDefaultStack(), KinsWatheShops.getDefaultPrice(WatheItems.FIRECRACKER, 10), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(new ItemStack(WatheItems.NOTE, 4), KinsWatheShops.getDefaultPrice(WatheItems.NOTE, 10), ShopEntry.Type.TOOL));
        });
    }
}
