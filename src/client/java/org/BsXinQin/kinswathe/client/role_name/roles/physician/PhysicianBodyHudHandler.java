package org.BsXinQin.kinswathe.client.role_name.roles.physician;

import dev.doctor4t.wathe.api.client.gui.RoleNameHudApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.role_name.support.KinsRoleNameHudRenderSupport;
import org.BsXinQin.kinswathe.component.BodyDeathReasonComponent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Physician 职业的尸体死因 HUD。
 *
 * <p>医师对准 Wathe 尸体时显示死亡时间和死因；如果尸体被 NoellesRoles Vulture 处理过，
 * 死因会用混淆文本遮盖，保留旧 mixin 的欺骗效果。</p>
 */
public final class PhysicianBodyHudHandler {
    private PhysicianBodyHudHandler() {
    }

    public static void register() {
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
                     * 医师尸体提示现在挂在 RoleName HUD API。
                     * Wathe 统一处理尸体射线和额外 HUD 渲染顺序，避免继续 mixin RoleNameRenderer 局部变量。
                     */
                    Text deathInfo = getDeathInfo(player, body);
                    KinsRoleNameHudRenderSupport.drawCentered(
                            context.renderer(),
                            context.drawContext(),
                            deathInfo,
                            32,
                            Colors.RED
                    );
                }
        );
    }

    private static Text getDeathInfo(@NotNull ClientPlayerEntity player, @NotNull PlayerBodyEntity body) {
        if (isNoellesVultured(body)) {
            /*
             * Vulture 吃过/污染过的尸体不能给医师稳定死因。
             * 每帧给一个随机长度的混淆串，维持“看到了信息但无法读懂”的旧表现。
             */
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
            /*
             * 反射兜底只用于兼容可选 NoellesRoles。
             * 任何类名、字段名或组件实现不匹配时，都按“没有被 Vulture 处理”继续显示 kinssaba 自己的死因。
             */
            return false;
        }
    }
}
