package org.BsXinQin.kinswathe.client.role_name;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.client.gui.RoleNameHudApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.KinsWatheInitializeClient;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.BodyDeathReasonComponent;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * kinssaba 接入 Wathe RoleName HUD API 的统一注册处。
 *
 * <p>这里替代旧的 RoleNameRenderer mixin：
 * HackerTargetHudMixin、DetectiveTargetHudMixin、PhysicianDeathReasonMixin、
 * KillerSidedTextMixin、KillerCohortHudMixin。</p>
 */
public final class KinsRoleNameHudHandlers {
    private KinsRoleNameHudHandlers() {
    }

    public static void register() {
        registerKillerSidedCohortRules();
        registerHackerTargetHud();
        registerDetectiveTargetHud();
        registerPhysicianBodyHud();
    }

    private static void registerKillerSidedCohortRules() {
        RoleNameHudApi.registerCohortState(
                KinsWathe.id("role_name/killer_sided_cohorts"),
                RoleNameHudApi.DEFAULT_PRIORITY,
                (viewer, subject, vanillaValue) -> {
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(subject.getWorld());
                    return countsAsKillerCohort(gameWorld, subject) ? true : null;
                }
        );
    }

    private static void registerHackerTargetHud() {
        RoleNameHudApi.registerExtraHud(
                KinsWathe.id("role_name/hacker_target"),
                RoleNameHudApi.DEFAULT_PRIORITY,
                context -> {
                    ClientPlayerEntity player = context.player();
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
                    if (!gameWorld.isRole(player, KinsWatheRoles.HACKER) || !GameFunctions.isPlayerAliveAndSurvival(player)) {
                        return;
                    }

                    PlayerEntity target = aliveTarget(context.targetPlayer());
                    if (target == null || gameWorld.getRole(target) == null || countsAsKillerCohort(gameWorld, target)) {
                        return;
                    }

                    Text targetInfo;
                    GameSafeComponent gameSafe = GameSafeComponent.KEY.get(player.getWorld());
                    if (gameSafe.isSafe()) {
                        targetInfo = Text.translatable("hud.kinswathe.hacker.target_safe").withColor(KinsWatheRoles.HACKER.color());
                    } else {
                        HackerComponent targetHack = HackerComponent.KEY.get(target);
                        int requiredTicks = ConfigWorldComponent.KEY.get(player.getWorld()).HackerHackingTime * 20;
                        if (targetHack.hackingTime < requiredTicks) {
                            targetInfo = Text.translatable("hud.kinswathe.hacker.target")
                                    .styled(style -> style.withColor(KinsWatheRoles.HACKER.color()))
                                    .append(Text.literal(" [ " + (int) (((float) targetHack.hackingTime / requiredTicks) * 100) + "% ]")
                                            .styled(style -> style.withColor(Color.GREEN.getRGB())));
                        } else {
                            targetInfo = Text.translatable("hud.kinswathe.hacker.target_hacked")
                                    .styled(style -> style.withColor(Color.GREEN.getRGB()));
                        }
                    }

                    /*
                     * 旧 mixin 自己在 RoleNameRenderer 尾部画这一行。
                     * 现在统一走 ExtraHud，位置仍保持在准心名字下方 32px。
                     */
                    drawCentered(context.renderer(), context.drawContext(), targetInfo, 32, KinsWatheRoles.HACKER.color());
                }
        );
    }

    private static void registerDetectiveTargetHud() {
        RoleNameHudApi.registerExtraHud(
                KinsWathe.id("role_name/detective_target"),
                RoleNameHudApi.DEFAULT_PRIORITY,
                context -> {
                    ClientPlayerEntity player = context.player();
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
                    if (!gameWorld.isRole(player, KinsWatheRoles.DETECTIVE) || !GameFunctions.isPlayerAliveAndSurvival(player)) {
                        return;
                    }

                    PlayerEntity target = aliveTarget(context.targetPlayer());
                    if (target == null || KinsWatheInitializeClient.abilityBind == null) {
                        return;
                    }

                    AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
                    PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
                    if (ability.cooldown > 0
                            || playerShop.balance < ConfigWorldComponent.KEY.get(player.getWorld()).DetectiveAbilityPrice) {
                        return;
                    }

                    /*
                     * 侦探只需要额外提示“按键检查目标”，准星目标本身由 Wathe 统一提供。
                     */
                    Text targetInfo = Text.translatable(
                            "hud.kinswathe.detective.target",
                            KinsWatheInitializeClient.abilityBind.getBoundKeyLocalizedText()
                    ).withColor(KinsWatheRoles.DETECTIVE.color());
                    drawCentered(context.renderer(), context.drawContext(), targetInfo, 32, KinsWatheRoles.DETECTIVE.color());
                }
        );
    }

