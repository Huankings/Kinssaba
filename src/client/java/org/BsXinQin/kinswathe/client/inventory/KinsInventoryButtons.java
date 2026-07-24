package org.BsXinQin.kinswathe.client.inventory;

import org.BsXinQin.kinswathe.client.roles.bodymaker.BodymakerInventoryButtons;
import org.BsXinQin.kinswathe.client.roles.judge.JudgeInventoryButtons;

/**
 * kinssaba 背包按钮注册入口。
 *
 * <p>各职业自己的按钮逻辑放回对应职业客户端包里，这里只负责统一调用注册。</p>
 */
public final class KinsInventoryButtons {
    private KinsInventoryButtons() {
    }

    public static void register() {
        JudgeInventoryButtons.register();
        BodymakerInventoryButtons.register();
    }
}
