package org.BsXinQin.kinswathe.client.hud;

import org.BsXinQin.kinswathe.client.hud.gui.StaminaHud;
import org.BsXinQin.kinswathe.client.hud.host.GameSafeHud;

/**
 * kinssaba 接入 Wathe 通用 HUD API 的注册入口。
 *
 * <p>这里只负责聚合，具体 HUD 仍按原来的功能分类放在 gui / host 子包，
 * 避免迁移出 mixin 后把所有渲染逻辑塞进一个大类。</p>
 */
public final class KinsHudHandlers {
    private KinsHudHandlers() {
    }

    public static void register() {
        StaminaHud.register();
        GameSafeHud.register();
    }
}
