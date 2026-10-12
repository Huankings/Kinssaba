package org.BsXinQin.kinswathe.component;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class GameSafeComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {

    public static final ComponentKey<GameSafeComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(KinsWathe.MOD_ID, "safe"), GameSafeComponent.class);

    @NotNull private final World world;
    public boolean isGameSafe = false;
    public int safeTicks = 0;

    public GameSafeComponent(@NotNull World world) {this.world = world;}

    @Override
    public void serverTick() {
        if (this.isGameSafe) {
            this.notInGameReset();
            if (this.world instanceof @NotNull ServerWorld) {
                if (this.safeTicks <= KinsWatheConfig.HANDLER.instance().StartingCooldown * 20) {
                    ++ this.safeTicks;
                    if (this.world instanceof @NotNull ServerWorld) {
                        if (this.safeTicks > KinsWatheConfig.HANDLER.instance().StartingCooldown * 20) {
                            this.reset();
                        }
                    }
                } else {
                    this.reset();
                }
            }
        }
    }

    @Override
    public void clientTick() {
        /* 开始/结束由服务端同步，中间倒计时由客户端本地推进，避免世界组件每 tick 广播。 */
        if (this.isGameSafe && this.safeTicks <= this.getDurationTicks()) {
            ++this.safeTicks;
        }
    }

    private int getDurationTicks() {
        /* 客户端必须读取服务端同步配置，不能使用玩家自己的本地配置文件。 */
        return Math.max(0, ConfigWorldComponent.KEY.get(this.world).StartingCooldown * 20);
    }

    public void notInGameReset() {
        if (!GameWorldComponent.KEY.get(this.world).isRunning()) {
            this.reset();
        }
    }

    public void startGameSafe() {
        this.reset();
        this.isGameSafe = true;
        this.sync();
    }

    public boolean isSafe() {
        return this.isGameSafe && this.safeTicks > 0;
    }

    public void reset() {
        if (!this.isGameSafe && this.safeTicks == 0) {
            return;
        }
        this.isGameSafe = false;
        this.safeTicks = 0;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.world);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("safeTicks", this.safeTicks);
        tag.putBoolean("isGameSafe", this.isGameSafe);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.safeTicks = tag.contains("safeTicks") ? tag.getInt("safeTicks") : 0;
        this.isGameSafe = tag.contains("isGameSafe") && tag.getBoolean("isGameSafe");
    }
}
