package org.BsXinQin.kinswathe.roles.bodymaker;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheEntities;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheParticles;
import dev.doctor4t.wathe.index.WatheSounds;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.BodyDeathReasonComponent;
import org.BsXinQin.kinswathe.packet.roles.BodymakerC2SPacket;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BodymakerAbility {

    public static void register(@NotNull BodymakerC2SPacket payload, @NotNull PlayerEntity player) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
        if (gameWorld.isRole(player, KinsWatheRoles.BODYMAKER) && GameFunctions.isPlayerAliveAndSurvival(player) && ability.cooldown <= 0) {
            ServerPlayerEntity target = player.getServer().getPlayerManager().getPlayer(payload.target());
            if (target != null) {
                PlayerBodyEntity playerBody = WatheEntities.PLAYER_BODY.create(target.getWorld());
                if (playerBody != null) {
                    playerBody.setPlayerUuid(target.getUuid());
                    Vec3d spawnPos = player.getPos().add(player.getRotationVector().normalize().multiply(1));
                    playerBody.refreshPositionAndAngles(spawnPos.getX(), player.getY(), spawnPos.getZ(), player.getYaw(), 0f);
                    playerBody.setYaw(player.getYaw());
                    playerBody.setHeadYaw(player.getYaw());
                    playerBody.prevYaw = player.getYaw();
                    playerBody.prevHeadYaw = player.getYaw();
                    playerBody.bodyYaw = player.getYaw();
                    playerBody.prevBodyYaw = player.getYaw();
                    playerBody.setPitch(0f);
                    playerBody.age = 0;
                    target.getWorld().spawnEntity(playerBody);
                    BodyDeathReasonComponent bodyDeathReason = BodyDeathReasonComponent.KEY.get(playerBody);
                    bodyDeathReason.deathReason = Identifier.of(payload.deathReason());
                    if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
                        try {
                            // 当前 NoellesRoles 已将尸体组件归档到 roles.coroner 包；保留反射是为了让 kinssaba 在未安装 Noelles 时仍可独立运行。
                            Class<?> bodyDeathReasonClass = Class.forName("org.agmas.noellesroles.roles.coroner.BodyDeathReasonComponent");
                            Field keyField = bodyDeathReasonClass.getField("KEY");
                            Object componentKey = keyField.get(null);
                            Method getComponentMethod = componentKey.getClass().getMethod("get", Object.class);
                            Object deathReasonInstance = getComponentMethod.invoke(componentKey, playerBody);
                            Field deathReasonField = bodyDeathReasonClass.getField("deathReason");
                            Field playerRoleField = bodyDeathReasonClass.getField("playerRole");
                            Method syncMethod = bodyDeathReasonClass.getMethod("sync");
                            deathReasonField.set(deathReasonInstance, Identifier.of(payload.deathReason()));
                            if (!KinsWatheConfig.HANDLER.instance().BodymakerAbilityFakeRole) {
                                if (gameWorld.isRole(target, KinsWatheRoles.BODYMAKER)) {
                                    playerRoleField.set(deathReasonInstance, WatheRoles.KILLER.identifier());
                                } else {
                                    playerRoleField.set(deathReasonInstance, gameWorld.getRole(target) != null ? gameWorld.getRole(target).identifier() : WatheRoles.CIVILIAN.identifier());
                                }
                            } else {
                                playerRoleField.set(deathReasonInstance, Identifier.of(payload.role()));
                            }
                            if (Identifier.of(payload.role()).equals(Identifier.of("noellesroles", "noisemaker"))) {
                                playerBody.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 1200, 0));
                            }
                            syncMethod.invoke(deathReasonInstance);
                        } catch (NoSuchFieldException | ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException ignored) {}
                    }
                    if (Identifier.of(payload.deathReason()).equals(GameConstants.DeathReasons.GUN)) {
                        player.getWorld().playSound(null, player.getX(), player.getEyeY(), player.getZ(), WatheSounds.ITEM_REVOLVER_SHOOT, SoundCategory.PLAYERS, 5f, 1f + player.getRandom().nextFloat() * .1f - .05f);
                    } else if (Identifier.of(payload.deathReason()).equals(GameConstants.DeathReasons.GRENADE)) {
                        if (player.getWorld() instanceof @NotNull ServerWorld serverWorld) {
                            serverWorld.playSound(null, playerBody.getBlockPos(), WatheSounds.ITEM_GRENADE_EXPLODE, SoundCategory.PLAYERS, 5f, 1f + playerBody.getRandom().nextFloat() * .1f - .05f);
                            serverWorld.spawnParticles(WatheParticles.BIG_EXPLOSION, playerBody.getX(), playerBody.getY() + .1f, playerBody.getZ(), 1, 0, 0, 0, 0);
                            serverWorld.spawnParticles(ParticleTypes.SMOKE, playerBody.getX(), playerBody.getY() + .1f, playerBody.getZ(), 100, 0, 0, 0, .2f);
                            serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, WatheItems.THROWN_GRENADE.getDefaultStack()), playerBody.getX(), playerBody.getY() + .1f, playerBody.getZ(), 100, 0, 0, 0, 1f);
                        }
                    } else {
                        player.playSoundToPlayer(SoundEvents.ENTITY_SKELETON_CONVERTED_TO_STRAY, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
                }
                ability.setAbilityCooldown(KinsWatheConfig.HANDLER.instance().BodymakerAbilityCooldown);
            }
        }
    }
}
