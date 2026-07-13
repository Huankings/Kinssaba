package org.BsXinQin.kinswathe.client.instinct;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.roles.cook.CookComponent;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.BsXinQin.kinswathe.roles.technician.TechnicianComponent;
import org.agmas.noellesroles.framing.DelusionPlayerComponent;

import java.awt.Color;
import java.util.UUID;

public final class KinsWatheInstinctHandlers {
    private static final int PRIORITY_INSTINCT_COLOR = 100;
    private static final int PRIORITY_ABILITY_MARK = 100;
    private static final UUID DELUSION_MARKER = UUID.fromString("00000000-0000-0000-dead-c0de00000000");

    private KinsWatheInstinctHandlers() {
    }

    public static void register() {
        registerAvailability();
        registerKillerNeutralColors();
        registerRoleInstinctColors();
        registerAbilityMarks();
        registerNoellesIntegrationMarks();
    }

    private static void registerAvailability() {
        InstinctApi.registerAvailability(KinsWathe.id("instinct/auto_psycho"), InstinctApi.DEFAULT_PRIORITY, viewer -> {
            ConfigWorldComponent config = ConfigWorldComponent.KEY.get(viewer.getWorld());
            PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(viewer);
            if (config.EnableAutoPsychoInstinct
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && psycho.psychoTicks > 0) {
                /*
                 * 自动疯狂本能不是本能键触发，但旧逻辑会直接让 WatheClient.isInstinctEnabled() 为 true。
                 * 现在也保留为 availability ENABLE；如果 StupidExpress 的 Convener 压制存在，
                 * 它的高优先级 DISABLE 会先执行并压住这里。
                 */
                return InstinctApi.AvailabilityResult.ENABLE;
            }
            return InstinctApi.AvailabilityResult.PASS;
        });

        InstinctApi.registerAvailability(KinsWathe.id("instinct/role_instinct"), InstinctApi.DEFAULT_PRIORITY, viewer -> {
            if (!WatheClient.isInstinctInputActive()) {
                return InstinctApi.AvailabilityResult.PASS;
            }
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(viewer.getWorld());
            return gameWorld.isRole(viewer, KinsWatheRoles.DREAMER)
                    || gameWorld.isRole(viewer, KinsWatheRoles.HACKER)
                    || gameWorld.isRole(viewer, KinsWatheRoles.LICENSED_VILLAIN)
                    ? InstinctApi.AvailabilityResult.ENABLE
                    : InstinctApi.AvailabilityResult.PASS;
        });
    }

