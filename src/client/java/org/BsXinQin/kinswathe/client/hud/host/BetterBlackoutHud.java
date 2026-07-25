package org.BsXinQin.kinswathe.client.hud.host;

import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.api.client.hud.HudOverlayApi;
import dev.doctor4t.wathe.api.client.hud.HudOverlayLayer;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.client.KinsWatheInitializeClient;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.roles.technician.TechnicianComponent;

/**
 * kinssaba 增强停电遮罩 HUD。
 */
public final class BetterBlackoutHud {
    private static long insideTime = 0;
    private static boolean outside = true;
    private static long outsideTime = 0;
    private static boolean wasInside = false;
    private static long instinctChangeTime = 0;
    private static boolean lastInstinctState = false;
    private static float instinctStartAlpha = 0;
    private static float currentAlpha = 0;

    private BetterBlackoutHud() {
    }

    public static void register() {
        HudOverlayApi.register(KinsWathe.id("hud/better_blackout"), HudOverlayLayer.BEFORE_HUD, HudOverlayApi.DEFAULT_PRIORITY, context -> {
            if (!ConfigWorldComponent.KEY.get(context.player().getWorld()).EnableBetterBlackout) {
                return;
            }

            TechnicianComponent blackout = TechnicianComponent.KEY.get(context.player());
            long currentTime = System.currentTimeMillis();
            long blackoutTime = KinsWatheInitializeClient.BLACKOUT_TIME;
            if (!context.gameWorld().isRunning() || currentTime >= blackoutTime || blackout.blackoutTicks <= 0) {
                return;
            }

            boolean isOutside = Wathe.isSkyVisibleAdjacent(context.player());
            boolean isInstinctEnabled = WatheClient.isInstinctEnabled();
            float alphaBeforeChange = currentAlpha;
            if (isInstinctEnabled != lastInstinctState) {
                instinctChangeTime = currentTime;
                instinctStartAlpha = alphaBeforeChange;
                lastInstinctState = isInstinctEnabled;
            }
            if (outside && !isOutside) {
                insideTime = currentTime;
            }
            if (!outside && isOutside) {
                outsideTime = currentTime;
                wasInside = true;
            }
            outside = isOutside;

            /*
             * 停电黑幕是局内视觉限制，统一使用 Wathe 的 aliveAndSurvival 入口；
             * 玩家死亡、旁观或创造后立即停止渲染，避免非存活视角仍被旧黑幕遮挡。
             */
            if (context.aliveAndSurvival() && !context.player().hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                int alpha = calculateAlpha(isOutside, isInstinctEnabled, getBlackoutAlpha(blackoutTime, currentTime), currentTime);
                currentAlpha = alpha;
                if (alpha > 0) {
                    context.drawContext().fill(0, 0, context.width(), context.height(), alpha << 24);
                }
            }
        });
    }

    private static int calculateAlpha(boolean isOutside, boolean isInstinctEnabled, int targetAlpha, long currentTime) {
        long timeSinceInstinctChange = currentTime - instinctChangeTime;
        float instinctProgress = MathHelper.clamp((float) timeSinceInstinctChange / 500, 0f, 1f);
        float baseTarget;
        if (isOutside) {
            if (wasInside) {
                long timeOutside = currentTime - outsideTime;
                if (timeOutside < 500) {
                    float outsideProgress = (float) timeOutside / 500;
                    baseTarget = targetAlpha * (1 - outsideProgress);
                } else {
                    wasInside = false;
                    baseTarget = 0;
                }
            } else {
                baseTarget = 0;
            }
        } else {
            long timeInside = currentTime - insideTime;
            if (timeInside < 500) {
                float insideProgress = (float) timeInside / 500;
                baseTarget = targetAlpha * insideProgress;
            } else {
                baseTarget = targetAlpha;
            }
        }

        float finalTarget = isInstinctEnabled ? 0 : baseTarget;
        if (timeSinceInstinctChange < 500) {
            return (int) (instinctStartAlpha + (finalTarget - instinctStartAlpha) * instinctProgress);
        }
        return (int) finalTarget;
    }

    private static int getBlackoutAlpha(long blackoutTime, long currentTime) {
        long startTime = blackoutTime - (GameConstants.BLACKOUT_MAX_DURATION * 50L);
        long fadeStartTime = startTime + (GameConstants.BLACKOUT_MIN_DURATION * 50L);
        if (currentTime < fadeStartTime) {
            return (int) (255 * 0.8f);
        }

        long fadeDuration = (GameConstants.BLACKOUT_MAX_DURATION - GameConstants.BLACKOUT_MIN_DURATION) * 50L;
        long fadeElapsed = currentTime - fadeStartTime;
        float progress = MathHelper.clamp((float) fadeElapsed / fadeDuration, 0f, 1f);
        return (int) (255 * 0.8f * (1 - progress));
    }
}
