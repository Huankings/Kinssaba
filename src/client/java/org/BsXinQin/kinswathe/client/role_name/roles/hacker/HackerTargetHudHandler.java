package org.BsXinQin.kinswathe.client.role_name.roles.hacker;

import dev.doctor4t.wathe.api.client.gui.RoleNameHudApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.role_name.killer_sided.KinsKillerSidedCohortSupport;
import org.BsXinQin.kinswathe.client.role_name.support.KinsRoleNameHudRenderSupport;
import org.BsXinQin.kinswathe.client.role_name.support.KinsRoleNameHudTargetSupport;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;

import java.awt.Color;

/**
 * Hacker 职业的准心目标破解提示。
 *
 * <p>Hacker 对准可破解的非杀手侧存活目标时，在 Wathe 准心名字下方显示安全状态、
 * 破解进度或已破解状态。目标获取和绘制顺序都交给 RoleNameHudApi 的 Context。</p>
 */
public final class HackerTargetHudHandler {
    private HackerTargetHudHandler() {
    }

    public static void register() {
        RoleNameHudApi.registerExtraHud(
                KinsWathe.id("role_name/hacker_target"),
                RoleNameHudApi.DEFAULT_PRIORITY,
                context -> {
                    ClientPlayerEntity player = context.player();
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
                    if (!gameWorld.isRole(player, KinsWatheRoles.HACKER) || !GameFunctions.isPlayerAliveAndSurvival(player)) {
                        return;
                    }

                    PlayerEntity target = KinsRoleNameHudTargetSupport.aliveTarget(context.targetPlayer());
                    if (target == null
                            || gameWorld.getRole(target) == null
                            || KinsKillerSidedCohortSupport.countsAsHackerFilteredKillerCohort(gameWorld, target)) {
                        return;
                    }

                    /*
                     * 游戏处于 safe 阶段时，Hacker 只能看到“安全期不可破解”的提示。
                     * 非 safe 阶段再根据目标组件里的 hackingTime 计算进度百分比。
                     */
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

                    KinsRoleNameHudRenderSupport.drawCentered(
                            context.renderer(),
                            context.drawContext(),
                            targetInfo,
                            32,
                            KinsWatheRoles.HACKER.color()
                    );
                }
        );
    }
}