    private static void registerKillerNeutralColors() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/killer_neutral_colors"), PRIORITY_INSTINCT_COLOR, (viewer, target) -> {
            if (!(target instanceof PlayerEntity targetPlayer)
                    || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    || !WatheClient.isInstinctEnabled()
                    || !WatheClient.isKiller()
                    || !WatheClient.isPlayerAliveAndInSurvival()) {
                return InstinctApi.HighlightResult.pass();
            }

            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(viewer.getWorld());
            Role role = gameWorld.getRole(targetPlayer);
            if (role == null) {
                return InstinctApi.HighlightResult.pass();
            }
            if (KinsWatheRoles.NEUTRAL_ROLES.contains(role)) {
                return InstinctApi.HighlightResult.color(0x4EDD35);
            }
            if (KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(role)) {
                return InstinctApi.HighlightResult.color(role.color());
            }
            return InstinctApi.HighlightResult.pass();
        });
    }

    private static void registerRoleInstinctColors() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/dreamer_targets"), PRIORITY_INSTINCT_COLOR, (viewer, target) -> {
            if (target instanceof PlayerEntity targetPlayer
                    && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.DREAMER)
                    && WatheClient.isInstinctEnabled()) {
                return InstinctApi.HighlightResult.color(KinsWatheRoles.DREAMER.color());
            }
            return InstinctApi.HighlightResult.pass();
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/hacker_targets"), PRIORITY_INSTINCT_COLOR, (viewer, target) -> {
            if (!(target instanceof PlayerEntity targetPlayer)
                    || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    || !GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.HACKER)
                    || !WatheClient.isInstinctEnabled()) {
                return InstinctApi.HighlightResult.pass();
            }

            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(viewer.getWorld());
            Role targetRole = gameWorld.getRole(targetPlayer);
            if (targetRole == null) {
                return InstinctApi.HighlightResult.pass();
            }

            HackerComponent targetHack = HackerComponent.KEY.get(targetPlayer);
            Role mimic = FabricLoader.getInstance().isModLoaded("noellesroles") ? KinsWatheRoles.noellesrolesRoles("MIMIC") : null;
            if (gameWorld.canUseKillerFeatures(targetPlayer) || (mimic != null && gameWorld.isRole(targetPlayer, mimic))) {
                return InstinctApi.HighlightResult.color(MathHelper.hsvToRgb(0.0F, 1.0F, 0.6F));
            }
            if (KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(targetRole) || KinsWatheRoles.isKillerSidedNeutral(targetPlayer)) {
                return InstinctApi.HighlightResult.color(targetRole.color());
            }
            if (targetHack.hackingTime >= ConfigWorldComponent.KEY.get(viewer.getWorld()).HackerHackingTime * 20) {
                return InstinctApi.HighlightResult.color(Color.GREEN.getRGB());
            }
            return InstinctApi.HighlightResult.color(KinsWatheRoles.HACKER.color());
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/licensed_villain_targets"), PRIORITY_INSTINCT_COLOR, (viewer, target) -> {
            if (target instanceof PlayerEntity targetPlayer
                    && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.LICENSED_VILLAIN)
                    && WatheClient.isInstinctEnabled()) {
                return InstinctApi.HighlightResult.color(KinsWatheRoles.LICENSED_VILLAIN.color());
            }
            return InstinctApi.HighlightResult.pass();
        });
    }

    private static void registerAbilityMarks() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/dream_imprint"), PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (!(target instanceof PlayerEntity targetPlayer) || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                return InstinctApi.HighlightResult.pass();
            }

            DreamerComponent targetDream = DreamerComponent.KEY.get(targetPlayer);
            if (targetDream.dreamerUUID == null || targetDream.dreamArmor <= 0) {
                return InstinctApi.HighlightResult.pass();
            }

            PlayerEntity dreamer = targetPlayer.getWorld().getPlayerByUuid(targetDream.dreamerUUID);
            boolean viewerIsDreamer = viewer == dreamer && WatheClient.isPlayerAliveAndInSurvival();
            if ((viewerIsDreamer && !WatheClient.isKiller())
                    || (viewerIsDreamer && WatheClient.isKiller() && !WatheClient.isInstinctEnabled())) {
                return InstinctApi.HighlightResult.color(KinsWatheRoles.DREAMER.color());
            }
            return InstinctApi.HighlightResult.pass();
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/cook_marks"), PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (target instanceof PlayerEntity targetPlayer
                    && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.COOK)
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && CookComponent.KEY.get(targetPlayer).eatTicks > 0) {
                return InstinctApi.HighlightResult.color(Color.GREEN.getRGB());
            }
            return InstinctApi.HighlightResult.pass();
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/drugmaker_marks"), PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (!(target instanceof PlayerEntity targetPlayer) || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                return InstinctApi.HighlightResult.pass();
            }
            PlayerPoisonComponent targetPoison = PlayerPoisonComponent.KEY.get(targetPlayer);
            if (GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.DRUGMAKER)
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && !WatheClient.isInstinctEnabled()
                    && targetPoison.poisonTicks > 0
                    && !(targetPoison.poisoner != null && targetPoison.poisoner.equals(DELUSION_MARKER))) {
                return InstinctApi.HighlightResult.color(KinsWatheRoles.DRUGMAKER.color());
            }
            return InstinctApi.HighlightResult.pass();
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/physician_marks"), PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (!(target instanceof PlayerEntity targetPlayer) || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                return InstinctApi.HighlightResult.pass();
            }
            PlayerPoisonComponent targetPoison = PlayerPoisonComponent.KEY.get(targetPlayer);
            DelusionPlayerComponent targetDelusion = DelusionPlayerComponent.KEY.get(targetPlayer);
            if (GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.PHYSICIAN)
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && (targetPoison.poisonTicks > 0 || targetDelusion.isActive())) {
                return InstinctApi.HighlightResult.color(Color.RED.getRGB());
            }
            return InstinctApi.HighlightResult.pass();
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/technician_marks"), PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (target instanceof PlayerEntity targetPlayer
                    && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.TECHNICIAN)
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && TechnicianComponent.KEY.get(targetPlayer).technicianTicks > 0) {
                return InstinctApi.HighlightResult.color(KinsWatheRoles.TECHNICIAN.color());
            }
            return InstinctApi.HighlightResult.pass();
        });
    }

    private static void registerNoellesIntegrationMarks() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/noelles_conductor_items"), PRIORITY_ABILITY_MARK, (viewer, target) -> {
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
                return InstinctApi.HighlightResult.color(0xDB9D00);
            }
            return InstinctApi.HighlightResult.pass();
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/noelles_coroner_bodies"), PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (!FabricLoader.getInstance().isModLoaded("noellesroles")
                    || !(target instanceof PlayerBodyEntity)
                    || !ConfigWorldComponent.KEY.get(viewer.getWorld()).EnableNoellesRolesModify
                    || !ConfigWorldComponent.KEY.get(viewer.getWorld()).CoronerInstinctModify
                    || WatheClient.moodComponent == null) {
                return InstinctApi.HighlightResult.pass();
            }
            Role coroner = KinsWatheRoles.noellesrolesRoles("CORONER");
            PlayerMoodComponent playerMood = PlayerMoodComponent.KEY.get(viewer);
            if (coroner != null
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, coroner)
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && playerMood != null
                    && !playerMood.isLowerThanMid()) {
                return InstinctApi.HighlightResult.color(0x606060);
            }
            return InstinctApi.HighlightResult.pass();
        });
    }
}
