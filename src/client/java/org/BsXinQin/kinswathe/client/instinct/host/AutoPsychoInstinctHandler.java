package org.BsXinQin.kinswathe.client.instinct.host;

import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.client.WatheClient;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;

public final class AutoPsychoInstinctHandler {
    private AutoPsychoInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerAvailability(KinsWathe.id("instinct/auto_psycho"), InstinctApi.DEFAULT_PRIORITY, viewer -> {
            ConfigWorldComponent config = ConfigWorldComponent.KEY.get(viewer.getWorld());
            PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(viewer);
            if (config.EnableAutoPsychoInstinct
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && psycho.psychoTicks > 0) {
                /*
                 * 自动疯狂本能不是本能键触发，而是精神狂暴状态直接给予本能资格。
                 * 仍注册在 availability 层，这样 StupidExpress Convener 之类的高优先级 DISABLE
                 * 可以统一压住所有“依赖本能开启”的显示。
                 */
                return InstinctApi.AvailabilityResult.ENABLE;
            }
            return InstinctApi.AvailabilityResult.PASS;
        });
    }
}
