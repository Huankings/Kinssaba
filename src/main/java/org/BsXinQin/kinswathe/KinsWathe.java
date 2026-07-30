package org.BsXinQin.kinswathe;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.host.combat.KinsWatheCombatBootstrap;
import org.BsXinQin.kinswathe.host.death.KinsWatheDeathBootstrap;

public class KinsWathe implements ModInitializer {

    public static String MOD_ID = "kinswathe";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        //初始化游戏设置
        KinsWatheGameSettings.init();
        // 接入 Wathe 公开死亡/枪械 API，替代旧的 GameFunctions/GunShootPayload 流程 mixin。
        KinsWatheDeathBootstrap.init();
        KinsWatheCombatBootstrap.init();
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
