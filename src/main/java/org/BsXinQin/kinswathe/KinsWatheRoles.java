package org.BsXinQin.kinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.api.economy.EconomyApi;
import dev.doctor4t.wathe.api.task.TaskCompletionApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.packet.roles.BodymakerC2SPacket;
import org.BsXinQin.kinswathe.packet.roles.JudgeC2SPacket;
import org.BsXinQin.kinswathe.roles.bodymaker.BodymakerAbility;
import org.BsXinQin.kinswathe.roles.judge.JudgeAbility;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KinsWatheRoles {

    private static final HashMap<String, Role> ROLES = new HashMap<>();
    public static HashMap<String, Role> getRoles() {return ROLES;}
    private static final HashMap<String, Modifier> MODIFIERS = new HashMap<>();
    public static HashMap<String, Modifier> getModifiers() {return MODIFIERS;}

    /// 新增身份
    //造尸怪
    public static Role BODYMAKER = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID,"bodymaker"),
            0x2148d1,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
    ));
    //大法官
    public static Role JUDGE = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "judge"),
            0xECECF7,
            true,
            false,
            Role.MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
    ));
    //执照恶棍
    public static Role LICENSED_VILLAIN = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "licensed_villain"),
            0x404040,
            false,
            false,
            Role.MoodType.FAKE,
            WatheRoles.CIVILIAN.getMaxSprintTime() * 3 / 2,
            false
    ));
    //技术员
    public static Role TECHNICIAN = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "technician"),
            0x003366,
            true,
            false,
            Role.MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
    ));

    /// 新增词条
    //违禁者
    public static Modifier VIOLATOR = registerModifier(new Modifier(
            Identifier.of(KinsWathe.MOD_ID, "violator"),
            0x660000,
            null,
            null,
            false,
            false
    ));

    /// 注册方法
    //注册身份
    public static Role registerRole(Role role) {
        WatheRoles.registerRole(role);
        ROLES.put(role.identifier().getPath(), role);
        return role;
    }
    //注册词条
    public static Modifier registerModifier(Modifier modifier) {
        HMLModifiers.registerModifier(modifier);
        MODIFIERS.put(modifier.identifier().getPath(), modifier);
        return modifier;
    }

    /// 引入其他模组角色
    //引入NoellesRoles角色
    public static Role noellesrolesRoles(@NotNull String role) {
        try {
            Class<?> roleClass = Class.forName("org.agmas.noellesroles.Noellesroles");
            Field roleField = roleClass.getField(role);
            return (Role) roleField.get(null);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException ignored) {}
        return null;
    }
    public static boolean noellesrolesKillerSidedNeutrals(@NotNull Object role) {
        try {
            Class<?> noellesrolesClass = Class.forName("org.agmas.noellesroles.Noellesroles");
            Field field = noellesrolesClass.getDeclaredField("KILLER_SIDED_NEUTRALS");
            field.setAccessible(true);
            ArrayList<?> neutralList = (ArrayList<?>) field.get(null);
            return neutralList.contains(role);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException ignored) {}
        return false;
    }
    public static boolean isKillerSidedNeutral(@NotNull PlayerEntity player) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        return FabricLoader.getInstance().isModLoaded("noellesroles") && gameWorld.getRole(player) != null && (KinsWatheRoles.noellesrolesKillerSidedNeutrals(gameWorld.getRole(player)) || gameWorld.isRole(player, noellesrolesRoles("MIMIC")));
    }

    /// 新增阵营
    //新增阵营
    public static final ArrayList<Role> NEUTRAL_ROLES = new ArrayList<>();
    public static final ArrayList<Role> KILLER_NEUTRAL_ROLES = new ArrayList<>();
    public static void addNewRoleCamps() {
        addNeutralRoles();
        addKillerNeutralRoles();
    }
    //新增中立身份
    public static void addNeutralRoles() {
        NEUTRAL_ROLES.add(LICENSED_VILLAIN);
    }
    //新增杀手方中立身份
    public static void addKillerNeutralRoles() {
        // 当前 kinssaba 侧没有需要单独加入杀手侧中立池的职业。
    }

    /// 限制身份生成人数
    public static void limitRolesGeneratePlayers() {
        ServerTickEvents.END_SERVER_TICK.register(((server) -> {
            //限制执照恶棍生成人数
            if (server.getPlayerManager().getCurrentPlayerCount() >= KinsWatheConfig.HANDLER.instance().LicensedVillainPlayerLimit) {
                Harpymodloader.setRoleMaximum(LICENSED_VILLAIN,1);} else {
                Harpymodloader.setRoleMaximum(LICENSED_VILLAIN,0);
            }
        }));
    }

    /// 设置初始事件
    public static void setDefaultEvents() {
        ModdedRoleAssigned.EVENT.register((player, role)->{
            AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
            ability.cooldown = KinsWatheConfig.HANDLER.instance().StartingCooldown * 20;
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
            PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
            //阵营初始收入
            if (KinsWatheConfig.HANDLER.instance().EnableWatheModify) {
                if (gameWorld.isInnocent(player)) playerShop.addToBalance(KinsWatheConfig.HANDLER.instance().InitialCivilianIncome);
                if (!gameWorld.isInnocent(player) && !gameWorld.canUseKillerFeatures(player)) playerShop.addToBalance(KinsWatheConfig.HANDLER.instance().InitialNeutralIncome);
                if (gameWorld.canUseKillerFeatures(player)) playerShop.addToBalance(KinsWatheConfig.HANDLER.instance().InitialKillerIncome - 100);
            }
            //执照恶棍初始物品
            if (role.equals(LICENSED_VILLAIN)) {
                player.giveItemStack(WatheItems.LOCKPICK.getDefaultStack());
            }
        });
    }

    /// 注册身份技能
    public static void registerRolesAbility() {
        ServerPlayNetworking.registerGlobalReceiver(BodymakerC2SPacket.ID, (payload, context) -> {
            BodymakerAbility.register(payload, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(JudgeC2SPacket.ID, (payload, context) -> {
            JudgeAbility.register(payload, context.player());
        });
    }

    /// 限制词条自动启用配置
    public static void limitModifiersGenerateConfig() {
        if (!KinsWatheConfig.HANDLER.instance().ViolatorEnabled) {
            HarpyModLoaderConfig.HANDLER.load();
            //限制违禁者自动启用配置
            if (!HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.contains(Identifier.of(KinsWathe.MOD_ID, "violator").toString())) {
                HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.add(Identifier.of(KinsWathe.MOD_ID, "violator").toString());
            }
            HarpyModLoaderConfig.HANDLER.save();
        }
    }

    private static void registerEconomyApi() {
        /*
         * 金币 HUD：迁移旧 IncomeIconMixin 的角色名单。
         * 杀手能力角色由 Wathe 默认显示金币 HUD，因此这里只注册非默认但仍使用金币经济的 kinssaba 角色。
         */
        EconomyApi.registerBalanceHudRoles(List.of(
                JUDGE,
                LICENSED_VILLAIN,
                TECHNICIAN
        ));

        /*
         * 通用被动收入：迁移旧 PassiveIncomeMixin 的额外角色。
         * BODYMAKER 等杀手能力角色不需要注册，
         * 因为 Wathe 的 EconomyApi 会保留“杀手能力角色默认拥有被动收入”的原行为。
         */
        EconomyApi.registerPassiveIncomeRoles(List.of(
                JUDGE
        ));

        /*
         * 任务金币统一在这里结算：
         * kinssaba 自己明确有任务收入的角色每个任务 50 金币。
         * Taskmaster 已迁移到 NoellesRoles，kinssaba 不再在这里追加词条收益。
         */
        TaskCompletionApi.registerTaskIncomeProvider(
                KinsWathe.id("task_income"),
                TaskCompletionApi.DEFAULT_PRIORITY,
                context -> {
                    int income = 0;
                    Role role = context.role();
                    if (hasBaseTaskIncome(role)) {
                        income += 50;
                    }

                    return income;
                }
        );
    }

    private static boolean hasBaseTaskIncome(Role role) {
        return role == JUDGE
                || role == LICENSED_VILLAIN
                || role == TECHNICIAN;
    }

    /// 初始化方法
    public static void init() {
        //新增阵营
        addNewRoleCamps();
        //注册 Wathe 公开经济接口
        registerEconomyApi();
        //限制身份生成人数
        limitRolesGeneratePlayers();
        // 中立结算板块已经迁移到 Wathe 本体，KinsWathe 不再注册第二套中立公告文本。
        //设置初始事件
        setDefaultEvents();
        //注册身份技能
        registerRolesAbility();
        //限制词条自动启用配置
        limitModifiersGenerateConfig();
    }
}
