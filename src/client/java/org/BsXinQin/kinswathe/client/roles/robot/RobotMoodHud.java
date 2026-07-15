package org.BsXinQin.kinswathe.client.roles.robot;

import dev.doctor4t.wathe.api.client.mood.MoodHudApi;
import dev.doctor4t.wathe.api.client.mood.MoodHudStyle;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWatheRoles;

public final class RobotMoodHud {
    private static final Identifier ROBOT_MOOD = Identifier.of("wathe", "hud/mood_happy");

    private RobotMoodHud() {
    }

    public static void register() {
        /*
         * 机器人虽然是 FAKE mood 类型，但视觉上复用平民开心图标和 HSV 心情条。
         * 这里直接通过公开样式表达，不再 mixin renderKiller。
         */
        MoodHudApi.registerRoleStyle(KinsWatheRoles.ROBOT, MoodHudStyle
                .builder(ROBOT_MOOD)
                .hsvMoodBar()
                .build());
    }
}
