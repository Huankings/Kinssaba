package org.BsXinQin.kinswathe.roles.kidnapper;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
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

public class KidnapperComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<KidnapperComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(KinsWathe.MOD_ID, "kidnapper"), KidnapperComponent.class);

    @NotNull private final PlayerEntity player;
    public UUID controllerUUID = null;
    public int controlTicks = 0;

    public KidnapperComponent(@NotNull PlayerEntity player) {this.player = player;}

    @Override
    public void serverTick() {
        if (this.controlTicks > 0) {
            this.notInGameReset();
            if (this.controlTicks <= 0) {
                return;
            }
            // manualRelease = true 表示绑匪本人按潜行键主动放人；
            // false 则是时间结束、距离过远、控制者消失等自然结束。
            if (this.connectWithController()) {
                return;
            }
            this.teleportToController();
            this.notifyControllerRemainingTime();
            -- this.controlTicks;
            if (this.controlTicks <= 0) {
                this.endControl(false);
                return;
            }
            this.sync();
        }
    }

    public void notInGameReset() {
        if (GameWorldComponent.KEY.get(this.player.getWorld()).getRole(this.player) == null) {
            this.reset();
        }
    }

    public void startControl(@NotNull PlayerEntity controller) {
        this.controllerUUID = controller.getUuid();
        this.controlTicks = GameConstants.getInTicks(0,30);
        this.sync();
    }

    public boolean connectWithController() {
        if (this.controllerUUID == null) return false;
        PlayerEntity controller = this.player.getWorld().getPlayerByUuid(this.controllerUUID);
        if (controller == null) {
            this.endControl(false);
            return true;
        }
        if (this.player.distanceTo(controller) > 5.0f) {
            this.releaseControlTip();
            this.endControl(false);
            return true;
        }
        if (controller.isSneaking() && GameFunctions.isPlayerAliveAndSurvival(controller) && GameFunctions.isPlayerAliveAndSurvival(this.player)) {
            this.releaseControlTip();
            this.endControl(true);
            return true;
        }
        if (GameFunctions.isPlayerSpectatingOrCreative(controller) || GameFunctions.isPlayerSpectatingOrCreative(this.player)) {
            this.releaseControlTip();
            this.endControl(false);
            return true;
        }
        return false;
    }

    public void teleportToController() {
        if (this.controllerUUID == null || this.player.getWorld().isClient) return;
        PlayerEntity controller = this.player.getWorld().getPlayerByUuid(this.controllerUUID);
        if (controller != null) {
            if (this.player.getWorld() instanceof @NotNull ServerWorld serverWorld) {
                this.player.teleport(serverWorld, controller.getX(), controller.getY(), controller.getZ(), Set.of(), controller.getYaw(), controller.getPitch());
            }
        }
    }

    public void notifyControllerRemainingTime() {
        if (this.controllerUUID == null) return;
        PlayerEntity controller = this.player.getWorld().getPlayerByUuid(this.controllerUUID);
        if (controller != null && this.controlTicks / 20 >= 0) {
            controller.sendMessage(Text.translatable("tip.kinswathe.kidnapper.timeleft", this.controlTicks / 20).withColor(KinsWatheRoles.KIDNAPPER.color()), true);
            if (this.controlTicks == 1) releaseControlTip();
        }
    }

    public void releaseControlTip() {
        if (this.controllerUUID == null) return;
        PlayerEntity controller = this.player.getWorld().getPlayerByUuid(this.controllerUUID);
        if (controller != null) {
            controller.sendMessage(Text.translatable("tip.kinswathe.kidnapper.release").withColor(KinsWatheRoles.KIDNAPPER.color()), true);
            controller.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    private void endControl(boolean manualRelease) {
        PlayerEntity controller = this.controllerUUID == null ? null : this.player.getWorld().getPlayerByUuid(this.controllerUUID);
        if (manualRelease) {
            // 绑匪自己结束劫持时，回放要记成“提前结束了对某人的劫持”。
            if (controller instanceof ServerPlayerEntity serverController && this.player instanceof ServerPlayerEntity serverTarget) {
                GameRecordManager.recordSkillUse(serverController, KinsWathe.id("kidnapper_release"), serverTarget, null);
            }
        } else if (this.player instanceof ServerPlayerEntity serverTarget) {
            // 目标自然脱离 / 时间结束时，只保留“被劫持状态结束”。
            GameRecordManager.recordSkillUse(serverTarget, KinsWathe.id("kidnapper_release"), null, null);
        }
        this.reset();
    }

    public void reset() {
        this.controllerUUID = null;
        this.controlTicks = 0;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("controlTicks", this.controlTicks);
        if (this.controllerUUID != null) tag.putUuid("controllerUUID", this.controllerUUID);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.controlTicks = tag.contains("controlTicks") ? tag.getInt("controlTicks") : 0;
        this.controllerUUID = tag.contains("controllerUUID") ? tag.getUuid("controllerUUID") : null;
    }
}
