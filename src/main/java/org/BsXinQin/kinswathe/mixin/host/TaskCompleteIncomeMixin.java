package org.BsXinQin.kinswathe.mixin.host;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.util.TaskCompletePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworking.class)
public abstract class TaskCompleteIncomeMixin {

    /**
     * 统一处理 KinsWathe 里所有“做任务给金币”的职业/词条逻辑。
     *
     * <p>这里不再继续混入 PlayerMoodComponent 的私有实现细节，
     * 而是改成监听 Wathe 在“任务真正完成”后统一发送的 TaskCompletePayload。
     *
     * <p>这样做有两个直接好处：
     * 1. 旧发布版 Wathe 与当前本地魔改版 Wathe 都会发送这个包，因此兼容两边；
     * 2. 不再依赖 completeTask / setMood 这类容易随着实现重构而变化的内部方法，
     *    IDE 和运行时都更稳定。
     *
     * <p>只有当发出的 payload 确实是 TaskCompletePayload 时，
     * 才说明这个玩家刚完成了一个任务，此时再统一结算 KinsWathe 的任务收入。
     */
    @Inject(
            method = "send(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/packet/CustomPayload;)V",
            at = @At("HEAD")
    )
    private static void kinswathe$giveIncomeOnTaskComplete(@NotNull ServerPlayerEntity player, @NotNull CustomPayload payload, CallbackInfo ci) {
        if (!(payload instanceof TaskCompletePayload)) {
            return;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        Role role = gameWorld.getRole(player);
        if (role == null) {
            return;
        }

        PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
        WorldModifierComponent modifier = WorldModifierComponent.KEY.get(player.getWorld());
        int income = 0;

        /**
         * 通用任务收入：
         * 没有加载 noellesroles 时，KinsWathe 会负责给真实心情角色的任务金币。
         * 旧逻辑是“心情上升就给 50”，现在改成“任务真实完成就给 50”，
         * 效果等价，但不再受 setMood 是否被调用影响。
         */
        if (!FabricLoader.getInstance().isModLoaded("noellesroles") && role.getMoodType() == Role.MoodType.REAL) {
            income += 50;
        }

        /**
         * 黑客任务收入：
         * 只有开启 Hacker 商店配置时，黑客做完任务才给 50 金币。
         */
        if (KinsWatheConfig.HANDLER.instance().HackerHasShop && gameWorld.isRole(player, KinsWatheRoles.HACKER)) {
            income += 50;
        }

        /**
         * 执照恶棍任务收入：
         * 每完成一个任务固定给 50 金币。
         */
        if (gameWorld.isRole(player, KinsWatheRoles.LICENSED_VILLAIN)) {
            income += 50;
        }

        /**
         * 任务大师词条加成：
         * 这部分原本同样挂在 setMood 上，现在一起迁到真实任务完成点。
         * 可用杀手能力的角色给 50，否则给 25，保持旧行为不变。
         */
        if (modifier.isModifier(player, KinsWatheRoles.TASKMASTER)) {
            if (role.canUseKiller()) {
                income += 50;
            } else {
                income += 25;
            }
        }

        /**
         * 统一累计后只同步一次，避免同一个任务完成事件因为多种身份/词条奖励叠加而重复刷同步包。
         */
        if (income > 0) {
            playerShop.addToBalance(income);
        }
    }
}
