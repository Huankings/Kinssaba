package org.BsXinQin.kinswathe;

import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.api.event.AllowPlayerPunching;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.nbt.NbtCompound;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.BsXinQin.kinswathe.packet.host.AbilityC2SPacket;
import org.BsXinQin.kinswathe.packet.items.BlowgunC2SPacket;
import org.BsXinQin.kinswathe.packet.items.HuntingKnifeC2SPacket;
import org.BsXinQin.kinswathe.packet.items.PanC2SPacket;
import org.BsXinQin.kinswathe.packet.roles.BodymakerC2SPacket;
import org.BsXinQin.kinswathe.packet.roles.JudgeC2SPacket;
import org.BsXinQin.kinswathe.roles.cook.CookComponent;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerPhoneComponent;
import org.BsXinQin.kinswathe.roles.hunter.HunterComponent;
import org.BsXinQin.kinswathe.roles.kidnapper.KidnapperComponent;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.BsXinQin.kinswathe.roles.technician.TechnicianComponent;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.jetbrains.annotations.NotNull;

public class KinsWatheGameSettings {

    private static boolean GAME_START = false;
    private static boolean GAME_STOP = false;

    /// 初始化配置文件
    public static void initializeConfig() {
        KinsWatheConfig.HANDLER.load();
    }

    /// 初始化指令
    public static void initializeCommands() {
        KinsWatheCommands.register();
    }

