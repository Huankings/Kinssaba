package org.BsXinQin.kinswathe;

import dev.doctor4t.wathe.api.shop.ShopApi;
import dev.doctor4t.wathe.api.shop.ShopPrice;
import dev.doctor4t.wathe.api.shop.ShopPurchaseContext;
import dev.doctor4t.wathe.api.shop.ShopPurchaseResult;
import dev.doctor4t.wathe.api.economy.EconomyApi;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.record.ShopPurchaseTracker;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.BsXinQin.kinswathe.roles.technician.TechnicianComponent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KinsWatheShops {

    private static final Map<String, Integer> ITEM_PRICES = new HashMap<>();
    private static final Map<String, ShopPrice> ITEM_SHOP_PRICES = new HashMap<>();
    private static List<ShopEntry> FRAMING_ROLES_SHOP = Collections.emptyList();

    /// 提取其他模组商店物品价格
    static {
        for (@NotNull ShopEntry entry : GameConstants.SHOP_ENTRIES) {
            String itemKey = getItemKeyFromStack(entry.stack());
            if (itemKey != null) {
                /*
                 * 这里同时缓存旧版 int 金币价格和新版 ShopPrice 完整价格。
                 * int 价格只用于“基于金币做乘除/加减”的旧职业逻辑；
                 * ShopPrice 会保留任务币、多货币 AND、多个支付方案 OR 等完整信息。
                 * 它只适合明确要完整继承默认杀手商品价格的职业，普通商店请按货币单独读取。
                 */
                ITEM_SHOP_PRICES.put(itemKey, entry.shopPrice());
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

    /**
     * 读取 Wathe 默认杀手商店价格。
     *
     * <p>各职业 ShopHandler 只描述“相对默认商店怎么改”，价格统一从这里取，
     * 避免 Wathe 以后调整默认价格时，扩展职业还残留旧数字。</p>
     *
     * <p>注意：这个方法只返回默认商店第 0 组支付方案里的金币价格。
     * 例如开锁器会返回 50，而不会把 25 任务币也带进来。
     * 中立职业、平民职业或只想做金币商店的扩展都应该用这个方法。</p>
     */
    public static int getDefaultPrice(@NotNull Item item, int defaultValue) {
        String itemKey = getItemKeyFromStack(item.getDefaultStack());
        int cachedPrice = itemKey == null ? defaultValue : getItemPrice(itemKey, defaultValue);
        return getDefaultCurrencyPrice(item, 0, EconomyApi.MONEY, cachedPrice);
    }

    /**
     * 按“支付方案索引 + 货币 id”细粒度读取默认价格。
     *
     * <p>用于疯魔模式这类有多组支付方案的商品：调用方可以明确读取
     * 第 0 组金币、第 0 组任务币、第 1 组金币、第 1 组任务币，
     * 不需要也不应该为了取一个数字而复制整套 ShopPrice。</p>
     */
    public static int getDefaultCurrencyPrice(
            @NotNull Item item,
            int optionIndex,
            @NotNull Identifier currency,
            int defaultValue
    ) {
        return ShopApi.getDefaultCurrencyPrice(item, optionIndex, currency, defaultValue);
    }

    /**
     * 读取 Wathe 默认杀手商店的完整多货币价格。
     *
     * <p>和 {@link #getDefaultPrice(Item, int)} 不同，这里返回 {@link ShopPrice}，
     * 因此不会把“50 金币 + 25 任务币”或“100 金币 / 50 任务币二选一”截断成单个金币数。
     * 只有某个职业明确想完整继承默认杀手商品价格时才应该用这个方法；
     * 中立职业如果无法获得任务币，不要用它复制开锁器、匕首、疯魔模式这类多货币条目。</p>
     */
    public static @NotNull ShopPrice getDefaultShopPrice(@NotNull Item item, int defaultValue) {
        String itemKey = getItemKeyFromStack(item.getDefaultStack());
        ShopPrice cachedPrice = itemKey == null ? null : ITEM_SHOP_PRICES.get(itemKey);
        return cachedPrice == null ? ShopPrice.money(getDefaultPrice(item, defaultValue)) : cachedPrice;
    }

    public static int indexOfItem(@NotNull List<ShopEntry> entries, @NotNull Item item) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).stack().isOf(item)) {
                return i;
            }
        }
        return -1;
    }

    public static void removeItem(@NotNull List<ShopEntry> entries, @NotNull Item item) {
        entries.removeIf(entry -> entry.stack().isOf(item));
    }

    public static void replaceItem(@NotNull List<ShopEntry> entries, @NotNull Item item, @NotNull ShopEntry replacement) {
        int index = indexOfItem(entries, item);
        if (index >= 0) {
            entries.set(index, replacement);
        }
    }

    public static void insertBeforeItem(@NotNull List<ShopEntry> entries, @NotNull Item item, @NotNull ShopEntry entry) {
        int index = indexOfItem(entries, item);
        entries.add(index >= 0 ? index : 0, entry);
    }

    public static void insertAfterItem(@NotNull List<ShopEntry> entries, @NotNull Item item, @NotNull ShopEntry entry) {
        int index = indexOfItem(entries, item);
        entries.add(index >= 0 ? index + 1 : entries.size(), entry);
    }

    // 杀手方中立商店兜底数据仍放在公共工具类里，具体职业通过 DreamerShopHandler 读取。
    public static List<ShopEntry> getKillerNeutralRolesShop() {
        return FRAMING_ROLES_SHOP;
    }

    /// 商店处理方法
    public static @NotNull ShopPurchaseResult purchase(@NotNull ShopPurchaseContext context) {
        PlayerEntity player = context.player();
        ShopEntry entry = context.entry();
        Item item = entry.stack().getItem();
        /*
         * 购买判定必须走 Wathe 的 ShopPrice，而不是只比较金币余额。
         * 这样职业商店里的默认开锁器、匕首等条目如果继承了任务币价格，
         * 就能正确判断“金币 + 任务币”或“金币 / 任务币二选一”是否满足。
         */
        if (!context.canAffordEntry() || player.getItemCooldownManager().isCoolingDown(item)) {
            return ShopPurchaseResult.FAIL_SHOW_MESSAGE;
        }

        /*
         * kinssaba 旧商店 mixin 里，很多特殊图标不是发物品，而是立刻执行能力。
         * 现在这些能力仍然集中在这里交付，Wathe 负责后续扣钱、音效、同步和回放。
         */
        return deliverPurchasedStack(player, entry.stack())
                ? ShopPurchaseResult.SUCCESS
                : ShopPurchaseResult.FAIL_SHOW_MESSAGE;
    }

    public static boolean handlePurchase(@NotNull PlayerEntity player, int balance, @NotNull Item item, int price) {
        if (balance >= price && !player.getItemCooldownManager().isCoolingDown(item)) {
            deliverPurchasedStack(player, item == WatheItems.NOTE ? new ItemStack(WatheItems.NOTE, 4) : item.getDefaultStack());
            /*
             * KinsWathe 里很多职业都直接改写了 Wathe 的商店内容，
             * 因此这里在购买成功时主动回填真实商品给 Wathe 回放系统，
             * 防止回放仍按原版固定格子播报错误的商店物品。
             */
            ItemStack purchasedStack = item == WatheItems.NOTE ? new ItemStack(WatheItems.NOTE, 4) : item.getDefaultStack();
            ShopPurchaseTracker.captureSuccessfulPurchase(player, purchasedStack, -1, price);
            ShopApi.playBuySound(player);
            return true;
        } else {
            ShopApi.sendPurchaseFailedMessage(player);
            ShopApi.playFailSound(player);
            return false;
        }
    }

    private static boolean deliverPurchasedStack(@NotNull PlayerEntity player, @NotNull ItemStack stack) {
        Item item = stack.getItem();
        if (item == WatheItems.BLACKOUT) return PlayerShopComponent.useBlackout(player);
        if (item == WatheItems.PSYCHO_MODE) return PlayerShopComponent.usePsychoMode(player);
        if (item == KinsWatheItems.ICON_WEAPON_COOLDOWN_REFRESH) {
            HackerComponent.refreshWeaponCooldown(player);
            return true;
        }
        if (item == KinsWatheItems.ICON_ABILITY_COOLDOWN_REFRESH) {
            HackerComponent.refreshAbilityCooldown(player);
            return true;
        }
        if (item == KinsWatheItems.ICON_POTION_EFFECT_REFRESH) {
            HackerComponent.refreshPotionEffect(player);
            return true;
        }
        if (item == KinsWatheItems.ICON_POWER_RESTORATION) {
            TechnicianComponent.stopBlackout(player);
            return true;
        }
        return player.giveItemStack(stack.copy());
    }
}