    private static void registerPhysicianBodyHud() {
        RoleNameHudApi.registerExtraHud(
                KinsWathe.id("role_name/physician_body"),
                RoleNameHudApi.DEFAULT_PRIORITY,
                context -> {
                    ClientPlayerEntity player = context.player();
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
                    if (!gameWorld.isRole(player, KinsWatheRoles.PHYSICIAN) || !GameFunctions.isPlayerAliveAndSurvival(player)) {
                        return;
                    }

                    PlayerBodyEntity body = RoleNameHudApi.findLookedAtBody(player, 2.0F);
                    if (body == null) {
                        return;
                    }

                    /*
                     * 医师尸体提示现在也挂在 RoleName HUD API。
                     * 这样尸体射线、额外 HUD 渲染顺序都由 Wathe 统一管理。
                     */
                    Text deathInfo = getDeathInfo(player, body);
                    drawCentered(context.renderer(), context.drawContext(), deathInfo, 32, Colors.RED);
                }
        );
    }

    private static Text getDeathInfo(@NotNull ClientPlayerEntity player, @NotNull PlayerBodyEntity body) {
        if (isNoellesVultured(body)) {
            int randomLength = player.getRandom().nextBetween(12, 26);
            return Text.literal("a".repeat(randomLength)).formatted(Formatting.OBFUSCATED);
        }

        BodyDeathReasonComponent bodyDeathReason = BodyDeathReasonComponent.KEY.get(body);
        return Text.translatable("hud.death_info", body.age / 20)
                .append(Text.translatable(
                        "death_reason." + bodyDeathReason.deathReason.getNamespace() + "." + bodyDeathReason.deathReason.getPath()
                ));
    }

    private static boolean isNoellesVultured(@NotNull PlayerBodyEntity body) {
        if (!FabricLoader.getInstance().isModLoaded("noellesroles")) {
            return false;
        }

        /*
         * noellesroles 的包名曾在旧代码里写错过。
         * 这里优先读当前正确路径，再保留旧路径兜底，避免玩家本地装的是旧联调包时直接失效。
         */
        return readNoellesVultured(body, "org.agmas.noellesroles.roles.coroner.BodyDeathReasonComponent")
                || readNoellesVultured(body, "org.agmas.noellesroles.coroner.BodyDeathReasonComponent");
    }

    private static boolean readNoellesVultured(@NotNull PlayerBodyEntity body, String className) {
        try {
            Class<?> bodyDeathReasonClass = Class.forName(className);
            Field keyField = bodyDeathReasonClass.getField("KEY");
            Object componentKey = keyField.get(null);
            Method getComponentMethod = componentKey.getClass().getMethod("get", Object.class);
            Object deathReasonInstance = getComponentMethod.invoke(componentKey, body);
            Field vulturedField = bodyDeathReasonClass.getField("vultured");
            return (boolean) vulturedField.get(deathReasonInstance);
        } catch (NoSuchFieldException | ClassNotFoundException | InvocationTargetException |
                 IllegalAccessException | NoSuchMethodException ignored) {
            return false;
        }
    }

    private static boolean countsAsKillerCohort(@NotNull GameWorldComponent gameWorld, @NotNull PlayerEntity player) {
        Role role = gameWorld.getRole(player);
        return role != null
                && (gameWorld.canUseKillerFeatures(player)
                || KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(role)
                || KinsWatheRoles.isKillerSidedNeutral(player));
    }

    private static @Nullable PlayerEntity aliveTarget(@Nullable PlayerEntity target) {
        return target != null && GameFunctions.isPlayerAliveAndSurvival(target) ? target : null;
    }

    private static void drawCentered(@NotNull TextRenderer renderer,
                                     @NotNull DrawContext context,
                                     @NotNull Text text,
                                     int y,
                                     int color) {
        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2.0F, context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
        context.getMatrices().scale(0.6F, 0.6F, 1.0F);
        context.drawTextWithShadow(renderer, text, -renderer.getWidth(text) / 2, y, color);
        context.getMatrices().pop();
    }
}
