package org.BsXinQin.kinswathe.client.instinct.noellesroles;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.ItemEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;

public final class ConductorInstinctHandler {
    private ConductorInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/noelles_conductor_items"), KinsWatheInstinctHandlers.PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (!FabricLoader.getInstance().isModLoaded("noellesroles")
                    || !(target instanceof ItemEntity)
                    || !ConfigWorldComponent.KEY.get(viewer.getWorld()).EnableNoellesRolesModify
                    || !ConfigWorldComponent.KEY.get(viewer.getWorld()).ConductorInstinctModify) {
                return InstinctApi.HighlightResult.pass();
            }

            Role conductor = KinsWatheRoles.noellesrolesRoles("CONDUCTOR");
            if (conductor != null
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, conductor)
                    && WatheClient.isPlayerAliveAndInSurvival()) {
                /*
                 * NoellesRoles 乘务员联动：允许存活乘务员看到物品黄框。
                 * 这是 KinsWathe 配置控制的联动提示，不需要本能键。
                 */
                return InstinctApi.HighlightResult.color(0xDB9D00);
            }
            return InstinctApi.HighlightResult.pass();
        });
    }
}
