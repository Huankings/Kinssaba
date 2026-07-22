package org.BsXinQin.kinswathe.component;

import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class PlayerEffectComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<PlayerEffectComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(KinsWathe.MOD_ID, "effect"), PlayerEffectComponent.class);

    @NotNull private final PlayerEntity player;
    public int stunTicks = 0;
    private @Nullable Identifier stunSource = null;

    public PlayerEffectComponent(@NotNull PlayerEntity player) {this.player = player;}

    @Override
    public void serverTick() {
        boolean changed = false;

        if (this.stunTicks > 0) {
            -- this.stunTicks;
            changed = true;
            // 眩晕自然结束时，补一条“结束事件”回放，避免只记录“开始敲晕”而没有收尾。
            if (this.stunTicks == 0 && this.stunSource != null && this.player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                GameRecordManager.recordGlobalEvent(serverPlayer.getServerWorld(), this.stunSource, serverPlayer, null);
                this.stunSource = null;
            }
        }

        if (changed) {
            this.sync();
        }
    }

    public void setStunTicks(int ticks) {
        this.setStunTicks(ticks, null);
    }

    public void setStunTicks(int ticks, @Nullable Identifier source) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, ticks, 5, false, true, true));
        this.stunTicks = ticks;
        // source 记录“这个眩晕是谁触发的”，例如平底锅结束时才能对应回放出“眩晕状态解除”。
        this.stunSource = source;
        this.sync();
    }

    public void reset() {
        this.stunTicks = 0;
        this.stunSource = null;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("stunTicks", this.stunTicks);
        if (this.stunSource != null) {
            tag.putString("stunSource", this.stunSource.toString());
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.stunTicks = tag.contains("stunTicks") ? tag.getInt("stunTicks") : 0;
        this.stunSource = tag.contains("stunSource") ? Identifier.of(tag.getString("stunSource")) : null;
    }
}
