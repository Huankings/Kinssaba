package org.BsXinQin.kinswathe.client.role_name.support;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

/**
 * kinssaba 准心额外 HUD 的绘制工具。
 *
 * <p>Hacker、Detective、Physician 都沿用旧 mixin 的同一行位置：
 * 准心名字下方 32px，并用 0.6 倍缩放绘制。</p>
 */
public final class KinsRoleNameHudRenderSupport {
    private KinsRoleNameHudRenderSupport() {
    }

    public static void drawCentered(@NotNull TextRenderer renderer,
                                    @NotNull DrawContext context,
                                    @NotNull Text text,
                                    int y,
                                    int color) {
        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2.0F, context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
        context.getMatrices().scale(0.6F, 0.6F, 1.0F);
        context.drawTextWithShadow(renderer, text, -renderer.getWidth(text) / 2, y, color);
        context.getMatrices().pop();
    }
}
