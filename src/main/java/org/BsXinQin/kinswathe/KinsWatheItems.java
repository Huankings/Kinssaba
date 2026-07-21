package org.BsXinQin.kinswathe;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.items.*;
import org.jetbrains.annotations.NotNull;

public class KinsWatheItems {

    /// 新增物品
    //吹矢
    public static final Item BLOWGUN = registerItem(
            new BlowgunItem(new Item.Settings().maxCount(1)),
            "blowgun"
    );
    //捕捉装置
    public static final Item CAPTURE_DEVICE = registerItem(
            new CaptureDeviceItem(new Item.Settings().maxCount(1)),
            "capture_device"
    );
    //猎刀
    public static final Item HUNTING_KNIFE = registerItem(
            new HuntingKnifeItem(new Item.Settings().maxCount(1)),
            "hunting_knife"
    );
    //迷药
    public static final Item KNOCKOUT_DRUG = registerItem(
            new KnockoutDrugItem(new Item.Settings().maxCount(4)),
            "knockout_drug"
    );
    //毒液注射器
    public static final Item POISON_INJECTOR = registerItem(
            new PoisonInjectorItem(new Item.Settings().maxCount(1)),
            "poison_injector"
    );
    //硫酸桶
    public static final Item SULFURIC_ACID_BARREL = registerItem(
            new SulfuricAcidBarrelItem(new Item.Settings().maxCount(1)),
            "sulfuric_acid_barrel"
    );
    //扳手
    public static final Item WRENCH = registerItem(
            new WrenchItem(new Item.Settings().maxCount(1)),
            "wrench"
    );

    /// 新增图标
    //电力恢复图标
    public static final Item ICON_POWER_RESTORATION = registerItem(
            new Item(new Item.Settings().maxCount(1)),
            "icon_power_restoration"
    );

    /// 注册方法
    public static Item registerItem(@NotNull Item item, @NotNull String id) {
        Identifier itemID = Identifier.of(KinsWathe.MOD_ID, id);
        return Registry.register(Registries.ITEM, itemID, item);
    }

    /// 设置物品使用
    public static void setItemAfterUsing(@NotNull PlayerEntity player, @NotNull Item item, Hand hand) {
        Integer cooldown = GameConstants.ITEM_COOLDOWNS.get(item);
        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
            if (cooldown != null) player.getItemCooldownManager().set(item, cooldown);
            if (hand != null) player.getStackInHand(hand).decrement(1);
        }
    }

    /// 添加物品冷却
    public static void addItemCooldowns() {
        //物品冷却
        GameConstants.ITEM_COOLDOWNS.put(BLOWGUN, GameConstants.getInTicks(1,0));
        GameConstants.ITEM_COOLDOWNS.put(CAPTURE_DEVICE, GameConstants.getInTicks(1,0));
        GameConstants.ITEM_COOLDOWNS.put(HUNTING_KNIFE, GameConstants.getInTicks(0,45));
        GameConstants.ITEM_COOLDOWNS.put(KNOCKOUT_DRUG, GameConstants.getInTicks(0,45));
        GameConstants.ITEM_COOLDOWNS.put(POISON_INJECTOR, GameConstants.getInTicks(1,0));
        GameConstants.ITEM_COOLDOWNS.put(SULFURIC_ACID_BARREL, GameConstants.getInTicks(1,0));
        GameConstants.ITEM_COOLDOWNS.put(WRENCH, GameConstants.getInTicks(2,0));
        //图标冷却
        GameConstants.ITEM_COOLDOWNS.put(ICON_POWER_RESTORATION, GameConstants.getInTicks(3,0));
    }

    /// 添加物品组别
    public static void addItemGroup() {
        ItemGroupEvents.modifyEntriesEvent(WatheItems.EQUIPMENT_GROUP).register(entries -> {
            entries.add(BLOWGUN);
            entries.add(CAPTURE_DEVICE);
            entries.add(HUNTING_KNIFE);
            entries.add(KNOCKOUT_DRUG);
            entries.add(POISON_INJECTOR);
            entries.add(SULFURIC_ACID_BARREL);
            entries.add(WRENCH);
        });
    }

    /// 初始化方法
    public static void init() {
        //添加物品冷却
        addItemCooldowns();
        //添加物品组别
        addItemGroup();
    }
}
