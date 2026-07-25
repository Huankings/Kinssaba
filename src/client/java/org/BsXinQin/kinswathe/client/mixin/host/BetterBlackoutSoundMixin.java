package org.BsXinQin.kinswathe.client.mixin.host;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.BsXinQin.kinswathe.client.KinsWatheInitializeClient;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.roles.technician.TechnicianComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 增强停电的声音触发同步。
 *
 * <p>屏幕黑幕已经迁移到 Wathe HudOverlayApi；这里仍然保留为 mixin，
 * 因为它监听的是原版 SoundSystem 播放停电环境音的瞬间，用来记录客户端黑幕结束时间。</p>
 */
@Mixin(SoundSystem.class)
public class BetterBlackoutSoundMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void onPlaySound(@NotNull SoundInstance sound, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) {
            return;
        }
        if (!ConfigWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld()).EnableBetterBlackout) {
            return;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        TechnicianComponent playerBlackout = TechnicianComponent.KEY.get(MinecraftClient.getInstance().player);
        if (gameWorld.isRunning() && sound.getId().equals(WatheSounds.AMBIENT_BLACKOUT.getId())) {
            playerBlackout.setBlackoutTicks(GameConstants.BLACKOUT_MAX_DURATION);
            KinsWatheInitializeClient.BLACKOUT_TIME = System.currentTimeMillis() + (GameConstants.BLACKOUT_MAX_DURATION * 50L);
        }
    }
}