    /// 设置游戏开始和结束功能
    public static void betterGameSettings() {
        //注册游戏开始时事件
        GameEvents.ON_GAME_START.register((gameMode) -> {GAME_START = true;});
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (GAME_START) {
                //切换物品栏
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.getInventory().selectedSlot = 8;
                }
                //指令
                setCommands(server);
                //游戏安全时间
                setGameSafeTime(server);
                GAME_START = false;
            }
        });
        //注册游戏结束时事件
        GameEvents.ON_GAME_STOP.register((gameMode) -> {GAME_STOP = true;});
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (GAME_STOP) {
                //指令
                setCommands(server);
                //游戏安全时间
                GameSafeComponent.KEY.get(server.getOverworld()).reset();
                GAME_STOP = false;
            }
        });
    }

    /// 设置指令
    public static void setCommands(@NotNull MinecraftServer server) {
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "kill @e[type=kinswathe:capture_device]");
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "kill @e[type=wathe:player_body]");
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "kill @e[type=item]");
        if (GAME_STOP) {
            server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "clear @a");
            server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "effect clear @a");
        }
        if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
            server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "kill @e[type=noellesroles:cube]");
        }
    }

    /// 设置游戏安全时间
    public static void setGameSafeTime(@NotNull MinecraftServer server) {
        if (!KinsWatheConfig.HANDLER.instance().EnableStartSafeTime) return;
        if (GameWorldComponent.KEY.get(server.getOverworld()).getGameMode() == WatheGameModes.DISCOVERY || GameWorldComponent.KEY.get(server.getOverworld()).getGameMode() == WatheGameModes.LOOSE_ENDS) return;
        GameSafeComponent.KEY.get(server.getOverworld()).startGameSafe();
        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            if (serverPlayer == null) continue;
            //KinsWathe物品安全时间
            serverPlayer.getItemCooldownManager().set(KinsWatheItems.BLOWGUN, GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            serverPlayer.getItemCooldownManager().set(KinsWatheItems.HUNTING_KNIFE, GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            serverPlayer.getItemCooldownManager().set(KinsWatheItems.KNOCKOUT_DRUG, GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            serverPlayer.getItemCooldownManager().set(KinsWatheItems.POISON_INJECTOR, GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            //Wathe物品安全时间
            serverPlayer.getItemCooldownManager().set(WatheItems.DERRINGER, GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            serverPlayer.getItemCooldownManager().set(WatheItems.KNIFE, GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            serverPlayer.getItemCooldownManager().set(WatheItems.GRENADE, GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            serverPlayer.getItemCooldownManager().set(WatheItems.REVOLVER, GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            serverPlayer.getItemCooldownManager().set(WatheItems.PSYCHO_MODE, GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            //HarpySimpleRoles物品安全时间
            if (FabricLoader.getInstance().isModLoaded("harpysimpleroles")) {
                serverPlayer.getItemCooldownManager().set(Registries.ITEM.get(Identifier.of("harpysimpleroles", "toxin")), GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
                serverPlayer.getItemCooldownManager().set(Registries.ITEM.get(Identifier.of("harpysimpleroles", "bandit_revolver")), GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            }
            //StarryExpress物品安全时间
            if (FabricLoader.getInstance().isModLoaded("starexpress")) {
                serverPlayer.getItemCooldownManager().set(Registries.ITEM.get(Identifier.of("starexpress", "tape")), GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            }
            //StupidExpress物品安全时间
            if (FabricLoader.getInstance().isModLoaded("stupid_express")) {
                serverPlayer.getItemCooldownManager().set(Registries.ITEM.get(Identifier.of("stupid_express", "lighter")), GameConstants.getInTicks(0, KinsWatheConfig.HANDLER.instance().StartingCooldown));
            }
        }
    }

    /// 注册网络数据包
    public static void registerPackets() {
        PayloadTypeRegistry.playC2S().register(AbilityC2SPacket.ID, AbilityC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(BodymakerC2SPacket.ID, BodymakerC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(JudgeC2SPacket.ID, JudgeC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(BlowgunC2SPacket.ID, BlowgunC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(HuntingKnifeC2SPacket.ID, HuntingKnifeC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PanC2SPacket.ID, PanC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BlowgunC2SPacket.ID, new BlowgunC2SPacket.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(HuntingKnifeC2SPacket.ID, new HuntingKnifeC2SPacket.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(PanC2SPacket.ID, new PanC2SPacket.Receiver());
    }

    /// 注册游戏事件
    public static void registerEvents() {
        //死亡事件
        AllowPlayerDeath.EVENT.register(((player, killer, identifier) -> {
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
            DreamerComponent playerDream = DreamerComponent.KEY.get(player);
            PhysicianComponent playerPhysician = PhysicianComponent.KEY.get(player);
            ServerPlayerEntity victimPlayer = player instanceof ServerPlayerEntity serverPlayer ? serverPlayer : null;
            ServerPlayerEntity attackerPlayer = killer instanceof ServerPlayerEntity serverAttacker ? serverAttacker : null;
            PlayerPoisonComponent.KEY.get(player).reset();
            //安全时间死亡事件
            if (GameSafeComponent.KEY.get(player.getWorld()).isSafe()) {
                return identifier == GameConstants.DeathReasons.FELL_OUT_OF_TRAIN;
            }
            //厨师死亡事件
            if (player.getMainHandStack().isOf(KinsWatheItems.PAN) && player.isUsingItem() && player.getActiveItem().getItem().getUseAction(player.getActiveItem()) == UseAction.SPEAR) {
                if (identifier == GameConstants.DeathReasons.GUN) {
                    // 平底锅在挡子弹时先记“挡伤回放”，再消耗平底锅并取消死亡。
                    NbtCompound extra = new NbtCompound();
                    extra.putString("death_reason", identifier.toString());
                    GameRecordManager.recordShieldBlocked(
                            victimPlayer,
                            attackerPlayer,
                            KinsWathe.id("pan"),
                            GameFunctions.resolveDamageItemForBlockedDeath(killer, identifier),
                            extra
                    );
                    KinsWatheItems.setItemAfterUsing(player, KinsWatheItems.PAN, Hand.MAIN_HAND);
                    player.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    return false;
                }
            }
            //梦者死亡事件
            if (playerDream.dreamArmor > 0) {
                // 梦之印记挡伤同理：先记回放，再进行瞬移和清空护盾。
                NbtCompound extra = new NbtCompound();
                extra.putString("death_reason", identifier.toString());
                GameRecordManager.recordShieldBlocked(
                        victimPlayer,
                        attackerPlayer,
                        KinsWathe.id("dream_imprint"),
                        GameFunctions.resolveDamageItemForBlockedDeath(killer, identifier),
                        extra
                );
                playerDream.teleportToDreamer();
                playerDream.reset();
                return false;
            }
            //医师死亡事件
            if (playerPhysician.physicianArmor > 0) {
                if (identifier == GameConstants.DeathReasons.FELL_OUT_OF_TRAIN) return true;
                // 药丸也是独立护盾，不再依赖外部模组酒保护盾，因此挡伤也在这里统一记录。
                NbtCompound extra = new NbtCompound();
                extra.putString("death_reason", identifier.toString());
                GameRecordManager.recordShieldBlocked(
                        victimPlayer,
                        attackerPlayer,
                        KinsWathe.id("pill"),
                        GameFunctions.resolveDamageItemForBlockedDeath(killer, identifier),
                        extra
                );
                playerPhysician.armorSound();
                playerPhysician.reset();
                return false;
            }
            //狂信死亡事件
            if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
                if (KinsWatheConfig.HANDLER.instance().EnableNoellesRolesModify && KinsWatheConfig.HANDLER.instance().JesterAttackKillerModify) {
                    if (killer != null) {
                        if (gameWorld.getRole(player).canUseKiller() && gameWorld.isRole(killer, KinsWatheRoles.noellesrolesRoles("JESTER")) && PlayerPsychoComponent.KEY.get(killer).psychoTicks > 0) {
                            return identifier != GameConstants.DeathReasons.BAT;
                        }
                    }
                }
            }
            return true;
        }));
        //攻击事件
        AllowPlayerPunching.EVENT.register(((attacker, victim) -> {
            return attacker.getMainHandStack().isOf(KinsWatheItems.HUNTING_KNIFE);
        }));
    }

    /// 重置事件
    public static void resetEvents() {
        ResetPlayerEvent.EVENT.register(player -> {
            GameSafeComponent.KEY.get(player.getWorld()).reset();
            PlayerEffectComponent.KEY.get(player).reset();
            AbilityPlayerComponent.KEY.get(player).reset();
            CookComponent.KEY.get(player).reset();
            DreamerComponent.KEY.get(player).reset();
            DreamerKillerComponent.KEY.get(player).reset();
            HackerComponent.KEY.get(player).reset();
            HackerPhoneComponent.KEY.get(player).reset();
            HunterComponent.KEY.get(player).reset();
            KidnapperComponent.KEY.get(player).reset();
            PhysicianComponent.KEY.get(player).reset();
            TechnicianComponent.KEY.get(player).reset();
        });
    }

    /// 初始化方法
    public static void init() {
        //初始化配置文件
        initializeConfig();
        //初始化指令
        initializeCommands();
        //设置游戏开始和结束功能
        betterGameSettings();
        //注册网络数据包
        registerPackets();
        //注册游戏事件
        registerEvents();
        //重置事件
        resetEvents();
    }
}
