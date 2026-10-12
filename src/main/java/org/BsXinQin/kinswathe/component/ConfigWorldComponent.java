package org.BsXinQin.kinswathe.component;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class ConfigWorldComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<ConfigWorldComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(KinsWathe.MOD_ID, "config"), ConfigWorldComponent.class);

    private final World world;
    public void sync() {KEY.sync(this.world);}
    public ConfigWorldComponent(@NotNull World world) {this.world = world;}
    @Override
    public void serverTick() {
        /*
         * 配置组件过去每 tick 向全服广播完整 NBT，是挂机时仍持续占用带宽的直接来源。
         * 保留 tick 检查是为了让指令/YACL 修改能立即生效，但只有快照真的变化才发包。
         */
        if (this.refreshSnapshot()) {
            this.sync();
        }
    }

    private boolean refreshSnapshot() {
        var config = KinsWatheConfig.HANDLER.instance();
        boolean changed = this.StartingCooldown != config.StartingCooldown
                || this.EnableJumpNotInGame != config.EnableJumpNotInGame
                || this.EnableStartSafeTime != config.EnableStartSafeTime
                || this.EnableAutoPsychoInstinct != config.EnableAutoPsychoInstinct
                || this.BodymakerAbilityFakeRole != config.BodymakerAbilityFakeRole
                || this.JudgeAbilityPrice != config.JudgeAbilityPrice
                || this.TechnicianWrenchPrice != config.TechnicianWrenchPrice
                || this.TechnicianCaptureDevicePrice != config.TechnicianCaptureDevicePrice
                || this.TechnicianPowerRestorationPrice != config.TechnicianPowerRestorationPrice;
        this.StartingCooldown = config.StartingCooldown;
        this.EnableJumpNotInGame = config.EnableJumpNotInGame;
        this.EnableStartSafeTime = config.EnableStartSafeTime;
        this.EnableAutoPsychoInstinct = config.EnableAutoPsychoInstinct;
        this.BodymakerAbilityFakeRole = config.BodymakerAbilityFakeRole;
        this.JudgeAbilityPrice = config.JudgeAbilityPrice;
        this.TechnicianWrenchPrice = config.TechnicianWrenchPrice;
        this.TechnicianCaptureDevicePrice = config.TechnicianCaptureDevicePrice;
        this.TechnicianPowerRestorationPrice = config.TechnicianPowerRestorationPrice;
        return changed;
    }

    /// 具体配置内容
    //全局配置
    public int StartingCooldown = GameConstants.getInTicks(0,30) / 20;
    public boolean EnableJumpNotInGame = false;
    public boolean EnableStartSafeTime = false;
    public boolean EnableAutoPsychoInstinct = true;
    //关于KinsWathe修改
    public boolean BodymakerAbilityFakeRole = true;
    public int JudgeAbilityPrice = 300;
    public int TechnicianWrenchPrice = 100;
    public int TechnicianCaptureDevicePrice = 100;
    public int TechnicianPowerRestorationPrice = 300;

    /// 同步服务端客户端配置
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        //全局配置
        this.refreshSnapshot();
        tag.putInt("StartingCooldown", this.StartingCooldown);
        tag.putBoolean("EnableJumpNotInGame", this.EnableJumpNotInGame);
        tag.putBoolean("EnableStartSafeTime", this.EnableStartSafeTime);
        tag.putBoolean("EnableAutoPsychoInstinct", this.EnableAutoPsychoInstinct);
        //关于KinsWathe修改
        tag.putBoolean("BodymakerAbilityFakeRole", this.BodymakerAbilityFakeRole);
        tag.putInt("JudgeAbilityPrice", this.JudgeAbilityPrice);
        tag.putInt("TechnicianWrenchPrice", this.TechnicianWrenchPrice);
        tag.putInt("TechnicianCaptureDevicePrice", this.TechnicianCaptureDevicePrice);
        tag.putInt("TechnicianPowerRestorationPrice", this.TechnicianPowerRestorationPrice);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        //全局配置
        if (tag.contains("StartingCooldown"))   this.StartingCooldown = tag.getInt("StartingCooldown");
        if (tag.contains("EnableJumpNotInGame"))   this.EnableJumpNotInGame = tag.getBoolean("EnableJumpNotInGame");
        if (tag.contains("EnableStartSafeTime"))   this.EnableStartSafeTime = tag.getBoolean("EnableStartSafeTime");
        if (tag.contains("EnableAutoPsychoInstinct"))   this.EnableAutoPsychoInstinct = tag.getBoolean("EnableAutoPsychoInstinct");
        //关于KinsWathe修改
        if (tag.contains("BodymakerAbilityFakeRole"))   this.BodymakerAbilityFakeRole = tag.getBoolean("BodymakerAbilityFakeRole");
        if (tag.contains("JudgeAbilityPrice"))   this.JudgeAbilityPrice = tag.getInt("JudgeAbilityPrice");
        if (tag.contains("TechnicianWrenchPrice"))   this.TechnicianWrenchPrice = tag.getInt("TechnicianWrenchPrice");
        if (tag.contains("TechnicianCaptureDevicePrice"))   this.TechnicianCaptureDevicePrice = tag.getInt("TechnicianCaptureDevicePrice");
        if (tag.contains("TechnicianPowerRestorationPrice"))   this.TechnicianPowerRestorationPrice = tag.getInt("TechnicianPowerRestorationPrice");
    }
}
