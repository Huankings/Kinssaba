package org.BsXinQin.kinswathe.client.roles.dreamer;

import dev.doctor4t.wathe.api.client.mood.MoodHudApi;
import dev.doctor4t.wathe.api.client.mood.MoodHudContext;
import dev.doctor4t.wathe.api.client.mood.MoodHudStyle;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;

public final class DreamerMoodHud {
    private static final Identifier DREAMER_MOOD = Identifier.of(KinsWathe.MOD_ID, "hud/mood_dreamer");

    private DreamerMoodHud() {
    }

    public static void register() {
        MoodHudApi.registerRoleStyle(KinsWatheRoles.DREAMER, MoodHudStyle
                .builder(DREAMER_MOOD)
                .bar(DreamerMoodHud::renderRainbowBar)
                .build());
    }

    private static void renderRainbowBar(MoodHudContext context, int width, float alpha) {
        if (width <= 0 || alpha <= 0.0F) {
            return;
        }

        /*
         * 梦者沿用原来的彩虹心情条：颜色随真实时间循环，
         * 但坐标和透明度由 Wathe 的公共 HUD 状态统一控制。
         */
        float rainbowTime = (System.currentTimeMillis() % 6000) / 6000.0F;
        int rainbowColor = MathHelper.hsvToRgb(rainbowTime, 1.0F, 1.0F);
        context.drawContext().fill(0, 0, width, 1, rainbowColor | ((int) (alpha * 255.0F) << 24));
    }
}
