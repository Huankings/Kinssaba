package org.BsXinQin.kinswathe;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.record.KinsWatheReplayFormatters;
import org.BsXinQin.kinswathe.victory.KinsWatheVictoryRules;

public class KinsWathe implements ModInitializer {

    public static String MOD_ID = "kinswathe";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        //初始化游戏设置
        KinsWatheGameSettings.init();
        //初始化角色
        KinsWatheRoles.init();
        //注册独立胜利 / 保活规则：现在统一交给 Wathe VictoryApi 仲裁，不再 mixin MurderGameMode。
        KinsWatheVictoryRules.init();
        //初始化物品
        KinsWatheItems.init();
        // 初始化职业商店：商品列表由 kinssaba 维护，渲染与购买流程交给 Wathe ShopApi。
        KinsWatheShopBootstrap.init();
        //初始化实体
        KinsWatheEntities.init();
        //初始化回放格式化器
        KinsWatheReplayFormatters.register();
    }
}
