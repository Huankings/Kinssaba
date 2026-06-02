package org.BsXinQin.kinswathe.client.mixin.host;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

@Mixin(value = RoundTextRenderer.class, priority = 500)
public class EndTextRendererMixin {

    @Unique private static final ThreadLocal<Integer> CIVILIAN_TOTAL = new ThreadLocal<>();
    @Unique private static final ThreadLocal<Integer> NEUTRAL_TOTAL = new ThreadLocal<>();
    @Unique private static final ThreadLocal<Integer> NEUTRAL_COUNT = new ThreadLocal<>();
    @Unique private static final ThreadLocal<Boolean> NEUTRAL_TITLE_DRAWN = ThreadLocal.withInitial(() -> false);
    @Unique private static boolean STUPID_EXPRESS_RENDERER;
    @Unique private static final ThreadLocal<Boolean> SKIP_RENDERER = ThreadLocal.withInitial(() -> false);
    @Unique private static final int DYNAMIC_COLUMNS_BASE_COUNT = 6;
    @Unique private static final int DYNAMIC_COLUMNS_LEFT_STEP = 12;

    static {
        if (FabricLoader.getInstance().isModLoaded("stupid_express")) {
            try {
                Class.forName("pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent");
                STUPID_EXPRESS_RENDERER = true;
            } catch (Exception exception) {
                STUPID_EXPRESS_RENDERER = false;
            }
        }
    }

    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void onRenderStart(@NotNull TextRenderer renderer, @NotNull ClientPlayerEntity player, @NotNull DrawContext context, CallbackInfo ci) {
        if (!ConfigWorldComponent.KEY.get(player.getWorld()).EnableNeutralAnnouncement) {
            SKIP_RENDERER.set(false);
            CIVILIAN_TOTAL.remove();
            NEUTRAL_TOTAL.remove();
            NEUTRAL_COUNT.remove();
            NEUTRAL_TITLE_DRAWN.remove();
            clearExternalLeftExtraSectionCompat();
            return;
        }

        boolean stupidexpressCustomWin = STUPID_EXPRESS_RENDERER && isStupidExpressCustomWin(player);
        if (stupidexpressCustomWin) {
            SKIP_RENDERER.set(true);
            CIVILIAN_TOTAL.remove();
            NEUTRAL_TOTAL.remove();
            NEUTRAL_COUNT.remove();
            NEUTRAL_TITLE_DRAWN.remove();
            clearExternalLeftExtraSectionCompat();
            return;
        }

        SKIP_RENDERER.set(false);
        cacheNeutralRenderState(player);
        NEUTRAL_TITLE_DRAWN.set(false);
        int neutralCount = getNeutralTotalForCurrentFrame();
        if (neutralCount > 0) {
            setExternalLeftExtraSectionCompat(neutralCount, getNeutralColumns(getRoundTotalForColumns(player)));
        } else {
            clearExternalLeftExtraSectionCompat();
        }
    }

