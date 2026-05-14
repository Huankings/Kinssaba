package org.BsXinQin.kinswathe;

import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.BsXinQin.kinswathe.roles.technician.TechnicianComponent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KinsWatheShops {

    private static final Map<String, Integer> ITEM_PRICES = new HashMap<>();
    private static List<ShopEntry> FRAMING_ROLES_SHOP = Collections.emptyList();

    /// 提取其他模组商店物品价格
    static {
        for (@NotNull ShopEntry entry : GameConstants.SHOP_ENTRIES) {
            String itemKey = getItemKeyFromStack(entry.stack());
            if (itemKey != null) {
                ITEM_PRICES.put(itemKey, entry.price());
            }
        }
        if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
            try {
                Class<?> noellesRolesClass = Class.forName("org.agmas.noellesroles.Noellesroles");
                Field framingShopField = noellesRolesClass.getField("FRAMING_ROLES_SHOP");
                Object shop = framingShopField.get(null);
                if (shop instanceof List<?> list) {
                    // 这里直接保留对 noellesroles 原列表对象的引用，
                    // 这样即使对方在 onInitialize 里继续往列表追加内容，这边也能同步看到最新条目。
                    FRAMING_ROLES_SHOP = (List<ShopEntry>) list;
                }
            } catch (Exception exception) {
                // 兼容失败时退回空列表，避免梦者客户端/服务端商店界面直接空指针崩溃。
                FRAMING_ROLES_SHOP = Collections.emptyList();
            }
        }
    }

    private static String getItemKeyFromStack(@NotNull ItemStack stack) {
        if (stack.getItem() == WatheItems.KNIFE) return "KNIFE";
        if (stack.getItem() == WatheItems.REVOLVER) return "REVOLVER";
        if (stack.getItem() == WatheItems.GRENADE) return "GRENADE";
        if (stack.getItem() == WatheItems.PSYCHO_MODE) return "PSYCHO_MODE";
        if (stack.getItem() == WatheItems.POISON_VIAL) return "POISON_VIAL";
        if (stack.getItem() == WatheItems.SCORPION) return "SCORPION";
        if (stack.getItem() == WatheItems.FIRECRACKER) return "FIRECRACKER";
        if (stack.getItem() == WatheItems.LOCKPICK) return "LOCKPICK";
        if (stack.getItem() == WatheItems.CROWBAR) return "CROWBAR";
        if (stack.getItem() == WatheItems.BODY_BAG) return "BODY_BAG";
        if (stack.getItem() == WatheItems.BLACKOUT) return "BLACKOUT";
        if (stack.getItem() == WatheItems.NOTE) return "NOTE";
        return null;
    }

    private static int getItemPrice(@NotNull String itemKey, int defaultValue) {
        return ITEM_PRICES.getOrDefault(itemKey, defaultValue);
    }

    /// 身份商店
    //制毒师商店
    public static List<ShopEntry> getDrugmakerShop(@NotNull World world) {
        return Util.make(new ArrayList<>(), (entries) -> {
            entries.add(new ShopEntry(KinsWatheItems.POISON_INJECTOR.getDefaultStack(), ConfigWorldComponent.KEY.get(world).DrugmakerPoisonInjectorPrice, ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(KinsWatheItems.BLOWGUN.getDefaultStack(), ConfigWorldComponent.KEY.get(world).DrugmakerBlowgunPrice, ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(WatheItems.KNIFE.getDefaultStack(), getItemPrice("KNIFE", 100) * 2, ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(WatheItems.POISON_VIAL.getDefaultStack(), getItemPrice("POISON_VIAL", 100) / 2, ShopEntry.Type.POISON));
            entries.add(new ShopEntry(WatheItems.SCORPION.getDefaultStack(), getItemPrice("SCORPION", 50) / 2, ShopEntry.Type.POISON));
            entries.add(new ShopEntry(WatheItems.FIRECRACKER.getDefaultStack(), getItemPrice("FIRECRACKER", 10), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.LOCKPICK.getDefaultStack(), getItemPrice("LOCKPICK", 50), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.CROWBAR.getDefaultStack(), getItemPrice("CROWBAR", 25), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.BODY_BAG.getDefaultStack(), getItemPrice("BODY_BAG", 200), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.BLACKOUT.getDefaultStack(), getItemPrice("BLACKOUT", 200), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.NOTE.getDefaultStack(), getItemPrice("NOTE", 10), ShopEntry.Type.TOOL));
        });
    }
    //黑客商店
    public static List<ShopEntry> getHackerShop(@NotNull World world) {
        if (!KinsWatheConfig.HANDLER.instance().HackerHasShop) return null;
        return Util.make(new ArrayList<>(), (entries) -> {
            entries.add(new ShopEntry(WatheItems.LOCKPICK.getDefaultStack(), getItemPrice("LOCKPICK", 50), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.BLACKOUT.getDefaultStack(), getItemPrice("BLACKOUT", 200), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(KinsWatheItems.ICON_WEAPON_COOLDOWN_REFRESH.getDefaultStack(), ConfigWorldComponent.KEY.get(world).HackerRefreshWeaponCooldownPrice, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(KinsWatheItems.ICON_ABILITY_COOLDOWN_REFRESH.getDefaultStack(), ConfigWorldComponent.KEY.get(world).HackerRefreshAbilityCooldownPrice, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(KinsWatheItems.ICON_POTION_EFFECT_REFRESH.getDefaultStack(), ConfigWorldComponent.KEY.get(world).HackerRefreshPotionEffectPrice, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.FIRECRACKER.getDefaultStack(), getItemPrice("FIRECRACKER", 10), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.NOTE.getDefaultStack(), getItemPrice("NOTE", 10), ShopEntry.Type.TOOL));
        });
    }
    //追猎者商店
    public static List<ShopEntry> getHunterShop(@NotNull World world) {
        return Util.make(new ArrayList<>(), (entries) -> {
            entries.add(new ShopEntry(KinsWatheItems.HUNTING_KNIFE.getDefaultStack(), getItemPrice("KNIFE", 100), ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(WatheItems.KNIFE.getDefaultStack(), getItemPrice("KNIFE", 100) * 7 / 4, ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(WatheItems.REVOLVER.getDefaultStack(), getItemPrice("REVOLVER", 300), ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(WatheItems.GRENADE.getDefaultStack(), getItemPrice("GRENADE", 350), ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(WatheItems.PSYCHO_MODE.getDefaultStack(), getItemPrice("PSYCHO_MODE", 300), ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(WatheItems.FIRECRACKER.getDefaultStack(), getItemPrice("FIRECRACKER", 10), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.LOCKPICK.getDefaultStack(), getItemPrice("LOCKPICK", 50), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.CROWBAR.getDefaultStack(), getItemPrice("CROWBAR", 25), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.BODY_BAG.getDefaultStack(), getItemPrice("BODY_BAG", 200), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.BLACKOUT.getDefaultStack(), getItemPrice("BLACKOUT", 200), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.NOTE.getDefaultStack(), getItemPrice("NOTE", 10), ShopEntry.Type.TOOL));
        });
    }
    //绑匪商店
    public static List<ShopEntry> getKidnapperShop(@NotNull World world) {
        return Util.make(new ArrayList<>(), (entries) -> {
            entries.add(new ShopEntry(WatheItems.KNIFE.getDefaultStack(), getItemPrice("KNIFE", 100), ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(WatheItems.REVOLVER.getDefaultStack(), getItemPrice("REVOLVER", 300), ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(WatheItems.GRENADE.getDefaultStack(), getItemPrice("GRENADE", 350), ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(WatheItems.PSYCHO_MODE.getDefaultStack(), getItemPrice("PSYCHO_MODE", 300), ShopEntry.Type.WEAPON));
            entries.add(new ShopEntry(KinsWatheItems.KNOCKOUT_DRUG.getDefaultStack(), ConfigWorldComponent.KEY.get(world).KidnapperKnockoutDrugPrice, ShopEntry.Type.POISON));
            entries.add(new ShopEntry(WatheItems.POISON_VIAL.getDefaultStack(), getItemPrice("POISON_VIAL", 100), ShopEntry.Type.POISON));
            entries.add(new ShopEntry(WatheItems.SCORPION.getDefaultStack(), getItemPrice("SCORPION", 50), ShopEntry.Type.POISON));
            entries.add(new ShopEntry(WatheItems.FIRECRACKER.getDefaultStack(), getItemPrice("FIRECRACKER", 10), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.LOCKPICK.getDefaultStack(), getItemPrice("LOCKPICK", 50), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.BODY_BAG.getDefaultStack(), getItemPrice("BODY_BAG", 200), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.BLACKOUT.getDefaultStack(), getItemPrice("BLACKOUT", 200), ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.NOTE.getDefaultStack(), getItemPrice("NOTE", 10), ShopEntry.Type.TOOL));
        });
    }
    //技术员商店
    public static List<ShopEntry> getTechnicianShop(@NotNull World world) {
        return Util.make(new ArrayList<>(), (entries) -> {
            entries.add(new ShopEntry(KinsWatheItems.WRENCH.getDefaultStack(), ConfigWorldComponent.KEY.get(world).TechnicianWrenchPrice, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(KinsWatheItems.CAPTURE_DEVICE.getDefaultStack(), ConfigWorldComponent.KEY.get(world).TechnicianCaptureDevicePrice, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(KinsWatheItems.ICON_POWER_RESTORATION.getDefaultStack(), ConfigWorldComponent.KEY.get(world).TechnicianPowerRestorationPrice, ShopEntry.Type.TOOL));
        });
    }
    //杀手方中立商店
    public static List<ShopEntry> getKillerNeutralRolesShop() {
        return FRAMING_ROLES_SHOP;
    }

    /// 商店处理方法
    public static boolean handlePurchase(@NotNull PlayerEntity player, int balance, @NotNull Item item, int price) {
        if (balance >= price && !player.getItemCooldownManager().isCoolingDown(item)) {
            if (item == WatheItems.NOTE) player.giveItemStack((new ItemStack(WatheItems.NOTE, 4)));
            else if (item == WatheItems.BLACKOUT) PlayerShopComponent.useBlackout(player);
            else if (item == WatheItems.PSYCHO_MODE) PlayerShopComponent.usePsychoMode(player);
            else if (item == KinsWatheItems.ICON_WEAPON_COOLDOWN_REFRESH) HackerComponent.refreshWeaponCooldown(player);
            else if (item == KinsWatheItems.ICON_ABILITY_COOLDOWN_REFRESH) HackerComponent.refreshAbilityCooldown(player);
            else if (item == KinsWatheItems.ICON_POTION_EFFECT_REFRESH) HackerComponent.refreshPotionEffect(player);
            else if (item == KinsWatheItems.ICON_POWER_RESTORATION) TechnicianComponent.stopBlackout(player);
            else player.giveItemStack(item.getDefaultStack());
            if (player instanceof @NotNull ServerPlayerEntity serverPlayer) {
                serverPlayer.playSoundToPlayer(WatheSounds.UI_SHOP_BUY, SoundCategory.PLAYERS,1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
            }
            return true;
        } else {
            player.sendMessage(Text.translatable("shop.purchase_failed").withColor(0xAA0000), true);
            if (player instanceof @NotNull ServerPlayerEntity serverPlayer) {
                serverPlayer.playSoundToPlayer(WatheSounds.UI_SHOP_BUY_FAIL, SoundCategory.PLAYERS,1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
            }
            return false;
        }
    }
}
