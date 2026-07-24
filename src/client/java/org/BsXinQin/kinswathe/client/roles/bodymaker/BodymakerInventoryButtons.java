package org.BsXinQin.kinswathe.client.roles.bodymaker;

import dev.doctor4t.wathe.api.client.inventory.InventoryButtonContext;
import dev.doctor4t.wathe.api.client.inventory.InventoryButtonExtension;
import dev.doctor4t.wathe.api.client.inventory.InventoryButtonLayout;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.inventory.KinsInventoryButtonSupport;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BodymakerInventoryButtons {
    private BodymakerInventoryButtons() {
    }

    public static void register() {
        KinsInventoryButtonSupport.registerLimited("bodymaker", KinsWatheRoles.BODYMAKER, context -> new Extension());
    }

    private static final class Extension implements InventoryButtonExtension, BodymakerScreenCallback {
        private final KinsInventoryButtonSupport.PagedButtons<BodymakerPlayerWidget> playerButtons = new KinsInventoryButtonSupport.PagedButtons<>("bodymaker");
        private final Identifier deathReasonGroup = KinsInventoryButtonSupport.id("inventory_group/bodymaker_death_reasons");
        private final Identifier roleInputGroup = KinsInventoryButtonSupport.id("inventory_group/bodymaker_role_input");
        private InventoryButtonContext context;
        private int selectedLevel = 0;
        private UUID selectedPlayerUuid;
        private String selectedDeathReason;

        @Override
        public void init(@NotNull InventoryButtonContext context) {
            this.context = context;
            this.selectedLevel = 0;
            this.selectedPlayerUuid = null;
            this.selectedDeathReason = null;
            this.rebuild();
        }

        @Override
        public boolean allowInventoryKeyClose(@NotNull InventoryButtonContext context, int keyCode, int scanCode) {
            return this.selectedLevel != 2 && !BodymakerRoleWidget.stopClosing;
        }

        @Override
        public void close(@NotNull InventoryButtonContext context) {
            BodymakerRoleWidget.stopClosing = false;
        }

        @Override
        public void setSelectedPlayer(@NotNull UUID uuid) {
            this.selectedPlayerUuid = uuid;
            this.selectedLevel = 1;
            this.rebuild();
        }

        @Override
        public void setSelectedDeathReason(@NotNull String deathReason) {
            this.selectedDeathReason = deathReason;
            this.selectedLevel = 2;
            this.rebuild();
        }

        private void rebuild() {
            InventoryButtonContext context = this.context;
            if (context == null) {
                return;
            }
            LimitedInventoryScreen screen = context.requireLimitedScreen();
            ClientPlayerEntity player = context.requirePlayer();
            int y = InventoryButtonLayout.getPlayerRowY(context.height());
            context.clearGroup(this.deathReasonGroup);
            context.clearGroup(this.roleInputGroup);
            this.playerButtons.reset(context);

            if (this.selectedLevel == 0) {
                for (UUID targetUuid : KinsInventoryButtonSupport.onlineUuids(player)) {
                    PlayerListEntry entry = KinsInventoryButtonSupport.entry(player, targetUuid);
                    if (entry != null) {
                        this.playerButtons.addWidget(context, new BodymakerPlayerWidget(screen, 0, y, targetUuid, entry, this));
                    }
                }
                this.playerButtons.addPageButtons(context);
            } else if (this.selectedLevel == 1) {
                List<Item> deathReasons = deathReasons();
                int x = context.width() / 2 - deathReasons.size() * InventoryButtonLayout.SLOT_APART / 2 + InventoryButtonLayout.SLOT_X_OFFSET;
                for (int i = 0; i < deathReasons.size(); i++) {
                    context.addWidget(this.deathReasonGroup, new BodymakerDeathReasonWidget(screen, x + InventoryButtonLayout.SLOT_APART * i, y, deathReasons.get(i), i, this.selectedPlayerUuid, this));
                }
            } else if (this.selectedLevel == 2 && this.selectedPlayerUuid != null && this.selectedDeathReason != null) {
                BodymakerRoleWidget widget = context.addWidget(this.roleInputGroup, new BodymakerRoleWidget(screen, context.textRenderer(), context.width() / 2 - 100, y, this.selectedPlayerUuid, this.selectedDeathReason));
                widget.setFocused(true);
            }
        }

        private static List<Item> deathReasons() {
            List<Item> deathReasons = new ArrayList<>();
            deathReasons.add(WatheItems.KNIFE);
            deathReasons.add(WatheItems.REVOLVER);
            deathReasons.add(WatheItems.GRENADE);
            deathReasons.add(WatheItems.BAT);
            deathReasons.add(WatheItems.POISON_VIAL);
            if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
                deathReasons.add(Items.OMINOUS_BOTTLE);
            }
            if (FabricLoader.getInstance().isModLoaded("starexpress")) {
                deathReasons.add(Registries.ITEM.get(Identifier.of("starexpress", "tape")));
            }
            if (FabricLoader.getInstance().isModLoaded("stupid_express")) {
                deathReasons.add(Registries.ITEM.get(Identifier.of("stupid_express", "lighter")));
            }
            return deathReasons;
        }
    }
}
