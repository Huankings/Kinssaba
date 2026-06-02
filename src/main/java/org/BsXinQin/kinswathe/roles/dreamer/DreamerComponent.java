package org.BsXinQin.kinswathe.roles.dreamer;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Set;
import java.util.UUID;

public class DreamerComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<DreamerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(KinsWathe.MOD_ID, "dreamer"), DreamerComponent.class);

    @NotNull private final PlayerEntity player;
    public UUID dreamerUUID = null;
    public int dreamArmor = 0;

    public DreamerComponent(@NotNull PlayerEntity player) {this.player = player;}

    @Override
    public void serverTick() {
        if (this.dreamArmor > 0) {
            this.notInGameReset();
            this.connectWithDreamer();
        }
    }

    public void notInGameReset() {
        if (GameWorldComponent.KEY.get(this.player.getWorld()).getRole(this.player) == null) {
            this.reset();
        }
    }

    public void imprintDreamer(@NotNull PlayerEntity dreamer) {
        this.dreamerUUID = dreamer.getUuid();
        this.dreamArmor = 1;
        this.sync();
    }

    public void connectWithDreamer() {
        if (this.dreamerUUID == null) return;
        PlayerEntity dreamer = this.player.getWorld().getPlayerByUuid(this.dreamerUUID);
        if (dreamer == null || GameFunctions.isPlayerSpectatingOrCreative(dreamer)) {
            if (GameFunctions.isPlayerAliveAndSurvival(this.player)) {
                this.player.sendMessage(Text.translatable("tip.kinswathe.dreamer.disconnect").withColor(KinsWatheRoles.DREAMER.color()), true);
                this.player.playSoundToPlayer(SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            this.reset();
        }
    }

    public void teleportToDreamer() {
        if (this.dreamerUUID == null || this.player.getWorld().isClient) return;
        PlayerEntity dreamer = this.player.getWorld().getPlayerByUuid(this.dreamerUUID);
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
        PlayerPsychoComponent playerPsycho = PlayerPsychoComponent.KEY.get(this.player);
        if (GameFunctions.isPlayerAliveAndSurvival(dreamer) && GameFunctions.isPlayerAliveAndSurvival(this.player)) {
            if (gameWorld.isRole(dreamer, KinsWatheRoles.DREAMER)) {
                DreamerKillerComponent playerDreamer = DreamerKillerComponent.KEY.get(dreamer);
                if (!playerDreamer.hasBecomeKiller) {
                    playerDreamer.dreamerCounts += 1;
                    // 梦者计数是局内核心进度，记录成全局事件后，回放结算里也能看到“当前幻觉人数”。
                    if (dreamer instanceof net.minecraft.server.network.ServerPlayerEntity serverDreamer) {
                        NbtCompound extra = new NbtCompound();
                        extra.putInt("counts", playerDreamer.dreamerCounts);
                        extra.putInt("required", playerDreamer.dreamerRequired);
                        GameRecordManager.recordGlobalEvent(serverDreamer.getServerWorld(), KinsWathe.id("dreamer_counts"), serverDreamer, extra);
                    }
                    playerDreamer.sync();
                }
            }
            if (this.player.getWorld() instanceof @NotNull ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.PORTAL, this.player.getX(), this.player.getY(), this.player.getZ(), 75, 0.5, 1.5, 0.5, 0.1);
                serverWorld.playSound(null, this.player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                if (playerPsycho.psychoTicks <= 0) {
                    this.player.teleport(serverWorld, dreamer.getX(), dreamer.getY(), dreamer.getZ(), Set.of(), dreamer.getYaw(), dreamer.getPitch());
                }
            }
            dreamer.playSoundToPlayer(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    public void reset() {
        this.dreamerUUID = null;
        this.dreamArmor = 0;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("dreamArmor", this.dreamArmor);
        if (this.dreamerUUID != null) tag.putUuid("dreamerUUID", this.dreamerUUID);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.dreamArmor = tag.contains("dreamArmor") ? tag.getInt("dreamArmor") : 0;
        this.dreamerUUID = tag.contains("dreamerUUID") ? tag.getUuid("dreamerUUID") : null;
    }
}
