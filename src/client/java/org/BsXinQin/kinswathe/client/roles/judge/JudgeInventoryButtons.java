package org.BsXinQin.kinswathe.client.roles.judge;

import dev.doctor4t.wathe.api.client.inventory.InventoryButtonContext;
import dev.doctor4t.wathe.api.client.inventory.InventoryButtonExtension;
import dev.doctor4t.wathe.api.client.inventory.InventoryButtonLayout;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.inventory.KinsInventoryButtonSupport;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class JudgeInventoryButtons {
    private JudgeInventoryButtons() {
    }

    public static void register() {
        KinsInventoryButtonSupport.registerLimited("judge", KinsWatheRoles.JUDGE, context -> new Extension());
    }

    private static final class Extension implements InventoryButtonExtension {
        private final KinsInventoryButtonSupport.PagedButtons<JudgePlayerWidget> buttons = new KinsInventoryButtonSupport.PagedButtons<>("judge");

        @Override
        public void init(@NotNull InventoryButtonContext context) {
            LimitedInventoryScreen screen = context.requireLimitedScreen();
            ClientPlayerEntity player = context.requirePlayer();
            this.buttons.reset(context);
            int y = InventoryButtonLayout.getPlayerRowY(context.height());
            List<UUID> players = KinsInventoryButtonSupport.onlineUuids(player);
            players.removeIf(uuid -> uuid.equals(player.getUuid()));
            for (UUID targetUuid : players) {
                this.buttons.addWidget(context, new JudgePlayerWidget(screen, 0, y, targetUuid, KinsInventoryButtonSupport.entry(player, targetUuid)));
            }
            this.buttons.addPageButtons(context);
        }

        @Override
        public void tick(@NotNull InventoryButtonContext context) {
            this.buttons.refresh(context, true);
        }
    }
}
