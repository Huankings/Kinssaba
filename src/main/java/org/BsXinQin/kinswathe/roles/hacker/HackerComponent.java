package org.BsXinQin.kinswathe.roles.hacker;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HackerComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<HackerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(KinsWathe.MOD_ID, "hacker"), HackerComponent.class);

    @NotNull public final PlayerEntity player;
    public int hackingTime = 0;

    public HackerComponent(@NotNull PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        checkIfTargetedByHacker();
        if (this.hackingTime > 0) {
            notInGameReset();
        }
    }

    public void notInGameReset() {
        if (GameWorldComponent.KEY.get(this.player.getWorld()).getRole(this.player) == null) {
            this.reset();
        }
    }

    public void checkIfTargetedByHacker() {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
        GameSafeComponent gameSafe = GameSafeComponent.KEY.get(this.player.getWorld());
        if (!gameWorld.isRunning() || gameSafe.isSafe() || gameWorld.getRole(this.player) == null) return;
        if (!(gameWorld.canUseKillerFeatures(this.player) || KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(gameWorld.getRole(this.player)) || KinsWatheRoles.isKillerSidedNeutral(this.player)) && GameFunctions.isPlayerAliveAndSurvival(this.player)) {
            for (ServerPlayerEntity hacker : this.player.getServer().getPlayerManager().getPlayerList()) {
                if (hacker == null) continue;
                if (gameWorld.isRole(hacker, KinsWatheRoles.HACKER) && GameFunctions.isPlayerAliveAndSurvival(hacker)) {
                    if (isHackerLookingAtTarget(hacker)) {
                        if (this.hackingTime <= KinsWatheConfig.HANDLER.instance().HackerHackingTime * 20) {
                            ++ this.hackingTime;
                            this.addRoleOnPhone(hacker);
                            this.sync();
                        }
                        return;
                    }
                }
            }
        }
    }

    public boolean isHackerLookingAtTarget(@NotNull PlayerEntity hacker) {
        HitResult hitResult = ProjectileUtil.getCollision(hacker, entity -> entity instanceof @NotNull PlayerEntity targetPlayer && GameFunctions.isPlayerAliveAndSurvival(targetPlayer), 2.0F);
        return hitResult instanceof @NotNull EntityHitResult entityHitResult && entityHitResult.getEntity() == this.player;
    }

    public void addRoleOnPhone(@NotNull PlayerEntity hacker) {
        if (this.hackingTime == KinsWatheConfig.HANDLER.instance().HackerHackingTime * 20) {
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
            WorldModifierComponent modifier = WorldModifierComponent.KEY.get(this.player.getWorld());
            PlayerShopComponent hackerShop = PlayerShopComponent.KEY.get(hacker);
            if (KinsWatheConfig.HANDLER.instance().HackerHasShop) {
                if (modifier.isModifier(hacker, KinsWatheRoles.TASKMASTER)) hackerShop.balance += 150;
                else hackerShop.balance += 100;
                hackerShop.sync();
            }
            Role targetRole = gameWorld.getRole(this.player);
            if (targetRole == null) return;
            for (ServerPlayerEntity serverPlayer : this.player.getServer().getPlayerManager().getPlayerList()) {
                if (serverPlayer == null) continue;
                if (gameWorld.getRole(serverPlayer) == null) continue;
                if ((gameWorld.canUseKillerFeatures(serverPlayer) || gameWorld.isRole(serverPlayer, KinsWatheRoles.HACKER)) && GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
                    serverPlayer.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    for (int i = 0; i < serverPlayer.getInventory().size(); i++) {
                        ItemStack stack = serverPlayer.getInventory().getStack(i);
                        if (stack.isOf(KinsWatheItems.PHONE)) {
                            LoreComponent lore = stack.get(DataComponentTypes.LORE);
                            List<Text> loreLines = lore != null ? new ArrayList<>(lore.lines()) : new ArrayList<>();
                            Text playerInfo = Text.literal(this.player.getName().getString() + " ").styled(style -> style.withItalic(false).withColor(0xFFFFFF)).append(Harpymodloader.getRoleName(targetRole).copy().styled(style -> style.withItalic(false).withColor(targetRole.color())));
                            if (!loreLines.contains(playerInfo)) {
                                loreLines.add(playerInfo);
                                LoreComponent newLore = new LoreComponent(loreLines);
                                stack.set(DataComponentTypes.LORE, newLore);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void refreshWeaponCooldown(@NotNull PlayerEntity player) {
        player.getItemCooldownManager().set(KinsWatheItems.ICON_WEAPON_COOLDOWN_REFRESH, GameConstants.ITEM_COOLDOWNS.get(KinsWatheItems.ICON_WEAPON_COOLDOWN_REFRESH));
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            if (serverPlayer == null) continue;
            if (gameWorld.canUseKillerFeatures(serverPlayer)) {
                serverPlayer.sendMessage(Text.translatable("tip.kinswathe.hacker.weapon_cooldown_refresh").withColor(Color.RED.getRGB()), true);
                serverPlayer.playSoundToPlayer(SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                //KinsWathe杀手物品
                serverPlayer.getItemCooldownManager().set(KinsWatheItems.BLOWGUN, GameConstants.getInTicks(0, 0));
                serverPlayer.getItemCooldownManager().set(KinsWatheItems.HUNTING_KNIFE, GameConstants.getInTicks(0, 0));
                serverPlayer.getItemCooldownManager().set(KinsWatheItems.POISON_INJECTOR, GameConstants.getInTicks(0, 0));
                //Wathe杀手物品
                serverPlayer.getItemCooldownManager().set(WatheItems.KNIFE, GameConstants.getInTicks(0, 0));
                serverPlayer.getItemCooldownManager().set(WatheItems.REVOLVER, GameConstants.getInTicks(0, 0));
                //HarpySimpleRoles杀手物品
                if (FabricLoader.getInstance().isModLoaded("harpysimpleroles")) {
                    serverPlayer.getItemCooldownManager().set(Registries.ITEM.get(Identifier.of("harpysimpleroles", "toxin")), GameConstants.getInTicks(0, 0));
                    serverPlayer.getItemCooldownManager().set(Registries.ITEM.get(Identifier.of("harpysimpleroles", "bandit_revolver")), GameConstants.getInTicks(0, 0));
                }
                //StarryExpress杀手物品
                if (FabricLoader.getInstance().isModLoaded("starexpress")) {
                    serverPlayer.getItemCooldownManager().set(Registries.ITEM.get(Identifier.of("starexpress", "tape")), GameConstants.getInTicks(0, 0));
                }
            }
        }
    }

    public static void refreshAbilityCooldown(@NotNull PlayerEntity player) {
        player.getItemCooldownManager().set(KinsWatheItems.ICON_ABILITY_COOLDOWN_REFRESH, GameConstants.ITEM_COOLDOWNS.get(KinsWatheItems.ICON_ABILITY_COOLDOWN_REFRESH));
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            if (serverPlayer == null) continue;
            if (gameWorld.canUseKillerFeatures(serverPlayer)) {
                AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(serverPlayer);
                serverPlayer.sendMessage(Text.translatable("tip.kinswathe.hacker.ability_cooldown_refresh").withColor(Color.GREEN.getRGB()), true);
                serverPlayer.playSoundToPlayer(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                serverPlayer.getItemCooldownManager().set(WatheItems.PSYCHO_MODE, 0);
                ability.setAbilityCooldown(0);
            }
        }
    }

    public static void refreshPotionEffect(@NotNull PlayerEntity player) {
        player.getItemCooldownManager().set(KinsWatheItems.ICON_POTION_EFFECT_REFRESH, GameConstants.ITEM_COOLDOWNS.get(KinsWatheItems.ICON_POTION_EFFECT_REFRESH));
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            if (serverPlayer == null) continue;
            if (!gameWorld.canUseKillerFeatures(serverPlayer)) continue;
            PlayerEffectComponent playerEffect = PlayerEffectComponent.KEY.get(serverPlayer);
            serverPlayer.sendMessage(Text.translatable("tip.kinswathe.hacker.potion_effect_refresh").withColor(Color.YELLOW.getRGB()), true);
            serverPlayer.playSoundToPlayer(SoundEvents.ENTITY_ALLAY_ITEM_GIVEN, SoundCategory.PLAYERS, 1.0F, 1.0F);
            for (StatusEffectInstance effect : serverPlayer.getStatusEffects()) {
                if (!Objects.equals(StatusEffects.INVISIBILITY, effect.getEffectType())) {
                    serverPlayer.removeStatusEffect(effect.getEffectType());
                }
            }
            playerEffect.reset();
        }
    }

    public void reset() {
        this.hackingTime = 0;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("hackingTime", this.hackingTime);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.hackingTime = tag.getInt("hackingTime");
    }
}