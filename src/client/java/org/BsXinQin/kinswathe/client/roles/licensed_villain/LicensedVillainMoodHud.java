package org.BsXinQin.kinswathe.client.roles.licensed_villain;

import dev.doctor4t.wathe.api.client.mood.MoodHudApi;
import dev.doctor4t.wathe.api.client.mood.MoodHudStyle;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;

public final class LicensedVillainMoodHud {
    private static final Identifier LICENSED_VILLAIN_MOOD = Identifier.of(KinsWathe.MOD_ID, "hud/mood_licensed_villain");

    private LicensedVillainMoodHud() {
    }

    public static void register() {
        MoodHudApi.registerRoleStyle(KinsWatheRoles.LICENSED_VILLAIN, MoodHudStyle
                .builder(LICENSED_VILLAIN_MOOD)
                .barColor(KinsWatheRoles.LICENSED_VILLAIN.color())
                .build());
    }
}
