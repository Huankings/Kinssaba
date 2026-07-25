package org.BsXinQin.kinswathe.client.hud.gui;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.client.hud.HudOverlayApi;
import dev.doctor4t.wathe.api.client.hud.HudOverlayLayer;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.jetbrains.annotations.NotNull;

/**
 * kinssaba 体力条 HUD。
 */
public final class StaminaHud {
    private static final Identifier STAMINA_BAR_TEXTURE = Identifier.of(KinsWathe.MOD_ID, "textures/gui/container/stamina_bar.png");
    private static final int STAMINA_BAR_Y_OFFSET = 44;

    private StaminaHud() {
    }

    public static void register() {
        HudOverlayApi.register(KinsWathe.id("hud/stamina"), HudOverlayLayer.MAIN_HUD, HudOverlayApi.DEFAULT_PRIORITY, context -> {
            ClientPlayerEntity player = context.player();
            /*
             * 这里同时满足两层要求：
             * 1. Wathe 统一的 aliveAndSurvival 为 false 时不渲染，避免非存活玩家残留 HUD；
             * 2. kinssaba 原本刻意按原版游戏模式隐藏创造/旁观视角，特殊存活旁观也不显示体力条。
             */
            if (!context.aliveAndSurvival() || !shouldRenderInCurrentGameMode(player)) {
                return;
            }

            GameWorldComponent gameWorld = context.gameWorld();
            if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
                    && gameWorld.getGameStatus() != GameWorldComponent.GameStatus.STOPPING) {
                return;
            }

            Role role = gameWorld.getRole(player);
            if (!KinsWatheConfig.HANDLER.instance().EnableStaminaBar || context.hudHidden() || role == null) {
                return;
            }

            int maxSprintTime = role.getMaxSprintTime();
            if (maxSprintTime == -1) {
                renderInfiniteStaminaBar(context.drawContext());
            } else {
                NbtCompound nbt = player.writeNbt(new NbtCompound());
                renderRequiredStaminaBar(context.drawContext(), nbt.getFloat("sprintingTicks"), maxSprintTime);
            }
        });
    }

    private static boolean shouldRenderInCurrentGameMode(@NotNull ClientPlayerEntity player) {
        return !player.isSpectator() && !player.isCreative();
    }

    private static void renderRequiredStaminaBar(@NotNull DrawContext context, float sprintTime, float maxSprintTime) {
        int textureWidth = 174;
        int textureHeight = 11;
        int innerWidth = 166;
        int innerHeight = 3;
        int horizontalBorder = (textureWidth - innerWidth) / 2;
        int verticalBorder = (textureHeight - innerHeight) / 2;
        int x = context.getScaledWindowWidth() / 2 - textureWidth / 2;
        int y = context.getScaledWindowHeight() - STAMINA_BAR_Y_OFFSET;
        float percent = Math.max(0, Math.min(1, sprintTime / maxSprintTime));
        context.drawTexture(STAMINA_BAR_TEXTURE, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        int fillWidth = (int) (innerWidth * percent);
        if (fillWidth <= 0) {
            return;
        }
        int barX = x + horizontalBorder;
        int barY = y + verticalBorder;
        int red;
        int green;
        if (percent > 0.5f) {
            float t = (1 - percent) * 2;
            red = (int) (255 * t);
            green = 255;
        } else {
            float t = percent * 2;
            red = 255;
            green = (int) (255 * t);
        }
        int barColor = 0xFF000000 | (red << 16) | (green << 8);
        context.fill(barX, barY, barX + fillWidth, barY + innerHeight, barColor);
    }

    private static void renderInfiniteStaminaBar(@NotNull DrawContext context) {
        int textureWidth = 174;
        int textureHeight = 11;
        int innerWidth = 166;
        int innerHeight = 3;
        int horizontalBorder = (textureWidth - innerWidth) / 2;
        int verticalBorder = (textureHeight - innerHeight) / 2;
        int x = context.getScaledWindowWidth() / 2 - textureWidth / 2;
        int y = context.getScaledWindowHeight() - STAMINA_BAR_Y_OFFSET;
        context.drawTexture(STAMINA_BAR_TEXTURE, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        int fillWidth = innerWidth;
        int barX = x + horizontalBorder;
        int barY = y + verticalBorder;
        context.fill(barX, barY, barX + fillWidth, barY + innerHeight, 0xFF00FF00);
    }
}
