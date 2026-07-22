package org.BsXinQin.kinswathe.client.instinct;

import org.BsXinQin.kinswathe.client.instinct.host.AutoPsychoInstinctHandler;
import org.BsXinQin.kinswathe.client.instinct.host.KillerNeutralInstinctHandler;
import org.BsXinQin.kinswathe.client.instinct.noellesroles.ConductorInstinctHandler;
import org.BsXinQin.kinswathe.client.instinct.noellesroles.CoronerInstinctHandler;
import org.BsXinQin.kinswathe.client.instinct.roles.licensed_villain.LicensedVillainInstinctHandler;
import org.BsXinQin.kinswathe.client.instinct.roles.technician.TechnicianInstinctHandler;

public final class KinsWatheInstinctHandlers {
    public static final int PRIORITY_INSTINCT_COLOR = 100;
    public static final int PRIORITY_ABILITY_MARK = 100;

    private KinsWatheInstinctHandlers() {
    }

    public static void register() {
        AutoPsychoInstinctHandler.register();
        KillerNeutralInstinctHandler.register();
        LicensedVillainInstinctHandler.register();
        TechnicianInstinctHandler.register();
        ConductorInstinctHandler.register();
        CoronerInstinctHandler.register();
    }
}
