package org.BsXinQin.kinswathe.client.role_name.roles.detective;

import dev.doctor4t.wathe.api.client.gui.RoleNameHudApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.KinsWatheInitializeClient;
import org.BsXinQin.kinswathe.client.role_name.support.KinsRoleNameHudRenderSupport;
import org.BsXinQin.kinswathe.client.role_name.support.KinsRoleNameHudTargetSupport;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;

/**
 * Detective 职业的准心目标技能提示。
 *
 * <p>侦探对准存活玩家、技能不在冷却、且金币足够时，显示按键检查目标的提示。
 * 实际按键文本从 KinsWatheInitializeClient.abilityBind 读取，保证跟玩家当前键位一致。</p>
 */
public final class DetectiveTargetHudHandler {
    private DetectiveTargetHudHandler() {
    }

    public static void register() {
        RoleNameHudApi.registerExtraHud(
                KinsWathe.id("role_name/detective_target"),
                RoleNameHudApi.DEFAULT_PRIORITY,
                context -> {
                    ClientPlayerEntity player = context.player();
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
                    if (!gameWorld.isRole(player, KinsWatheRoles.DETECTIVE) || !GameFunctions.isPlayerAliveAndSurvival(player)) {
                        return;
                    }

                    PlayerEntity target = KinsRoleNameHudTargetSupport.aliveTarget(context.targetPlayer());
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
                     * 侦探只需要额外提示“按键检查目标”。
                     * 准心玩家本身由 Wathe 统一判定，这里不再重复做射线查询。
                     */
                    Text targetInfo = Text.translatable(
                            "hud.kinswathe.detective.target",
                            KinsWatheInitializeClient.abilityBind.getBoundKeyLocalizedText()
                    ).withColor(KinsWatheRoles.DETECTIVE.color());
                    KinsRoleNameHudRenderSupport.drawCentered(
                            context.renderer(),
                            context.drawContext(),
                            targetInfo,
                            32,
                            KinsWatheRoles.DETECTIVE.color()
                    );
                }
        );
    }
}
