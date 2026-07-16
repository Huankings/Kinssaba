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
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.packet.host.AbilityC2SPacket;
import org.BsXinQin.kinswathe.packet.roles.BodymakerC2SPacket;
import org.BsXinQin.kinswathe.packet.roles.JudgeC2SPacket;
import org.BsXinQin.kinswathe.roles.bellringer.BellringerAbility;
import org.BsXinQin.kinswathe.roles.bodymaker.BodymakerAbility;
import org.BsXinQin.kinswathe.roles.cleaner.CleanerAbility;
import org.BsXinQin.kinswathe.roles.detective.DetectiveAbility;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerPhoneComponent;
import org.BsXinQin.kinswathe.roles.hunter.HunterAbility;
import org.BsXinQin.kinswathe.roles.judge.JudgeAbility;
import org.BsXinQin.kinswathe.roles.robot.RobotAbility;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
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
    //敲钟人
    public static Role BELLRINGER = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "bellringer"),
            0x66B2FF,
            true,
            false,
            Role.MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            true
    ));
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
    //清道夫
    public static Role CLEANER = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "cleaner"),
            0x16582C,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
    ));
    //厨师
    public static Role COOK = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "cook"),
            0xCCFF99,
            true,
            false,
            Role.MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
    ));
    //侦探
    public static Role DETECTIVE = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "detective"),
            0xFFFFCC,
            true,
            false,
            Role.MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
    ));
    //梦者
    public static Role DREAMER = registerNoellesRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "dreamer"),
            0xE5CCFF,
            false,
            false,
            Role.MoodType.FAKE,
            -1,
            true
    ));
    //制毒师
    public static Role DRUGMAKER = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "drugmaker"),
            0x4C0099,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
    ));
    //黑客
    public static Role HACKER = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "hacker"),
            0x808080,
            false,
            false,
            Role.MoodType.FAKE,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            true
    ));
    //追猎者
    public static Role HUNTER = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "hunter"),
            0x663300,
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
    //绑匪
    public static Role KIDNAPPER = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "kidnapper"),
            0xCC0066,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
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
    //医师
    public static Role PHYSICIAN = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "physician"),
            0xFFE5CC,
            true,
            false,
            Role.MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime() * 3 / 2,
            false
    ));
    //机器人
    public static Role ROBOT = registerRole(new Role(
            Identifier.of(KinsWathe.MOD_ID, "robot"),
            0xC0C0C0,
            true,
            false,
            Role.MoodType.FAKE,
            -1,
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
    //富豪
    public static Modifier MAGNATE = registerModifier(new Modifier(
            Identifier.of(KinsWathe.MOD_ID, "magnate"),
            0xFFFF00,
            null,
            null,
            false,
            false
    ).setEligibilityPredicate((gameWorld, player, modifier) -> {
        /*
         * 富豪只应该生成在拥有通用被动收入的角色身上。
         * 这里不再维护手写职业名单，而是直接询问 Wathe 的被动收入 API；
         * 这样后续其他扩展只要注册了被动收入，富豪就能自动识别。
         */
        if (!(player instanceof ServerPlayerEntity serverPlayer) || !(player.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }
        return EconomyApi.canReceivePassiveIncome(serverWorld, gameWorld, serverPlayer);
    }));
    //任务大师
    public static Modifier TASKMASTER = registerModifier(new Modifier(
            Identifier.of(KinsWathe.MOD_ID, "taskmaster"),
            0xFF3399,
            null,
            null,
            false,
            false
    ).setEligibilityPredicate((gameWorld, player, modifier) -> {
        /*
         * 任务大师按照你的确认：只根据“这个角色是否显示金币 HUD”来决定能否生成。
         * 这样任何扩展职业只要通过 EconomyApi 注册了金币 HUD，就自然可以获得任务大师。
         */
        return EconomyApi.shouldRenderBalanceHud(gameWorld, player);
    }));
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
    public static Role registerNoellesRole(Role role) {
        if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
            WatheRoles.registerRole(role);
            ROLES.put(role.identifier().getPath(), role);
        }
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
        KILLER_NEUTRAL_ROLES.add(HACKER);
        if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
            KILLER_NEUTRAL_ROLES.add(DREAMER);
            Harpymodloader.setRoleMaximum(DREAMER, 1);
        }
    }

    /// 限制身份生成人数
    public static void limitRolesGeneratePlayers() {
        ServerTickEvents.END_SERVER_TICK.register(((server) -> {
            //限制制毒师生成人数
            if (server.getPlayerManager().getCurrentPlayerCount() >= KinsWatheConfig.HANDLER.instance().DrugmakerPlayerLimit) {
                Harpymodloader.setRoleMaximum(DRUGMAKER,1);} else {
                Harpymodloader.setRoleMaximum(DRUGMAKER,0);
            }
            //限制黑客生成人数
            if (server.getPlayerManager().getCurrentPlayerCount() >= KinsWatheConfig.HANDLER.instance().HackerPlayerLimit) {
                Harpymodloader.setRoleMaximum(HACKER,1);} else {
                Harpymodloader.setRoleMaximum(HACKER,0);
            }
            //限制执照恶棍生成人数
            if (server.getPlayerManager().getCurrentPlayerCount() >= KinsWatheConfig.HANDLER.instance().LicensedVillainPlayerLimit) {
                Harpymodloader.setRoleMaximum(LICENSED_VILLAIN,1);} else {
                Harpymodloader.setRoleMaximum(LICENSED_VILLAIN,0);
            }
        }));
    }

    /// 限制冲突身份同时生成
    public static boolean conflictRolesGenerate(@NotNull Role role1, @NotNull Role role2) {
        if (!FabricLoader.getInstance().isModLoaded("noellesroles")) return false;
        if (KinsWatheConfig.HANDLER.instance().HackerGenerateWithMimic) return false;
        return (role1 == HACKER && role2 == noellesrolesRoles("MIMIC")) || (role1 == noellesrolesRoles("MIMIC") && role2 == HACKER);
    }

    /// 设置初始事件
    public static void setDefaultEvents() {
        ModdedRoleAssigned.EVENT.register((player, role)->{
            AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
            ability.cooldown = KinsWatheConfig.HANDLER.instance().StartingCooldown * 20;
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
            PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
            DreamerKillerComponent playerDreamer = DreamerKillerComponent.KEY.get(player);
            HackerPhoneComponent phone = HackerPhoneComponent.KEY.get(player);
            //阵营初始收入
            if (KinsWatheConfig.HANDLER.instance().EnableWatheModify) {
                if (gameWorld.isInnocent(player)) playerShop.addToBalance(KinsWatheConfig.HANDLER.instance().InitialCivilianIncome);
                if (!gameWorld.isInnocent(player) && !gameWorld.canUseKillerFeatures(player)) playerShop.addToBalance(KinsWatheConfig.HANDLER.instance().InitialNeutralIncome);
                if (gameWorld.canUseKillerFeatures(player)) playerShop.addToBalance(KinsWatheConfig.HANDLER.instance().InitialKillerIncome - 100);
            }
            //清道夫初始物品
            if (role.equals(CLEANER)) {
                player.giveItemStack(KinsWatheItems.SULFURIC_ACID_BARREL.getDefaultStack());
            }
            //梦者初始物品
            if (role.equals(DREAMER)) {
                player.giveItemStack(new ItemStack(KinsWatheItems.DREAM_IMPRINT, KinsWatheConfig.HANDLER.instance().DreamerInitialItemQuantity));
                playerDreamer.setDreamerRequired();
            }
            //黑客初始物品
            if (role.equals(HACKER)) {
                player.giveItemStack(phone.getPhone());
                for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                    if (serverPlayer == null) continue;
                    if (gameWorld.canUseKillerFeatures(serverPlayer)) {
                        serverPlayer.giveItemStack(phone.getPhone());
                    }
                }
            }
            //绑匪初始物品
            if (role.equals(KIDNAPPER)) {
                player.giveItemStack(KinsWatheItems.KNOCKOUT_DRUG.getDefaultStack());
            }
            //执照恶棍初始物品
            if (role.equals(LICENSED_VILLAIN)) {
                player.giveItemStack(WatheItems.LOCKPICK.getDefaultStack());
            }
            //医师初始物品
            if (role.equals(PHYSICIAN)) {
                player.giveItemStack(KinsWatheItems.MEDICAL_KIT.getDefaultStack());
            }
        });
    }

    /// 注册身份技能
    public static void registerRolesAbility() {
        ServerPlayNetworking.registerGlobalReceiver(AbilityC2SPacket.ID, (payload, context) -> {
            BellringerAbility.register(context.player());
            CleanerAbility.register(context.player());
            DetectiveAbility.register(context.player());
            HunterAbility.register(context.player());
            RobotAbility.register(context.player());
        });
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
                BELLRINGER,
                COOK,
                DETECTIVE,
                DREAMER,
                JUDGE,
                LICENSED_VILLAIN,
                PHYSICIAN,
                TECHNICIAN
        ));
        EconomyApi.registerBalanceHudPredicate(
                KinsWathe.id("balance_hud/hacker"),
                EconomyApi.DEFAULT_PRIORITY,
                (gameWorld, player, role) -> KinsWatheConfig.HANDLER.instance().HackerHasShop && role == HACKER
        );

        /*
         * 通用被动收入：迁移旧 PassiveIncomeMixin 的额外角色。
         * BODYMAKER/CLEANER/DRUGMAKER/HUNTER/KIDNAPPER 等杀手能力角色不需要注册，
         * 因为 Wathe 的 EconomyApi 会保留“杀手能力角色默认拥有被动收入”的原行为。
         */
        EconomyApi.registerPassiveIncomeRoles(List.of(
                COOK,
                DREAMER,
                JUDGE
        ));
        EconomyApi.registerPassiveIncomeRule(
                KinsWathe.id("passive_income/hacker"),
                EconomyApi.DEFAULT_PRIORITY,
                context -> KinsWatheConfig.HANDLER.instance().HackerHasShop && context.role() == HACKER
                        ? EconomyApi.PassiveIncomeDecision.ALLOW
                        : EconomyApi.PassiveIncomeDecision.PASS
        );

        /*
         * 任务金币统一在这里结算：
         * 1. kinssaba 自己明确有任务收入的角色每个任务 50 金币；
         * 2. Hacker 只有开启商店配置时才有任务收入；
         * 3. Taskmaster 按你的确认，只要玩家当前角色拥有金币 HUD，就按阵营语义追加任务金币。
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
                    if (KinsWatheConfig.HANDLER.instance().HackerHasShop && role == HACKER) {
                        income += 50;
                    }

                    WorldModifierComponent modifier = WorldModifierComponent.KEY.get(context.player().getWorld());
                    if (modifier.isModifier(context.player(), TASKMASTER)
                            && EconomyApi.shouldRenderBalanceHud(context.gameWorld(), context.player())) {
                        income += role != null && role.canUseKiller() ? 50 : 25;
                    }
                    return income;
                }
        );

        /*
         * Magnate 的“双倍被动收入”现在作为 Wathe 被动收入数值修改器实现。
         * 它只把本次基础收入再补一份；最终加钱前仍由 Wathe 统一套用阵营金币上限，
         * 因此不会因为双倍效果突破上限。
         */
        EconomyApi.registerPassiveIncomeModifier(
                KinsWathe.id("magnate_double_passive_income"),
                EconomyApi.DEFAULT_PRIORITY,
                (context, currentIncome) -> {
                    WorldModifierComponent modifier = WorldModifierComponent.KEY.get(context.world());
                    return modifier.isModifier(context.player(), MAGNATE)
                            ? currentIncome + context.baseIncome()
                            : currentIncome;
                }
        );
    }

    private static boolean hasBaseTaskIncome(Role role) {
        return role == BELLRINGER
                || role == COOK
                || role == DETECTIVE
                || role == JUDGE
                || role == LICENSED_VILLAIN
                || role == PHYSICIAN
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
