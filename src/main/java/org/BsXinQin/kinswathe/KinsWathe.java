package org.BsXinQin.kinswathe;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

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
        //初始化物品
        KinsWatheItems.init();
        // 初始化职业商店：商品列表由 kinssaba 维护，渲染与购买流程交给 Wathe ShopApi。
        KinsWatheShopBootstrap.init();
        //初始化实体
        KinsWatheEntities.init();
    }
}
