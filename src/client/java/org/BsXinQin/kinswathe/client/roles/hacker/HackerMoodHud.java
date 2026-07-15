package org.BsXinQin.kinswathe.client.roles.hacker;

import dev.doctor4t.wathe.api.client.mood.MoodHudApi;
import dev.doctor4t.wathe.api.client.mood.MoodHudStyle;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;

public final class HackerMoodHud {
    private static final Identifier HACKER_MOOD = Identifier.of(KinsWathe.MOD_ID, "hud/mood_hacker");

    private HackerMoodHud() {
    }

    public static void register() {
        MoodHudApi.registerRoleStyle(KinsWatheRoles.HACKER, MoodHudStyle
                .builder(HACKER_MOOD)
                .barColor(KinsWatheRoles.HACKER.color())
                .build());
    }
}
