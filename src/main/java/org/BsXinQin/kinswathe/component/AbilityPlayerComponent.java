package org.BsXinQin.kinswathe.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;


public class AbilityPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {

    public static final ComponentKey<AbilityPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(KinsWathe.MOD_ID, "ability"), AbilityPlayerComponent.class);
    private final PlayerEntity player;
    public int cooldown = 0;

    public AbilityPlayerComponent(@NotNull PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        if (this.cooldown > 0) {
            -- this.cooldown;
            if (this.cooldown == 0 || this.cooldown % 20 == 0) {
                this.sync();
            }
        }
    }

    @Override
    public void clientTick() {
        if (this.cooldown > 0) {
            --this.cooldown;
        }
    }

    public void setAbilityCooldown(int ticks) {
        this.cooldown = ticks * 20;
        this.sync();
    }

    public void reset() {
        this.cooldown = 0;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("cooldown", this.cooldown);

    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity recipient) {
        // 技能按钮只读取本人的冷却，不能让每个玩家的倒计时广播给附近所有客户端。
        return this.player.equals(recipient);
    }
}
