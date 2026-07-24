package org.BsXinQin.kinswathe.client.inventory;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.client.inventory.InventoryButtonApi;
import dev.doctor4t.wathe.api.client.inventory.InventoryButtonContext;
import dev.doctor4t.wathe.api.client.inventory.InventoryButtonExtension;
import dev.doctor4t.wathe.api.client.inventory.InventoryButtonLayout;
import dev.doctor4t.wathe.api.client.inventory.InventoryPageState;
import dev.doctor4t.wathe.api.client.inventory.InventoryPageSwitchWidget;
import dev.doctor4t.wathe.api.client.inventory.InventoryScreenType;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * kinssaba 背包按钮共享工具。
 *
 * <p>只保留分页、玩家列表和 Wathe API 注册这些跨职业共用逻辑；
 * 具体按钮流程放在各自职业包，后续维护职业时可以就近修改。</p>
 */
public final class KinsInventoryButtonSupport {
    private KinsInventoryButtonSupport() {
    }

    public static void registerLimited(@NotNull String path, @NotNull Role role, @NotNull ExtensionFactory factory) {
        InventoryButtonApi.registerProvider(id("inventory/" + path), InventoryButtonApi.DEFAULT_PRIORITY, context -> {
            if (!isLimitedRole(context, role)) {
                return null;
            }
            return factory.create(context);
        });
    }

    public static boolean isLimitedRole(@NotNull InventoryButtonContext context, @NotNull Role role) {
        if (context.type() != InventoryScreenType.LIMITED || context.player() == null || context.limitedScreen() == null) {
            return false;
        }
        ClientPlayerEntity player = context.requirePlayer();
        return GameWorldComponent.KEY.get(player.getWorld()).isRole(player, role);
    }

    public static Identifier id(String path) {
        return Identifier.of(KinsWathe.MOD_ID, path);
    }

    public static List<UUID> onlineUuids(@NotNull ClientPlayerEntity player) {
        return player.networkHandler == null ? List.of() : new ArrayList<>(player.networkHandler.getPlayerUuids());
    }

    public static @Nullable PlayerListEntry entry(@NotNull ClientPlayerEntity player, @NotNull UUID uuid) {
        return player.networkHandler == null ? null : player.networkHandler.getPlayerListEntry(uuid);
    }

    public interface ExtensionFactory {
        @Nullable InventoryButtonExtension create(@NotNull InventoryButtonContext context);
    }

    public static final class PagedButtons<W extends ClickableWidget> {
        private final Identifier pageKey;
        private final Identifier groupKey;
        private final List<W> widgets = new ArrayList<>();
        private InventoryPageSwitchWidget previous;
        private InventoryPageSwitchWidget next;
        private int currentPage;

        public PagedButtons(String key) {
            this.pageKey = id("inventory_page/" + key);
            this.groupKey = id("inventory_group/" + key);
        }

        public void reset(InventoryButtonContext context) {
            context.clearGroup(this.groupKey);
            this.widgets.clear();
            this.previous = null;
            this.next = null;
            this.currentPage = InventoryPageState.getPage(this.pageKey);
        }

        public void addWidget(InventoryButtonContext context, W widget) {
            this.widgets.add(context.addWidget(this.groupKey, widget));
        }

        public void addPageButtons(InventoryButtonContext context) {
            int y = InventoryButtonLayout.getPlayerRowY(context.height());
            this.previous = context.addWidget(this.groupKey, new InventoryPageSwitchWidget(
                    0,
                    y,
                    Items.PURPLE_DYE.getDefaultStack(),
                    Text.translatable("ui.noellesroles.pagination.previous"),
                    button -> {
                        this.currentPage--;
                        this.refresh(context, true);
                    }
            ));
            this.next = context.addWidget(this.groupKey, new InventoryPageSwitchWidget(
                    0,
                    y,
                    Items.LIME_DYE.getDefaultStack(),
                    Text.translatable("ui.noellesroles.pagination.next"),
                    button -> {
                        this.currentPage++;
                        this.refresh(context, true);
                    }
            ));
            this.refresh(context, true);
        }

        public void refresh(InventoryButtonContext context, boolean visible) {
            int totalPages = InventoryButtonLayout.getTotalPageCount(this.widgets.size());
            this.currentPage = Math.max(0, Math.min(this.currentPage, totalPages - 1));
            InventoryPageState.setPage(this.pageKey, this.currentPage);

            int startIndex = this.currentPage * InventoryButtonLayout.PLAYERS_PER_PAGE;
            int endIndex = Math.min(startIndex + InventoryButtonLayout.PLAYERS_PER_PAGE, this.widgets.size());
            int visibleCount = Math.max(0, endIndex - startIndex);
            int y = InventoryButtonLayout.getPlayerRowY(context.height());
            boolean showPrevious = visible && this.currentPage > 0;
            boolean showNext = visible && this.currentPage < totalPages - 1;
            int groupStartX = InventoryButtonLayout.getCenteredGroupStartX(context.width(), visibleCount, showPrevious, showNext);
            int playerStartX = groupStartX + (showPrevious ? InventoryButtonLayout.SLOT_APART : 0);

            for (int i = 0; i < this.widgets.size(); i++) {
                W widget = this.widgets.get(i);
                boolean widgetVisible = visible && i >= startIndex && i < endIndex;
                widget.visible = widgetVisible;
                widget.active = widgetVisible;
                if (widgetVisible) {
                    int visibleIndex = i - startIndex;
                    widget.setX(playerStartX + visibleIndex * InventoryButtonLayout.SLOT_APART);
                    widget.setY(y);
                }
            }

            if (this.previous != null) {
                this.previous.visible = showPrevious;
                this.previous.active = showPrevious;
                this.previous.setX(groupStartX);
                this.previous.setY(y);
            }
            if (this.next != null) {
                this.next.visible = showNext;
                this.next.active = showNext;
                this.next.setX(playerStartX + visibleCount * InventoryButtonLayout.SLOT_APART);
                this.next.setY(y);
            }
        }
    }
}
