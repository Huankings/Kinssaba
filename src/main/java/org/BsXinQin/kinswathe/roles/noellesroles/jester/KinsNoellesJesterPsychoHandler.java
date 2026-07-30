package org.BsXinQin.kinswathe.roles.noellesroles.jester;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.api.psycho.PsychoModeApi;
import dev.doctor4t.wathe.api.psycho.PsychoModeProfile;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.jetbrains.annotations.Nullable;

/**
 * kinssaba 对 NoellesRoles 狂信者疯魔的兼容接入。
 *
 * <p>旧版通过 mixin 直接拦 {@code GameFunctions.killPlayer} 并手写
 * {@code psychoTicks/armour}。现在把“中立击杀狂信者触发 45 秒、0 护盾疯魔”
 * 收口成独立 profile，再通过 Wathe 的疯魔 API 启动；这样皮肤、物品、锁栏、
 * 背景音和结束回放都由 Wathe 统一维护。</p>
 */
public final class KinsNoellesJesterPsychoHandler {
    public static final Identifier PROFILE_ID = KinsWathe.id("noelles_jester_psycho");
    public static final Identifier NOELLES_JESTER_PROFILE_ID = Identifier.of("noellesroles", "jester_psycho");
    private static final int DURATION_TICKS = GameConstants.getInTicks(0, 45);
    private static boolean initialized = false;

    private KinsNoellesJesterPsychoHandler() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        PsychoModeProfile profile = PsychoModeProfile.copyOf(PsychoModeApi.createDefaultProfile(), PROFILE_ID)
                .nameTranslationKey(PsychoModeApi.DEFAULT_MODE_NAME_TRANSLATION_KEY)
                .durationTicks(DURATION_TICKS)
                .armour(0)
                .build();
        PsychoModeApi.registerProfile(profile);

        AllowPlayerDeath.EVENT.register(KinsNoellesJesterPsychoHandler::allowDeath);
    }

    public static boolean isJesterPsycho(@Nullable PlayerEntity player) {
        /*
         * NoellesRoles 自己会注册 noellesroles:jester_psycho；
         * kinssaba 兼容规则会注册 kinswathe:noelles_jester_psycho。
         * 两者都属于“狂信者疯魔”，后续判断是否禁止其攻击杀手时应一起识别。
         */
        return PsychoModeApi.isActive(player, PROFILE_ID)
                || PsychoModeApi.isActive(player, NOELLES_JESTER_PROFILE_ID);
    }

    private static boolean allowDeath(PlayerEntity victim, PlayerEntity killer, Identifier deathReason) {
        if (!FabricLoader.getInstance().isModLoaded("noellesroles")
                || !KinsWatheConfig.HANDLER.instance().EnableNoellesRolesModify
                || !tryTriggerFromDeath(victim, killer)) {
            return true;
        }
        return false;
    }

    private static boolean tryTriggerFromDeath(PlayerEntity victim, @Nullable PlayerEntity killer) {
        if (killer == null || PsychoModeApi.isActive(victim)) {
            return false;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.getWorld());
        Role jester = KinsWatheRoles.noellesrolesRoles("JESTER");
        Role killerRole = gameWorld.getRole(killer);
        if (jester == null || killerRole == null) {
            return false;
        }

        /*
         * 这里只保留 kinssaba 原 mixin 的“中立击杀 Noelles 狂信者”语义。
         * NoellesRoles 自己的“无辜者击杀狂信者”已由 Noelles 的职业 handler 接入 API，
         * 两边按触发来源拆开，避免以后继续在一个大类里堆规则。
         */
        if (!gameWorld.isRole(victim, jester) || !KinsWatheRoles.NEUTRAL_ROLES.contains(killerRole)) {
            return false;
        }

        return PsychoModeApi.start(victim, PROFILE_ID);
    }
}
