package org.BsXinQin.kinswathe.client.visibility;

import dev.doctor4t.wathe.api.client.invisibility.HeldItemInvisibilityApi;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;

/**
 * Kinssaba 接入 Wathe 手持物不可见 API 的统一注册处。
 *
 * <p>旧 Mixin 是“只要拿着这个物品就隐藏”，现在按你的确认改成：
 * 必须是对应职业拿着对应物品，其他局内存活玩家才看不见。</p>
 */
public final class KinsHeldItemVisibilityHandlers {
    private KinsHeldItemVisibilityHandlers() {
    }

    public static void register() {
        // 技术员的捕获装置：只有 Technician 自己拿出时才对别人不可见。
        HeldItemInvisibilityApi.registerHiddenItem(KinsWatheRoles.TECHNICIAN, KinsWatheItems.CAPTURE_DEVICE);
    }
}