    @Inject(
            method = "renderHud",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/client/gui/RoundTextRenderer;renderRoundEndPlayer(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/client/gui/DrawContext;Ldev/doctor4t/wathe/cca/GameRoundEndComponent;Ldev/doctor4t/wathe/cca/GameRoundEndComponent$RoundEndData;)V",
                    ordinal = 1
            )
    )
    private static void renderNeutralPlayers(@NotNull TextRenderer renderer, @NotNull ClientPlayerEntity player, @NotNull DrawContext context, CallbackInfo ci, @NotNull @Local(name = "entry") GameRoundEndComponent.RoundEndData entry) {
        if (!ConfigWorldComponent.KEY.get(player.getWorld()).EnableNeutralAnnouncement || SKIP_RENDERER.get()) return;
        if (entry.role() == KinsWatheRoles.NEUTRAL_TEXT) {
            ensureNeutralRenderState(player);
            Integer civilianTotalValue = CIVILIAN_TOTAL.get();
            Integer neutralTotalValue = NEUTRAL_TOTAL.get();
            Integer neutralCountValue = NEUTRAL_COUNT.get();
            if (civilianTotalValue == null || neutralTotalValue == null || neutralCountValue == null) {
                return;
            }
            int civilianTotal = civilianTotalValue;
            int neutralTotal = neutralTotalValue;
            int neutralCount = neutralCountValue;

            if (!NEUTRAL_TITLE_DRAWN.get() && neutralCount > 0) {
                MutableText titleText = KinsWatheRoles.NEUTRAL_TEXT.titleText.copy();
                context.drawTextWithShadow(
                        renderer,
                        titleText,
                        (int) (-renderer.getWidth(titleText) / 2f + getNeutralGroupCenterX(civilianTotal, neutralCount)),
                        (int) getNeutralHeaderY(civilianTotal),
                        0xFFFFFF
                );
                NEUTRAL_TITLE_DRAWN.set(true);
            }

            int roundTotal = getRoundTotalForColumns(player);
            context.getMatrices().translate(
                    getNeutralColumnStartX(neutralTotal, civilianTotal, neutralCount, roundTotal) + (neutralTotal % getNeutralColumns(roundTotal)) * RoundTextRenderer.getEndSlotStepX(),
                    getNeutralGridStartY(civilianTotal) + (neutralTotal / getNeutralColumns(roundTotal)) * RoundTextRenderer.getEndSlotStepY(),
                    0
            );
            NEUTRAL_TOTAL.set(++neutralTotal);
        }
    }

    @Inject(method = "renderHud", at = @At("RETURN"))
    private static void onRenderEnd(@NotNull TextRenderer renderer, @NotNull ClientPlayerEntity player, @NotNull DrawContext context, CallbackInfo ci) {
        if (!ConfigWorldComponent.KEY.get(player.getWorld()).EnableNeutralAnnouncement) return;
        SKIP_RENDERER.remove();
        CIVILIAN_TOTAL.remove();
        NEUTRAL_TOTAL.remove();
        NEUTRAL_COUNT.remove();
        NEUTRAL_TITLE_DRAWN.remove();
        clearExternalLeftExtraSectionCompat();
    }

    @Unique
    private static boolean isStupidExpressCustomWin(@NotNull ClientPlayerEntity player) {
        if (ConfigWorldComponent.KEY.get(player.getWorld()).EnableNeutralAnnouncement) {
            if (FabricLoader.getInstance().isModLoaded("stupid_express")) {
                try {
                    Class<?> componentClass = Class.forName("pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent");
                    var keyField = componentClass.getField("KEY");
                    var key = keyField.get(null);
                    var getMethod = key.getClass().getMethod("get", Object.class);
                    var component = getMethod.invoke(key, player.getWorld());
                    var hasCustomWinnerMethod = componentClass.getMethod("hasCustomWinner");
                    return (boolean) hasCustomWinnerMethod.invoke(component);
                } catch (NoSuchFieldException | ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException ignored) {}
            }
        }
        return false;
    }

    @Unique
    private static int getNeutralTotalForCurrentFrame() {
        /*
         * 中立阵营的标题与头像布局都需要知道这一帧总共有多少个中立玩家，
         * 才能在“最后一行不足整行”时自动居中。
         * 这里直接返回进入渲染阶段前缓存好的总人数，
         * 这样既能保证布局正确，也不会在“本局没有中立”时平白多预留出一整块空高度。
         */
        Integer neutralCount = NEUTRAL_COUNT.get();
        return neutralCount == null ? 0 : neutralCount;
    }

    @Unique
    private static void cacheNeutralRenderState(@NotNull ClientPlayerEntity player) {
        int civilianTotal = 0;
        int neutralCount = 0;
        for (var roundEndData : GameRoundEndComponent.KEY.get(player.getWorld()).getPlayers()) {
            if (roundEndData.role() == RoleAnnouncementTexts.CIVILIAN) {
                civilianTotal += 1;
            }
            if (roundEndData.role() == KinsWatheRoles.NEUTRAL_TEXT) {
                neutralCount += 1;
            }
        }
        CIVILIAN_TOTAL.set(civilianTotal);
        NEUTRAL_TOTAL.set(0);
        NEUTRAL_COUNT.set(neutralCount);
    }

    @Unique
    private static void ensureNeutralRenderState(@NotNull ClientPlayerEntity player) {
        if (CIVILIAN_TOTAL.get() == null || NEUTRAL_TOTAL.get() == null || NEUTRAL_COUNT.get() == null) {
            cacheNeutralRenderState(player);
        }
    }

    @Unique
    private static float getNeutralGroupCenterX(int civilianTotal, int neutralTotal) {
        int roundTotal = getCurrentRoundTotalOrFallback(civilianTotal, neutralTotal);
        Float reflectedValue = invokeRoundRendererFloat(
                "getCivilianExtraSectionGroupCenterX",
                new Class[]{int.class, int.class, int.class, int.class, int.class},
                neutralTotal,
                getNeutralColumns(roundTotal),
                civilianTotal,
                0,
                0
        );
        if (reflectedValue != null) {
            return reflectedValue;
        }
        return RoundTextRenderer.getCivilianColumnStartX()
                + getGroupWidth(Math.min(neutralTotal, getNeutralColumns(roundTotal))) / 2f;
    }

    @Unique
    private static float getNeutralColumnStartX(int index, int civilianTotal, int neutralTotal, int roundTotal) {
        Float reflectedValue = invokeRoundRendererFloat(
                "getCivilianExtraSectionColumnStartX",
                new Class[]{int.class, int.class, int.class, int.class, int.class, int.class},
                index,
                neutralTotal,
                getNeutralColumns(roundTotal),
                civilianTotal,
                0,
                0
        );
        if (reflectedValue != null) {
            return reflectedValue;
        }
        return getFallbackAlignedRowStartX(index, neutralTotal, getNeutralColumns(roundTotal), RoundTextRenderer.getCivilianColumnStartX());
    }

    @Unique
    private static float getNeutralHeaderY(int civilianTotal) {
        Float reflectedValue = invokeRoundRendererFloat(
                "getExtraSectionHeaderYForCivilian",
                new Class[]{int.class},
                civilianTotal
        );
        if (reflectedValue != null) {
            return reflectedValue;
        }
        return RoundTextRenderer.getExtraSectionHeaderY(civilianTotal, getCivilianColumns(civilianTotal));
    }

    @Unique
    private static float getNeutralGridStartY(int civilianTotal) {
        Float reflectedValue = invokeRoundRendererFloat(
                "getExtraSectionGridStartYForCivilian",
                new Class[]{int.class},
                civilianTotal
        );
        if (reflectedValue != null) {
            return reflectedValue;
        }
        return RoundTextRenderer.getExtraSectionGridStartY(civilianTotal, getCivilianColumns(civilianTotal));
    }

    @Unique
    private static int getNeutralColumns(int total) {
        Integer reflectedValue = invokeRoundRendererInt(
                "getEndGridColumnsCivilian",
                new Class[]{int.class},
                total
        );
        if (reflectedValue != null) {
            return reflectedValue;
        }
        return getDynamicColumnsFallback(total);
    }

    @Unique
    private static int getCivilianColumns(int total) {
        Integer reflectedValue = invokeRoundRendererInt(
                "getEndGridColumnsCivilian",
                new Class[]{int.class},
                total
        );
        if (reflectedValue != null) {
            return reflectedValue;
        }
        return getDynamicColumnsFallback(total);
    }

    @Unique
    private static int getDynamicColumnsFallback(int roundTotal) {
        int effectiveTotal = Math.max(roundTotal, DYNAMIC_COLUMNS_BASE_COUNT);
        return 4 + Math.max(0, (effectiveTotal - DYNAMIC_COLUMNS_BASE_COUNT) / DYNAMIC_COLUMNS_LEFT_STEP);
    }

    @Unique
    private static int getCurrentRoundTotalOrFallback(int civilianTotal, int neutralTotal) {
        Integer cachedCivilianTotal = CIVILIAN_TOTAL.get();
        Integer cachedNeutralCount = NEUTRAL_COUNT.get();
        int fallbackTotal = Math.max(0, civilianTotal) + Math.max(0, neutralTotal);
        int cachedTotal = Math.max(0, cachedCivilianTotal == null ? 0 : cachedCivilianTotal)
                + Math.max(0, cachedNeutralCount == null ? 0 : cachedNeutralCount);
        return Math.max(fallbackTotal, cachedTotal);
    }

    @Unique
    private static int getRoundTotalForColumns(@NotNull ClientPlayerEntity player) {
        /*
         * 中立阵营的动态扩列要和宿主 Wathe 完全一致，
         * 这里直接按本局结算名单总人数来算，而不是只看左侧阵营人数。
         */
        return Math.max(DYNAMIC_COLUMNS_BASE_COUNT, GameRoundEndComponent.KEY.get(player.getWorld()).getPlayers().size());
    }

    @Unique
    private static float getFallbackAlignedRowStartX(int index, int total, int columns, float baseStartX) {
        /*
         * 这里的回退逻辑继续采用左对齐，
         * 对应宿主 Wathe 里默认的“左侧阵营起点模式 = 左起”。
         * 如果以后你把宿主常量改成居中或右起，建议 KinsWathe 也同步加一个同值常量。
         */
        return baseStartX;
    }

    @Unique
    private static float getGroupWidth(int columns) {
        if (columns <= 0) {
            columns = 1;
        }
        return 34f + Math.max(0, columns - 1) * RoundTextRenderer.getEndSlotStepX();
    }

    @Unique
    private static Float invokeRoundRendererFloat(String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = RoundTextRenderer.class.getMethod(methodName, parameterTypes);
            Object result = method.invoke(null, args);
            if (result instanceof Number number) {
                return number.floatValue();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        return null;
    }

    @Unique
    private static Integer invokeRoundRendererInt(String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = RoundTextRenderer.class.getMethod(methodName, parameterTypes);
            Object result = method.invoke(null, args);
            if (result instanceof Number number) {
                return number.intValue();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        return null;
    }

    @Unique
    private static void setExternalLeftExtraSectionCompat(int count, int columns) {
        invokeRoundRendererVoid(
                "setExternalLeftExtraSection",
                new Class[]{int.class, int.class},
                count,
                columns
        );
    }

    @Unique
    private static void clearExternalLeftExtraSectionCompat() {
        invokeRoundRendererVoid(
                "clearExternalLeftExtraSection",
                new Class[0]
        );
    }

    @Unique
    private static void invokeRoundRendererVoid(String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = RoundTextRenderer.class.getMethod(methodName, parameterTypes);
            method.invoke(null, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
    }
}
