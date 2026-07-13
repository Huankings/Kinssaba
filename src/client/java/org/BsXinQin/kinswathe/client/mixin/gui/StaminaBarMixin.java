package org.BsXinQin.kinswathe.client.mixin.gui;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class StaminaBarMixin {

    @Unique private static final Identifier STAMINA_BAR_TEXTURE = Identifier.of(KinsWathe.MOD_ID, "textures/gui/container/stamina_bar.png");
    @Unique private static final int STAMINA_BAR_Y_OFFSET = 44;

    @Inject(method = "renderMainHud", at = @At("TAIL"))
    public void staminaBar(@NotNull DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        /*
         * 耐力条只跟随原版游戏模式显示：冒险 / 生存显示，创造 / 旁观隐藏。
         *
         * 这里刻意不再使用 WatheClient.isPlayerAliveAndInSurvival()。
         * 这样“特殊旁观/创造仍按玩法存活”的玩家不会被耐力条遮挡视野，
         * 同时普通冒险/生存模式下的存活玩家仍能看到耐力信息。
         */
        if (!shouldRenderInCurrentGameMode(player)) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        /*
         * ACTIVE：正常游戏中显示。
         * STOPPING：结算黑幕已经开始淡入，但玩家尚未被 reset/finalize；此时继续画在主 HUD 层，
         * 让 Wathe 自己的黑幕自然盖住耐力条，避免耐力条在黑幕刚出现时突兀消失。
         *
         * STARTING 阶段角色和游戏区域还没完全初始化，不主动显示；等进入 ACTIVE 后，
         * Wathe 的黑幕仍处于全黑再淡出，耐力条会和热栏一样从黑幕下自然露出。
         */
        if (gameWorld.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE
                || gameWorld.getGameStatus() == GameWorldComponent.GameStatus.STOPPING) {
            Role role = gameWorld.getRole(player);
            if (KinsWatheConfig.HANDLER.instance().EnableStaminaBar && !client.options.hudHidden && role != null) {
                int maxSprintTime = role.getMaxSprintTime();
                if (maxSprintTime == -1) {
                    getStaminaBarInfinite(context);
                } else {
                    NbtCompound nbt = player.writeNbt(new NbtCompound());
                    getStaminaBarRequire(context, nbt.getFloat("sprintingTicks"), maxSprintTime);
                }
            }
        }
    }

    @Unique
    private boolean shouldRenderInCurrentGameMode(@NotNull ClientPlayerEntity player) {
        return !player.isSpectator() && !player.isCreative();
    }

    @Unique
    private void getStaminaBarRequire(@NotNull DrawContext context, float sprintTime, float maxSprintTime) {
        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int textureWidth = 174;
        int textureHeight = 11;
        int innerWidth = 166;
        int innerHeight = 3;
        int horizontalBorder = (textureWidth - innerWidth) / 2;
        int verticalBorder = (textureHeight - innerHeight) / 2;
        int x = screenWidth / 2 - textureWidth / 2;
        int y = screenHeight - STAMINA_BAR_Y_OFFSET;
        float percent = Math.max(0, Math.min(1, sprintTime / maxSprintTime));
        context.drawTexture(STAMINA_BAR_TEXTURE, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        int fillWidth = (int) (innerWidth * percent);
        if (fillWidth <= 0) return;
        int barX = x + horizontalBorder;
        int barY = y + verticalBorder;
        int red, green;
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

    @Unique
    private void getStaminaBarInfinite(@NotNull DrawContext context) {
        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int textureWidth = 174;
        int textureHeight = 11;
        int innerWidth = 166;
        int innerHeight = 3;
        int horizontalBorder = (textureWidth - innerWidth) / 2;
        int verticalBorder = (textureHeight - innerHeight) / 2;
        int x = screenWidth / 2 - textureWidth / 2;
        int y = screenHeight - STAMINA_BAR_Y_OFFSET;
        context.drawTexture(STAMINA_BAR_TEXTURE, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        int fillWidth = (int) (innerWidth * (float) 1.0);
        if (fillWidth <= 0) return;
        int barX = x + horizontalBorder;
        int barY = y + verticalBorder;
        context.fill(barX, barY, barX + fillWidth, barY + innerHeight, 0xFF00FF00);
    }
}
