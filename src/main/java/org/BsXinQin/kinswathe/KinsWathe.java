package org.BsXinQin.kinswathe;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import dev.doctor4t.wathe.record.GameRecordManager;
import org.agmas.noellesroles.api.event.DelusionEvents;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;
import org.BsXinQin.kinswathe.record.KinsWatheReplayFormatters;

import java.awt.*;

public class KinsWathe implements ModInitializer {

    public static String MOD_ID = "kinswathe";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        //初始化游戏设置
        KinsWatheGameSettings.init();
        //初始化角色
        KinsWatheRoles.init();
        //初始化物品
        KinsWatheItems.init();
        //初始化实体
        KinsWatheEntities.init();
        //初始化回放格式化器
        KinsWatheReplayFormatters.register();

        /**
         * 幻觉试剂已经不再复用 fake poison marker。
         * 因此梦者计数、医师提示等兼容逻辑，改成直接监听 noellesroles 暴露出的运行时事件。
         */
        if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
            DelusionEvents.STARTED.register((player, applier) -> {
                if (dev.doctor4t.wathe.game.GameFunctions.isPlayerSpectatingOrCreative(player)) {
                    return;
                }

                dev.doctor4t.wathe.cca.GameWorldComponent gameWorld = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld());
                if (gameWorld.isRole(player, KinsWatheRoles.ROBOT)) {
                    return;
                }

                for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                    if (serverPlayer == null || !dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
                        continue;
                    }

                    if (gameWorld.isRole(serverPlayer, KinsWatheRoles.PHYSICIAN)) {
                        serverPlayer.sendMessage(Text.translatable("tip.kinswathe.physician.poisoned").withColor(Color.RED.getRGB()), true);
                    }

                    if (!gameWorld.canUseKillerFeatures(player)
                            && !gameWorld.isRole(player, KinsWatheRoles.DREAMER)
                            && gameWorld.isRole(serverPlayer, KinsWatheRoles.DREAMER)) {
                        DreamerKillerComponent playerDreamer = DreamerKillerComponent.KEY.get(serverPlayer);
                        serverPlayer.sendMessage(Text.translatable("tip.kinswathe.dreamer.fake_poisoned").withColor(KinsWatheRoles.DREAMER.color()), true);
                        if (!playerDreamer.hasBecomeKiller) {
                            playerDreamer.dreamerCounts += 1;
                            NbtCompound extra = new NbtCompound();
                            extra.putInt("counts", playerDreamer.dreamerCounts);
                            extra.putInt("required", playerDreamer.dreamerRequired);
                            GameRecordManager.recordGlobalEvent(player.getServerWorld(), id("dreamer_counts"), serverPlayer, extra);
                            playerDreamer.sync();
                        }
                    }
                }
            });
        }
    }
}
