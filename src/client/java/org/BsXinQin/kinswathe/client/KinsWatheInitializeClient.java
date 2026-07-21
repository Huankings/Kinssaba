package org.BsXinQin.kinswathe.client;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheEntities;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;
import org.BsXinQin.kinswathe.client.items.ItemExtraModel;
import org.BsXinQin.kinswathe.client.items.ItemToolTip;
import org.BsXinQin.kinswathe.client.role_name.KinsRoleNameHudHandlers;
import org.BsXinQin.kinswathe.client.roles.licensed_villain.LicensedVillainMoodHud;
import org.BsXinQin.kinswathe.client.roles.robot.RobotMoodHud;
import org.BsXinQin.kinswathe.client.roles.technician.CaptureDeviceEntityRenderer;
import org.BsXinQin.kinswathe.client.visibility.KinsHeldItemVisibilityHandlers;
import org.BsXinQin.kinswathe.packet.host.AbilityC2SPacket;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.lwjgl.glfw.GLFW;

public class KinsWatheInitializeClient {

    public static KeyBinding abilityBind;
    public static long BLACKOUT_TIME = 0;

    /// 设置技能按键
    public static void registerAbilityKey() {
        if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
            if (abilityBind == null) ClientTickEvents.START_CLIENT_TICK.register(client -> {
                abilityBind = NoellesrolesClient.abilityBind;
            });
        } else if (!FabricLoader.getInstance().isModLoaded("noellesroles") && FabricLoader.getInstance().isModLoaded("starexpress")) {
            abilityBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + KinsWathe.MOD_ID + ".ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.wathe.keybinds"));
        } else {
            abilityBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + KinsWathe.MOD_ID + ".ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.wathe.keybinds"));
        }
    }

    /// 设置有技能的角色
    public static void setRoleAbilityPackets() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (abilityBind == null) return;
            if (abilityBind.isPressed()) {
                client.execute(() -> {
                    if (MinecraftClient.getInstance().player == null) return;
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
                    boolean sendAbilityPacket = false;
                    Role[] rolesWithAbility = new Role[]{
                            KinsWatheRoles.BELLRINGER,
                            KinsWatheRoles.DETECTIVE,
                            KinsWatheRoles.HUNTER,
                            KinsWatheRoles.ROBOT,
                            KinsWatheRoles.CLEANER
                    };
                    for (Role role : rolesWithAbility) {
                        if (gameWorld.isRole(MinecraftClient.getInstance().player, role)) sendAbilityPacket = true;
                    }
                    if (!sendAbilityPacket) return;
                    ClientPlayNetworking.send(new AbilityC2SPacket());
                });
            }
        });
    }

    /// 注册实体渲染
    public static void registerEntityRender() {
        EntityRendererRegistry.register(KinsWatheEntities.CAPTURE_DEVICE, CaptureDeviceEntityRenderer::new);
    }

    /// 添加物品描述和模型
    public static void addItemTipAndModel() {
        ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> {
            //添加KinsWathe物品描述
            ItemToolTip.addItemtip(KinsWatheItems.BLOWGUN, itemStack, list);
            ItemToolTip.addItemtip(KinsWatheItems.CAPTURE_DEVICE, itemStack, list);
            ItemToolTip.addItemtip(KinsWatheItems.HUNTING_KNIFE, itemStack, list);
            ItemToolTip.addItemtip(KinsWatheItems.KNOCKOUT_DRUG, itemStack, list);
            ItemToolTip.addItemtip(KinsWatheItems.MEDICAL_KIT, itemStack, list);
            ItemToolTip.addItemtip(KinsWatheItems.PAN, itemStack, list);
            ItemToolTip.addItemtip(KinsWatheItems.PILL, itemStack, list);
            ItemToolTip.addItemtip(KinsWatheItems.POISON_INJECTOR, itemStack, list);
            ItemToolTip.addItemtip(KinsWatheItems.SULFURIC_ACID_BARREL, itemStack, list);
            ItemToolTip.addItemtip(KinsWatheItems.WRENCH, itemStack, list);
            //添加NoellreRoles物品冷却描述
            if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
                ItemToolTip.addCooldowntip(Registries.ITEM.get(Identifier.of("noellesroles", "fake_revolver")), itemStack, list);
            }
            //添加图标描述
            ItemToolTip.addItemtip(KinsWatheItems.ICON_POWER_RESTORATION, itemStack, list);
        }));
        //注册物品额外材质
        ItemExtraModel.registerExtraModel(KinsWatheItems.POISON_INJECTOR);
    }

    public static void init() {
        // 统一把所有 KinsWathe 本能透视/能力描边接入 Wathe 的公开 Instinct API。
        KinsWatheInstinctHandlers.register();
        // 准心名字周围的职业提示 / 同伙规则统一接入 Wathe 的 RoleName HUD API。
        KinsRoleNameHudHandlers.register();
        // 按职业分别注册心情 HUD 样式，避免不同职业的渲染逻辑继续塞进同一个 MoodRenderer mixin。
        LicensedVillainMoodHud.register();
        RobotMoodHud.register();
        // 手持物不可见规则统一接入 Wathe API，替代旧的 HandView / HandPos Mixin。
        KinsHeldItemVisibilityHandlers.register();
        //设置技能按键
        registerAbilityKey();
        //添加有技能的角色
        setRoleAbilityPackets();
        //注册实体渲染
        registerEntityRender();
        //添加物品描述和模型
        addItemTipAndModel();
    }
}
