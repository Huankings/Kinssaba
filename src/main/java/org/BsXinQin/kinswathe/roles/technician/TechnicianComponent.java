package org.BsXinQin.kinswathe.roles.technician;

import dev.doctor4t.wathe.api.blackout.BlackoutApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class TechnicianComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<TechnicianComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(KinsWathe.MOD_ID, "technician"), TechnicianComponent.class);

    @NotNull private final PlayerEntity player;
    public int technicianTicks = 0;

    public TechnicianComponent(@NotNull PlayerEntity player) {this.player = player;}

    @Override
    public void serverTick() {
        if (this.technicianTicks > 0) {
            this.notInGameReset();
            -- this.technicianTicks;
            this.sync();
        }
    }

    public void notInGameReset() {
        if (GameWorldComponent.KEY.get(this.player.getWorld()).getRole(this.player) == null) {
            this.reset();
        }
    }

    public void setCapturedTicks(int ticks) {
        this.technicianTicks = ticks;
        this.sync();
    }

    public static void stopBlackout(@NotNull PlayerEntity player) {
        player.getItemCooldownManager().set(KinsWatheItems.ICON_POWER_RESTORATION, GameConstants.ITEM_COOLDOWNS.get(KinsWatheItems.ICON_POWER_RESTORATION));
        /*
         * 停电黑幕和停电药水已迁移到 Wathe 本体。
         * 技师只负责触发“恢复电力”这个职业商品效果，真实清理由 BlackoutApi 统一处理：
         * 恢复灯光、清空停电倒计时、同步客户端黑幕，并清理 Wathe 自己发放的停电药水。
         */
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            BlackoutApi.restorePower(serverWorld);
        }
        if (player.getWorld().getServer() == null) {
            return;
        }
        for (ServerPlayerEntity serverPlayer : player.getWorld().getServer().getPlayerManager().getPlayerList()) {
            if (serverPlayer == null) continue;
            serverPlayer.playSoundToPlayer(WatheSounds.BLOCK_LIGHT_TOGGLE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    public void reset() {
        this.technicianTicks = 0;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("technicianTicks", this.technicianTicks);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.technicianTicks = tag.contains("technicianTicks") ? tag.getInt("technicianTicks") : 0;
    }
}
